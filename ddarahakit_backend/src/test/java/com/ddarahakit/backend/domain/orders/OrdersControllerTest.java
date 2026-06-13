package com.ddarahakit.backend.domain.orders;

import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.orders.model.Orders;
import com.ddarahakit.backend.domain.orders.model.OrdersItem;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.portone.sdk.server.payment.CancelPaymentResponse;
import io.portone.sdk.server.payment.PaymentClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrdersControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired OrdersRepository ordersRepository;
    @Autowired OrdersItemRepository ordersItemRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @MockBean PaymentClient portone;

    AuthUserDetails userAuth;
    User savedUser;
    Course savedCourse;
    Orders savedPaidOrders;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .email("user@test.com").name("테스터")
                .password(passwordEncoder.encode("pw"))
                .role("ROLE_USER").enabled(true).build());
        savedCourse = courseRepository.save(Course.builder()
                .name("테스트 코스").salePrice(10000).originalPrice(15000).isDisplay(true).build());

        userAuth = AuthUserDetails.builder()
                .idx(savedUser.getIdx()).email(savedUser.getEmail())
                .name(savedUser.getName()).password(savedUser.getPassword())
                .role("ROLE_USER").enabled(true).build();

        // 환불 테스트용 결제 완료 주문
        Orders orders = ordersRepository.save(Orders.builder()
                .user(savedUser).paid(true).refunded(false)
                .paymentId("imp_test_payment_001").paymentPrice(10000).build());
        ordersItemRepository.save(OrdersItem.builder().orders(orders).course(savedCourse).build());
        savedPaidOrders = orders;
    }

    @Test
    @DisplayName("POST /orders/create - 주문 생성 성공")
    void create_성공() throws Exception {
        // savedCourse is already purchased in setUp(); use a fresh course
        Course newCourse = courseRepository.save(Course.builder()
                .name("새 코스").salePrice(5000).originalPrice(8000).isDisplay(true).build());

        Map<String, Object> body = Map.of(
                "paymentPrice", 5000,
                "courseIdxList", List.of(newCourse.getIdx())
        );

        mockMvc.perform(post("/orders/create")
                        .with(user(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.ordersIdx").exists());
    }

    @Test
    @DisplayName("POST /orders/create - 비인증 시 401")
    void create_비인증_401() throws Exception {
        Map<String, Object> body = Map.of(
                "paymentPrice", 10000,
                "courseIdxList", List.of(savedCourse.getIdx())
        );

        mockMvc.perform(post("/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("DELETE /orders/{ordersIdx} - 주문 취소 성공")
    void cancel_성공() throws Exception {
        Orders unpaidOrders = ordersRepository.save(Orders.builder()
                .user(savedUser).paid(false).refunded(false).paymentPrice(10000).build());

        mockMvc.perform(delete("/orders/" + unpaidOrders.getIdx())
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /orders/{ordersIdx}/refund - 환불 성공")
    void refund_성공() throws Exception {
        CancelPaymentResponse cancelResp = mock(CancelPaymentResponse.class);
        when(portone.cancelPayment(anyString(), isNull(), isNull(), isNull(),
                anyString(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(CompletableFuture.completedFuture(cancelResp));

        Map<String, Object> body = Map.of("reason", "테스트 환불");

        mockMvc.perform(post("/orders/" + savedPaidOrders.getIdx() + "/refund")
                        .with(user(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.refunded").value(true));
    }
}
