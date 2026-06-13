package com.ddarahakit.backend.domain.orders;

import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.orders.model.Orders;
import com.ddarahakit.backend.domain.orders.model.OrdersDto;
import com.ddarahakit.backend.domain.orders.model.OrdersItem;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 무료(0원) 주문 완료 후 대시보드 수강목록/구매판정 회귀 테스트.
 *
 * 회귀 배경: 주문 생성 시 refunded 가 NULL 로 INSERT 되면
 * 수강목록(findByOrders...OrdersRefundedFalse) / 구매판정(existsBy...OrdersRefundedFalse)의
 * 'refunded = false' 조건(SQL 에서 NULL != false)에 걸려 무료 주문이 제외된다.
 * OrdersReq.toEntity 가 refunded=false 로 초기화하면 유료/무료 동일하게 노출되어야 한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class FreeOrderEnrollmentTest {

    @Autowired UserRepository userRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired OrdersRepository ordersRepository;
    @Autowired OrdersItemRepository ordersItemRepository;

    @Test
    @DisplayName("무료(0원) 주문 완료 후 수강목록·구매판정에 동일하게 노출된다")
    void freeOrder_appears_in_ordered_list_and_check() {
        User user = userRepository.save(User.builder()
                .email("free@test.com").name("free").password("pw")
                .role("ROLE_USER").enabled(true).build());
        Course free = courseRepository.save(Course.builder()
                .name("무료 코스").salePrice(0).originalPrice(0).build());

        // 1) 주문 생성: 실제 OrdersService.create 와 동일하게 OrdersReq.toEntity 경로 사용
        Orders orders = OrdersDto.OrdersReq.builder()
                .paymentPrice(0).courseIdxList(List.of(free.getIdx())).build()
                .toEntity(user);
        orders.getItems().add(OrdersItem.builder().orders(orders).course(free).build());
        Orders saved = ordersRepository.saveAndFlush(orders);

        // 생성 직후 refunded 는 NULL 이 아니라 false 여야 한다(회귀 핵심).
        assertEquals(Boolean.FALSE, saved.getRefunded(), "생성 시 refunded 는 false 로 초기화되어야 함");

        // 2) 무료 완료: freeComplete 와 동일한 멱등 확정(paid=true)
        int updated = ordersRepository.markPaidIfUnpaid(saved.getIdx(), "free_" + saved.getIdx());
        assertEquals(1, updated);

        // 3) 대시보드 수강목록(getOrderedCourseList 의 데이터 소스)
        List<OrdersItem> ordered = ordersItemRepository
                .findByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalse(user);
        assertEquals(1, ordered.size(), "무료 주문이 대시보드 수강목록에 노출되어야 함");
        assertEquals(free.getIdx(), ordered.get(0).getCourse().getIdx());

        // 4) 구매판정(check) 도 동일 기준으로 true
        assertTrue(
                ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(user, free),
                "무료 주문도 구매판정(check)에서 true 여야 함");
    }
}
