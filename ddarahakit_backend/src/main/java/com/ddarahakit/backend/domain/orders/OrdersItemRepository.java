package com.ddarahakit.backend.domain.orders;

import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.orders.model.OrdersItem;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrdersItemRepository extends JpaRepository<OrdersItem, Long> {
    boolean existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(User user, Course course);

    List<OrdersItem> findByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalse(User user);

    // 코스별 주문 항목 수 집계 (popular 정렬용). 코스 컬렉션을 메모리에 적재하지 않고 DB 에서 COUNT.
    // 반환: [course.idx(Long), count(Long)]. 주문 0건 코스는 행이 없으므로 호출부에서 0 으로 처리.
    @Query("SELECT oi.course.idx, COUNT(oi) FROM OrdersItem oi GROUP BY oi.course.idx")
    List<Object[]> countGroupByCourse();
}
