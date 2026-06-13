package com.ddarahakit.backend.domain.orders;

import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.orders.model.OrdersItem;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdersItemRepository extends JpaRepository<OrdersItem, Long> {
    boolean existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(User user, Course course);

    List<OrdersItem> findByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalse(User user);
}
