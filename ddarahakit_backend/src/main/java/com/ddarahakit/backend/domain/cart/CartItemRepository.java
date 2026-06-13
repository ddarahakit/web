package com.ddarahakit.backend.domain.cart;

import com.ddarahakit.backend.domain.cart.model.Cart;
import com.ddarahakit.backend.domain.cart.model.CartItem;
import com.ddarahakit.backend.domain.course.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByCartAndCourse(Cart cart, Course course);

    Optional<CartItem> findByIdxAndCart(Long idx, Cart cart);

    int countByCart(Cart cart);
}
