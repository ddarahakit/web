package com.ddarahakit.backend.domain.cart;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.cart.model.Cart;
import com.ddarahakit.backend.domain.cart.model.CartDto;
import com.ddarahakit.backend.domain.cart.model.CartItem;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.orders.OrdersItemRepository;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock CourseRepository courseRepository;
    @Mock OrdersItemRepository ordersItemRepository;
    @InjectMocks CartService cartService;

    AuthUserDetails userDetails;
    User userEntity;
    Course course;
    Cart cart;
    CartItem cartItem;

    @BeforeEach
    void setUp() {
        userDetails = AuthUserDetails.builder()
                .idx(1L).email("user@test.com").name("테스터")
                .password("pw").role("ROLE_USER").enabled(true).build();
        userEntity = User.builder().idx(1L).email("user@test.com").name("테스터")
                .password("pw").role("ROLE_USER").enabled(true).build();
        course = Course.builder().idx(1L).name("테스트 코스")
                .salePrice(10000).originalPrice(15000).build();
        cart = Cart.builder().idx(1L).user(userEntity).build();
        cartItem = CartItem.builder().idx(1L).cart(cart).course(course).build();
    }

    @Test
    @DisplayName("장바구니 항목 추가 성공")
    void add_성공() {
        CartDto.CartItemReq dto = CartDto.CartItemReq.builder().courseIdx(1L).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));
        when(ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(
                any(User.class), eq(course))).thenReturn(false);
        when(cartItemRepository.existsByCartAndCourse(eq(cart), eq(course))).thenReturn(false);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartDto.CartItemRes result = cartService.add(userDetails, dto);

        assertNotNull(result);
        assertEquals(1L, result.getCourseIdx());
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("이미 구매한 코스 장바구니 담기 시 예외")
    void add_이미구매한코스_예외() {
        CartDto.CartItemReq dto = CartDto.CartItemReq.builder().courseIdx(1L).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));
        when(ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(
                any(User.class), eq(course))).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class,
                () -> cartService.add(userDetails, dto));
        assertEquals(CART_ALREADY_PURCHASED, ex.getStatus());
    }

    @Test
    @DisplayName("이미 장바구니에 있는 코스 담기 시 예외")
    void add_이미장바구니에있음_예외() {
        CartDto.CartItemReq dto = CartDto.CartItemReq.builder().courseIdx(1L).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));
        when(ordersItemRepository.existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(
                any(User.class), eq(course))).thenReturn(false);
        when(cartItemRepository.existsByCartAndCourse(eq(cart), eq(course))).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class,
                () -> cartService.add(userDetails, dto));
        assertEquals(CART_ALREADY_EXISTS, ex.getStatus());
    }

    @Test
    @DisplayName("장바구니 항목 삭제 성공")
    void remove_성공() {
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdxAndCart(eq(1L), eq(cart))).thenReturn(Optional.of(cartItem));

        CartDto.CartItemRes result = cartService.remove(userDetails, 1L);

        assertNotNull(result);
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("없는 장바구니 항목 삭제 시 예외")
    void remove_없는항목_예외() {
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdxAndCart(eq(99L), eq(cart))).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> cartService.remove(userDetails, 99L));
        assertEquals(CART_NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("장바구니 항목 수 조회 성공")
    void count_성공() {
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.countByCart(cart)).thenReturn(3);

        CartDto.CartCountRes result = cartService.count(userDetails);

        assertEquals(3, result.getCount());
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void list_성공() {
        Cart cartWithItems = Cart.builder().idx(1L).user(userEntity).build();
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cartWithItems));

        CartDto.CartRes result = cartService.list(userDetails);

        assertNotNull(result);
    }
}
