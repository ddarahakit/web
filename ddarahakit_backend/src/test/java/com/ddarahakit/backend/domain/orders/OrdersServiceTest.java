package com.ddarahakit.backend.domain.orders;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.orders.model.Orders;
import com.ddarahakit.backend.domain.orders.model.OrdersDto;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.orders.model.OrdersItem;
import io.portone.sdk.server.payment.CancelPaymentResponse;
import io.portone.sdk.server.payment.PaidPayment;
import io.portone.sdk.server.payment.PaymentAmount;
import io.portone.sdk.server.payment.PaymentClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {

    @Mock PaymentClient portone;
    @Mock OrdersRepository ordersRepository;
    @Mock OrdersItemRepository ordersItemRepository;
    @Mock CourseRepository courseRepository;
    @InjectMocks OrdersService ordersService;

    AuthUserDetails userDetails;
    User userEntity;
    Course course;
    Orders unpaidOrders;
    Orders paidOrders;
    Orders refundedOrders;

    @BeforeEach
    void setUp() {
        userDetails = AuthUserDetails.builder()
                .idx(1L).email("user@test.com").name("테스터")
                .password("pw").role("ROLE_USER").enabled(true).build();
        userEntity = User.builder().idx(1L).email("user@test.com").name("테스터")
                .password("pw").role("ROLE_USER").enabled(true).build();
        course = Course.builder().idx(1L).name("테스트 코스").salePrice(10000).originalPrice(15000).build();

        unpaidOrders = Orders.builder().idx(1L).user(userEntity).paid(false)
                .refunded(false).paymentPrice(10000).build();
        paidOrders = Orders.builder().idx(2L).user(userEntity).paid(true)
                .refunded(false).paymentId("imp_test_001").paymentPrice(10000).build();
        refundedOrders = Orders.builder().idx(3L).user(userEntity).paid(true)
                .refunded(true).paymentId("imp_test_002").paymentPrice(10000).build();
    }

    // ============================
    // create
    // ============================

    @Test
    @DisplayName("주문 생성 성공")
    void create_성공() {
        OrdersDto.OrdersReq dto = OrdersDto.OrdersReq.builder()
                .paymentPrice(10000).courseIdxList(List.of(1L)).build();
        when(courseRepository.findAllById(List.of(1L))).thenReturn(List.of(course));
        when(ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(
                any(User.class), eq(course))).thenReturn(false);
        when(ordersRepository.save(any(Orders.class))).thenReturn(unpaidOrders);

        OrdersDto.OrdersRes result = ordersService.create(userDetails, dto);

        assertNotNull(result);
        assertFalse(result.getPaid());
        verify(ordersRepository, atLeastOnce()).save(any(Orders.class));
    }

    @Test
    @DisplayName("courseIdxList가 null이면 예외")
    void create_courseIdxList_null_예외() {
        OrdersDto.OrdersReq dto = OrdersDto.OrdersReq.builder()
                .paymentPrice(10000).courseIdxList(null).build();

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.create(userDetails, dto));
        assertEquals(REQUEST_ERROR, ex.getStatus());
    }

    @Test
    @DisplayName("courseIdxList가 비어있으면 예외")
    void create_courseIdxList_비어있음_예외() {
        OrdersDto.OrdersReq dto = OrdersDto.OrdersReq.builder()
                .paymentPrice(10000).courseIdxList(List.of()).build();

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.create(userDetails, dto));
        assertEquals(REQUEST_ERROR, ex.getStatus());
    }

    @Test
    @DisplayName("중복 courseIdx 포함 시 예외")
    void create_중복코스_예외() {
        OrdersDto.OrdersReq dto = OrdersDto.OrdersReq.builder()
                .paymentPrice(20000).courseIdxList(List.of(1L, 1L)).build();

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.create(userDetails, dto));
        assertEquals(ORDERS_VALIDATION_FAIL, ex.getStatus());
    }

    @Test
    @DisplayName("이미 구매한 코스 포함 시 예외")
    void create_이미구매한코스_예외() {
        OrdersDto.OrdersReq dto = OrdersDto.OrdersReq.builder()
                .paymentPrice(10000).courseIdxList(List.of(1L)).build();
        when(courseRepository.findAllById(List.of(1L))).thenReturn(List.of(course));
        when(ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(
                any(User.class), eq(course))).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.create(userDetails, dto));
        assertEquals(ORDERS_VALIDATION_FAIL, ex.getStatus());
    }

    @Test
    @DisplayName("가격 불일치 시 예외")
    void create_가격불일치_예외() {
        OrdersDto.OrdersReq dto = OrdersDto.OrdersReq.builder()
                .paymentPrice(99999).courseIdxList(List.of(1L)).build();
        when(courseRepository.findAllById(List.of(1L))).thenReturn(List.of(course));
        when(ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(
                any(User.class), eq(course))).thenReturn(false);

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.create(userDetails, dto));
        assertEquals(ORDERS_VALIDATION_FAIL, ex.getStatus());
    }

    // ============================
    // verify (결제 검증 멱등성)
    // ============================

    private PaidPayment paidPayment(long total, Long ordersIdx) {
        PaidPayment payment = mock(PaidPayment.class);
        when(payment.getCustomData()).thenReturn("{\"ordersIdx\":" + ordersIdx + "}");
        PaymentAmount amount = mock(PaymentAmount.class);
        when(amount.getTotal()).thenReturn(total);
        when(payment.getAmount()).thenReturn(amount);
        return payment;
    }

    private Orders unpaidWithItem() {
        Orders orders = Orders.builder().idx(1L).user(userEntity).paid(false)
                .refunded(false).paymentPrice(10000).build();
        orders.getItems().add(OrdersItem.builder().idx(1L).orders(orders).course(course).build());
        return orders;
    }

    @Test
    @DisplayName("결제 검증 성공 - 미결제 주문이 1회 원자적으로 확정된다")
    void verify_성공_1회확정() {
        OrdersDto.VerifyReq dto = OrdersDto.VerifyReq.builder().paymentId("imp_001").build();
        PaidPayment payment = paidPayment(10000L, 1L);
        when(portone.getPayment("imp_001"))
                .thenReturn(CompletableFuture.completedFuture(payment));
        when(ordersRepository.findUnpaidWithItemsForVerify(eq(1L), any(User.class)))
                .thenReturn(Optional.of(unpaidWithItem()));
        when(ordersRepository.markPaidIfUnpaid(1L, "imp_001")).thenReturn(1);

        OrdersDto.VerifyRes result = ordersService.verify(userDetails, dto);

        assertNotNull(result);
        assertTrue(result.getOrders().getPaid());
        verify(ordersRepository).markPaidIfUnpaid(1L, "imp_001");
    }

    @Test
    @DisplayName("결제 검증 멱등성 - 동시/중복 확정 시 두 번째는 ORDERS_ALREADY_PAID")
    void verify_중복확정_멱등성_예외() {
        OrdersDto.VerifyReq dto = OrdersDto.VerifyReq.builder().paymentId("imp_001").build();
        PaidPayment payment = paidPayment(10000L, 1L);
        when(portone.getPayment("imp_001"))
                .thenReturn(CompletableFuture.completedFuture(payment));
        when(ordersRepository.findUnpaidWithItemsForVerify(eq(1L), any(User.class)))
                .thenReturn(Optional.of(unpaidWithItem()));
        // 다른 트랜잭션이 먼저 확정 → 조건부 UPDATE 영향 행 0
        when(ordersRepository.markPaidIfUnpaid(1L, "imp_001")).thenReturn(0);

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.verify(userDetails, dto));
        assertEquals(ORDERS_ALREADY_PAID, ex.getStatus());
    }

    @Test
    @DisplayName("결제 검증 - 결제 금액 불일치 시 예외")
    void verify_금액불일치_예외() {
        OrdersDto.VerifyReq dto = OrdersDto.VerifyReq.builder().paymentId("imp_001").build();
        PaidPayment payment = paidPayment(99999L, 1L);
        when(portone.getPayment("imp_001"))
                .thenReturn(CompletableFuture.completedFuture(payment));
        when(ordersRepository.findUnpaidWithItemsForVerify(eq(1L), any(User.class)))
                .thenReturn(Optional.of(unpaidWithItem()));

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.verify(userDetails, dto));
        assertEquals(ORDERS_VALIDATION_FAIL, ex.getStatus());
        verify(ordersRepository, never()).markPaidIfUnpaid(anyLong(), anyString());
    }

    // ============================
    // freeComplete (무료 주문 완료)
    // ============================

    private Course freeCourse = Course.builder().idx(9L).name("무료 코스").salePrice(0).originalPrice(0).build();

    private Orders unpaidFreeWithItem() {
        Orders orders = Orders.builder().idx(1L).user(userEntity).paid(false)
                .refunded(false).paymentPrice(0).build();
        orders.getItems().add(OrdersItem.builder().idx(1L).orders(orders).course(freeCourse).build());
        return orders;
    }

    @Test
    @DisplayName("무료 주문 완료 성공 - 0원 주문이 1회 원자적으로 확정된다")
    void freeComplete_성공() {
        when(ordersRepository.findUnpaidWithItemsForFreeComplete(eq(1L), any(User.class)))
                .thenReturn(Optional.of(unpaidFreeWithItem()));
        when(ordersRepository.markPaidIfUnpaid(1L, "free_1")).thenReturn(1);

        OrdersDto.VerifyRes result = ordersService.freeComplete(userDetails, 1L);

        assertNotNull(result);
        assertTrue(result.getOrders().getPaid());
        verify(ordersRepository).markPaidIfUnpaid(1L, "free_1");
    }

    @Test
    @DisplayName("무료 주문 완료 - 코스 합계가 0이 아니면 예외(유료 우회 차단)")
    void freeComplete_유료코스_예외() {
        Orders paidPriceOrder = Orders.builder().idx(1L).user(userEntity).paid(false)
                .refunded(false).paymentPrice(0).build();
        paidPriceOrder.getItems().add(OrdersItem.builder().idx(1L).orders(paidPriceOrder).course(course).build());
        when(ordersRepository.findUnpaidWithItemsForFreeComplete(eq(1L), any(User.class)))
                .thenReturn(Optional.of(paidPriceOrder));

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.freeComplete(userDetails, 1L));
        assertEquals(ORDERS_VALIDATION_FAIL, ex.getStatus());
        verify(ordersRepository, never()).markPaidIfUnpaid(anyLong(), anyString());
    }

    @Test
    @DisplayName("무료 주문 완료 - 미결제 주문이 없으면 예외")
    void freeComplete_주문없음_예외() {
        when(ordersRepository.findUnpaidWithItemsForFreeComplete(eq(1L), any(User.class)))
                .thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.freeComplete(userDetails, 1L));
        assertEquals(ORDERS_NOT_ORDERED, ex.getStatus());
    }

    @Test
    @DisplayName("무료 주문 완료 멱등성 - 동시/중복 확정 시 두 번째는 ORDERS_ALREADY_PAID")
    void freeComplete_중복확정_멱등성_예외() {
        when(ordersRepository.findUnpaidWithItemsForFreeComplete(eq(1L), any(User.class)))
                .thenReturn(Optional.of(unpaidFreeWithItem()));
        when(ordersRepository.markPaidIfUnpaid(1L, "free_1")).thenReturn(0);

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.freeComplete(userDetails, 1L));
        assertEquals(ORDERS_ALREADY_PAID, ex.getStatus());
    }

    // ============================
    // cancel
    // ============================

    @Test
    @DisplayName("미결제 주문 취소 성공")
    void cancel_미결제주문_취소_성공() {
        when(ordersRepository.findByIdxAndUserAndPaidFalse(eq(1L), any(User.class)))
                .thenReturn(Optional.of(unpaidOrders));

        OrdersDto.OrdersRes result = ordersService.cancel(userDetails, 1L);

        assertNotNull(result);
        verify(ordersRepository).delete(unpaidOrders);
    }

    @Test
    @DisplayName("결제된 주문 취소 시 예외")
    void cancel_결제된주문_예외() {
        when(ordersRepository.findByIdxAndUserAndPaidFalse(eq(2L), any(User.class)))
                .thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.cancel(userDetails, 2L));
        assertEquals(ORDERS_NOT_ORDERED, ex.getStatus());
    }

    // ============================
    // refund
    // ============================

    @Test
    @DisplayName("환불 성공")
    void refund_성공() {
        OrdersDto.RefundReq dto = OrdersDto.RefundReq.builder().reason("환불 테스트").build();
        when(ordersRepository.findByIdxAndUser(eq(2L), any(User.class)))
                .thenReturn(Optional.of(paidOrders));
        CancelPaymentResponse cancelResp = mock(CancelPaymentResponse.class);
        when(portone.cancelPayment(anyString(), isNull(), isNull(), isNull(),
                anyString(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(CompletableFuture.completedFuture(cancelResp));
        when(ordersRepository.save(any(Orders.class))).thenReturn(paidOrders);

        OrdersDto.OrdersRes result = ordersService.refund(userDetails, 2L, dto);

        assertNotNull(result);
        verify(portone).cancelPayment(anyString(), isNull(), isNull(), isNull(),
                anyString(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("미결제 주문 환불 시 예외")
    void refund_미결제주문_예외() {
        OrdersDto.RefundReq dto = OrdersDto.RefundReq.builder().reason("환불").build();
        when(ordersRepository.findByIdxAndUser(eq(1L), any(User.class)))
                .thenReturn(Optional.of(unpaidOrders));

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.refund(userDetails, 1L, dto));
        assertEquals(ORDERS_NOT_PAID, ex.getStatus());
    }

    @Test
    @DisplayName("이미 환불된 주문 재환불 시 예외")
    void refund_이미환불된주문_예외() {
        OrdersDto.RefundReq dto = OrdersDto.RefundReq.builder().reason("재환불").build();
        when(ordersRepository.findByIdxAndUser(eq(3L), any(User.class)))
                .thenReturn(Optional.of(refundedOrders));

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.refund(userDetails, 3L, dto));
        assertEquals(ORDERS_ALREADY_REFUNDED, ex.getStatus());
    }

    // ============================
    // check
    // ============================

    @Test
    @DisplayName("코스 구매 확인 성공")
    void check_구매확인_성공() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(
                any(User.class), eq(course))).thenReturn(true);

        boolean result = ordersService.check(userDetails, 1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("미구매 코스 확인 시 예외")
    void check_미구매_예외() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(
                any(User.class), eq(course))).thenReturn(false);

        BaseException ex = assertThrows(BaseException.class,
                () -> ordersService.check(userDetails, 1L));
        assertEquals(ORDERS_NOT_ORDERED, ex.getStatus());
    }
}
