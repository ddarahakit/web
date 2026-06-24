package com.ddarahakit.backend.domain.cart;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.cart.model.Cart;
import com.ddarahakit.backend.domain.cart.model.CartDto;
import com.ddarahakit.backend.domain.cart.model.CartItem;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.orders.OrdersItemRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CourseRepository courseRepository;
    private final OrdersItemRepository ordersItemRepository;

    private Cart getOrCreateCart(AuthUserDetails authUserDetails) {
        return cartRepository.findByUser(authUserDetails.toEntity())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(authUserDetails.toEntity()).build()
                ));
    }

    public CartDto.CartCountRes count(AuthUserDetails authUserDetails) {
        Cart cart = getOrCreateCart(authUserDetails);
        int count = cartItemRepository.countByCart(cart);
        return CartDto.CartCountRes.of(count);
    }

    public CartDto.CartRes list(AuthUserDetails authUserDetails) {
        Cart cart = getOrCreateCart(authUserDetails);
        return CartDto.CartRes.of(cart.getCartItems());
    }

    @Transactional
    public CartDto.CartItemRes add(AuthUserDetails authUserDetails, CartDto.CartItemReq dto) {
        Course course = courseRepository.findById(dto.getCourseIdx()).orElseThrow(
                () -> BaseException.of(COURSE_NOT_FOUND)
        );

        Cart cart = getOrCreateCart(authUserDetails);

        boolean alreadyPurchased = ordersItemRepository
                .existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(authUserDetails.toEntity(), course);
        if (alreadyPurchased) {
            throw BaseException.of(CART_ALREADY_PURCHASED);
        }

        if (cartItemRepository.existsByCartAndCourse(cart, course)) {
            throw BaseException.of(CART_ALREADY_EXISTS);
        }

        CartItem cartItem = cartItemRepository.save(dto.toEntity(cart, course));

        return CartDto.CartItemRes.of(cartItem);
    }

    @Transactional
    public CartDto.CartItemRes remove(AuthUserDetails authUserDetails, Long cartItemIdx) {
        Cart cart = getOrCreateCart(authUserDetails);

        CartItem cartItem = cartItemRepository.findByIdxAndCart(cartItemIdx, cart).orElseThrow(
                () -> BaseException.of(CART_NOT_FOUND)
        );

        cartItemRepository.delete(cartItem);
        return CartDto.CartItemRes.of(cartItem);
    }

    @Transactional
    public void clear(AuthUserDetails authUserDetails) {
        Cart cart = getOrCreateCart(authUserDetails);
        cart.getCartItems().clear();
    }
}
