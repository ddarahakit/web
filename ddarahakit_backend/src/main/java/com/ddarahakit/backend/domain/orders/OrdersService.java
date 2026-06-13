package com.ddarahakit.backend.domain.orders;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.orders.model.Orders;
import com.ddarahakit.backend.domain.orders.model.OrdersDto;
import com.ddarahakit.backend.domain.orders.model.OrdersItem;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import io.portone.sdk.server.payment.CancelPaymentResponse;
import io.portone.sdk.server.payment.PaidPayment;
import io.portone.sdk.server.payment.Payment;
import io.portone.sdk.server.payment.PaymentClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrdersService {
    private final PaymentClient portone;
    private final OrdersRepository ordersRepository;
    private final OrdersItemRepository ordersItemRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public OrdersDto.OrdersRes create(AuthUserDetails authUserDetails, OrdersDto.OrdersReq dto) {
        if (dto.getCourseIdxList() == null || dto.getCourseIdxList().isEmpty()) {
            throw BaseException.of(REQUEST_ERROR);
        }

        List<Long> courseIdxList = dto.getCourseIdxList();
        if (courseIdxList.size() != courseIdxList.stream().distinct().count()) {
            throw BaseException.of(ORDERS_VALIDATION_FAIL);
        }

        List<Course> courses = courseRepository.findAllById(courseIdxList);
        if (courses.size() != courseIdxList.size()) {
            throw BaseException.of(RESPONSE_NULL_ERROR);
        }

        for (Course course : courses) {
            boolean alreadyPurchased = ordersItemRepository
                    .existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(authUserDetails.toEntity(), course);
            if (alreadyPurchased) {
                throw BaseException.of(ORDERS_VALIDATION_FAIL);
            }
        }

        int totalPrice = courses.stream().mapToInt(Course::getSalePrice).sum();
        if (!Objects.equals(dto.getPaymentPrice(), totalPrice)) {
            throw BaseException.of(ORDERS_VALIDATION_FAIL);
        }

        Orders orders = dto.toEntity(authUserDetails.toEntity());
        Orders savedOrders = ordersRepository.save(orders);


        List<OrdersItem> items = courses.stream()
                .map(course -> OrdersItem.builder()
                        .orders(savedOrders)
                        .course(course)
                        .build())
                .toList();
        orders.getItems().addAll(items);
        ordersRepository.save(orders);

        return OrdersDto.OrdersRes.of(orders);
    }

    @Transactional
    public OrdersDto.VerifyRes verify(AuthUserDetails authUserDetails, OrdersDto.VerifyReq dto) {
        CompletableFuture<Payment> completableFuture = portone.getPayment(dto.getPaymentId());
        Payment payment = completableFuture.join();

        if (payment instanceof PaidPayment paidPayment) {
            Map<String, Object> customData = new GsonBuilder()
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .create().fromJson(paidPayment.getCustomData(), Map.class);

            Long ordersIdx = toLong(customData.get("ordersIdx"));
            // items + course 를 fetch join 으로 한 번에 로딩 (N+1 제거)
            Orders orders = ordersRepository.findUnpaidWithItemsForVerify(ordersIdx, authUserDetails.toEntity()).orElseThrow(
                    () -> new BaseException(ORDERS_NOT_ORDERED)
            );
            int totalPrice = orders.getItems().stream()
                    .map(OrdersItem::getCourse)
                    .mapToInt(Course::getSalePrice)
                    .sum();

            if (paidPayment.getAmount().getTotal() != totalPrice) {
                throw BaseException.of(ORDERS_VALIDATION_FAIL);
            }

            // 멱등 확정: paid=false 인 경우에만 원자적으로 paid=true 로 전이.
            // 동시/중복 verify 시 단 1회만 성공(updated==1)하고 나머지는 ORDERS_ALREADY_PAID.
            int updated = ordersRepository.markPaidIfUnpaid(ordersIdx, dto.getPaymentId());
            if (updated == 0) {
                throw BaseException.of(ORDERS_ALREADY_PAID);
            }

            orders.setPaid(true);
            orders.setPaymentId(dto.getPaymentId());
            return OrdersDto.VerifyRes.of(orders);
        } else {
            throw BaseException.of(ORDERS_VALIDATION_FAIL);
        }
    }

    /**
     * 무료(0원) 주문 완료 처리.
     * 포트원/결제 검증을 건너뛰고 서버에서 바로 결제완료 상태로 전이한다.
     * 보안: 클라이언트 값이 아닌 서버의 코스 salePrice 합계로 0원 여부를 재검증하여
     * 유료 강의를 무료로 우회 등록하는 것을 차단한다.
     * 수강 등록(=paid=true 인 OrdersItem)은 verify 성공 경로와 동일하게 처리된다.
     */
    @Transactional
    public OrdersDto.VerifyRes freeComplete(AuthUserDetails authUserDetails, Long ordersIdx) {
        Orders orders = ordersRepository
                .findUnpaidWithItemsForFreeComplete(ordersIdx, authUserDetails.toEntity())
                .orElseThrow(() -> BaseException.of(ORDERS_NOT_ORDERED));

        // 서버 측 금액 재계산: 코스 salePrice 합계가 0일 때만 무료 완료 허용.
        int totalPrice = orders.getItems().stream()
                .map(OrdersItem::getCourse)
                .mapToInt(Course::getSalePrice)
                .sum();
        if (totalPrice != 0) {
            throw BaseException.of(ORDERS_VALIDATION_FAIL);
        }

        // 무료 주문은 포트원 결제 ID 가 없으므로 충돌하지 않는 합성 식별자를 부여한다.
        // (paymentId 유니크 제약 충족 + verify 와 동일한 멱등 확정 로직 재사용)
        String freePaymentId = "free_" + ordersIdx;

        // 멱등 확정: paid=false 인 경우에만 1회 원자적으로 paid=true 로 전이.
        int updated = ordersRepository.markPaidIfUnpaid(ordersIdx, freePaymentId);
        if (updated == 0) {
            throw BaseException.of(ORDERS_ALREADY_PAID);
        }

        orders.setPaid(true);
        orders.setPaymentId(freePaymentId);
        return OrdersDto.VerifyRes.of(orders);
    }

    public OrdersDto.OrdersRes cancel(AuthUserDetails authUserDetails, Long ordersIdx) {
        Orders orders = ordersRepository.findByIdxAndUserAndPaidFalse(ordersIdx, authUserDetails.toEntity()).orElseThrow(
                () -> BaseException.of(ORDERS_NOT_ORDERED)
        );

        ordersRepository.delete(orders);

        return OrdersDto.OrdersRes.of(orders);
    }

    @Transactional
    public OrdersDto.OrdersRes refund(AuthUserDetails authUserDetails, Long ordersIdx, OrdersDto.RefundReq dto) {
        // 1. 주문 조회 (본인 주문만)
        Orders orders = ordersRepository.findByIdxAndUser(ordersIdx, authUserDetails.toEntity())
                .orElseThrow(() -> BaseException.of(ORDERS_NOT_ORDERED));

        // 2. 결제 완료 여부 확인
        if (!Boolean.TRUE.equals(orders.getPaid())) {
            throw BaseException.of(ORDERS_NOT_PAID);
        }

        // 3. 이미 환불된 주문 확인
        if (Boolean.TRUE.equals(orders.getRefunded())) {
            throw BaseException.of(ORDERS_ALREADY_REFUNDED);
        }

        // 4. Portone 환불 API 호출 (전액 환불)
        try {
            CompletableFuture<CancelPaymentResponse> future = portone.cancelPayment(
                    orders.getPaymentId(), null, null, null,
                    dto.getReason(), null, null, null, null
            );
            future.join();
        } catch (Exception e) {
            log.error("[환불 실패] ordersIdx={}, paymentId={}, reason={}", ordersIdx, orders.getPaymentId(), e.getMessage());
            throw BaseException.of(ORDERS_REFUND_FAIL);
        }

        // 5. 환불 상태 업데이트
        orders.setRefunded(true);
        return OrdersDto.OrdersRes.of(ordersRepository.save(orders));
    }

    /**
     * 결제 내역 페이징 조회(본인 결제 완료 주문). items+course fetch join → 목록 N+1 없음.
     */
    public OrdersDto.PaymentPageRes payments(AuthUserDetails authUserDetails, org.springframework.data.domain.Pageable pageable) {
        return OrdersDto.PaymentPageRes.of(
                ordersRepository.findPaidPageByUser(authUserDetails.toEntity(), pageable));
    }

    /**
     * 영수증 데이터 조회(본인 결제 완료 주문만).
     */
    public OrdersDto.ReceiptRes receipt(AuthUserDetails authUserDetails, Long ordersIdx) {
        Orders orders = ordersRepository.findReceipt(ordersIdx, authUserDetails.toEntity())
                .orElseThrow(() -> BaseException.of(ORDERS_NOT_ORDERED));
        return OrdersDto.ReceiptRes.of(orders);
    }

    public boolean check(AuthUserDetails authUserDetails, Long courseIdx) {
        Course course = courseRepository.findById(courseIdx).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );
        boolean exists = ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(authUserDetails.toEntity(), course);
        if (!exists) {
            throw BaseException.of(ORDERS_NOT_ORDERED);
        }
        return true;
    }

    private Long toLong(Object value) {
        if (value == null) {
            throw BaseException.of(ORDERS_VALIDATION_FAIL);
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw BaseException.of(ORDERS_VALIDATION_FAIL);
        }
    }
}
