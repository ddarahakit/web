package com.ddarahakit.backend.domain.cart;

import com.ddarahakit.backend.domain.cart.model.Cart;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
