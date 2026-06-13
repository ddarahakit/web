package com.ddarahakit.backend.domain.cart;

import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.cart.model.Cart;
import com.ddarahakit.backend.domain.cart.model.CartItem;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired CartRepository cartRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired PasswordEncoder passwordEncoder;

    AuthUserDetails userAuth;
    User savedUser;
    Course savedCourse;
    Cart savedCart;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .email("user@test.com").name("테스터")
                .password(passwordEncoder.encode("pw"))
                .role("ROLE_USER").enabled(true).build());
        savedCourse = courseRepository.save(Course.builder()
                .name("테스트 코스").salePrice(10000).originalPrice(15000).isDisplay(true).build());
        savedCart = cartRepository.save(Cart.builder().user(savedUser).build());

        userAuth = AuthUserDetails.builder()
                .idx(savedUser.getIdx()).email(savedUser.getEmail())
                .name(savedUser.getName()).password(savedUser.getPassword())
                .role("ROLE_USER").enabled(true).build();
    }

    @Test
    @DisplayName("GET /cart - 장바구니 목록 조회 성공")
    void list_성공() throws Exception {
        mockMvc.perform(get("/cart")
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.cartItems").isArray());
    }

    @Test
    @DisplayName("GET /cart - 비인증 시 401")
    void list_비인증_401() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /cart/count - 장바구니 항목 수 조회 성공")
    void count_성공() throws Exception {
        mockMvc.perform(get("/cart/count")
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.count").exists());
    }

    @Test
    @DisplayName("GET /cart/count - 비인증 시 NPE(500) 아니라 인증 차단(non-2xx)")
    void count_비인증_차단() throws Exception {
        // 미인증이면 authUserDetails 가 null 이라 핸들러 진입 시 NPE 500 이 나던 버그.
        // SecurityConfig 가드로 핸들러 도달 전 차단되어 2xx 가 아니어야 한다(NPE 500 아님).
        mockMvc.perform(get("/cart/count"))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(
                            sc != 200 && sc != 500,
                            "expected auth block (non-2xx, not 500 NPE) but was " + sc);
                });
    }

    @Test
    @DisplayName("POST /cart - 장바구니 항목 추가 성공")
    void add_성공() throws Exception {
        Map<String, Object> body = Map.of("courseIdx", savedCourse.getIdx());

        mockMvc.perform(post("/cart")
                        .with(user(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.cartItemIdx").exists());
    }

    @Test
    @DisplayName("DELETE /cart/{cartItemIdx} - 장바구니 항목 삭제 성공")
    void remove_성공() throws Exception {
        CartItem cartItem = cartItemRepository.save(
                CartItem.builder().cart(savedCart).course(savedCourse).build());

        mockMvc.perform(delete("/cart/" + cartItem.getIdx())
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /cart - 장바구니 전체 비우기 성공")
    void clear_성공() throws Exception {
        cartItemRepository.save(CartItem.builder().cart(savedCart).course(savedCourse).build());

        mockMvc.perform(delete("/cart")
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
