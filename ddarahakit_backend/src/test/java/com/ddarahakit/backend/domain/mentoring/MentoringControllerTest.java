package com.ddarahakit.backend.domain.mentoring;

import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.mentoring.model.MentoringSession;
import com.ddarahakit.backend.domain.mentoring.model.MentoringStatus;
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
class MentoringControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired MentoringSessionRepository sessionRepository;
    @Autowired PasswordEncoder passwordEncoder;

    AuthUserDetails menteeAuth;
    AuthUserDetails mentorAuth;
    User savedMentee;
    User savedMentor;
    MentoringSession savedSession;

    @BeforeEach
    void setUp() {
        savedMentee = userRepository.save(User.builder()
                .email("mentee@test.com").name("멘티")
                .password(passwordEncoder.encode("pw"))
                .role("ROLE_USER").enabled(true).build());
        savedMentor = userRepository.save(User.builder()
                .email("mentor@test.com").name("멘토")
                .password(passwordEncoder.encode("pw"))
                .role("ROLE_MENTOR").enabled(true).build());

        menteeAuth = AuthUserDetails.builder()
                .idx(savedMentee.getIdx()).email(savedMentee.getEmail())
                .name(savedMentee.getName()).password(savedMentee.getPassword())
                .role("ROLE_USER").enabled(true).build();
        mentorAuth = AuthUserDetails.builder()
                .idx(savedMentor.getIdx()).email(savedMentor.getEmail())
                .name(savedMentor.getName()).password(savedMentor.getPassword())
                .role("ROLE_MENTOR").enabled(true).build();

        savedSession = sessionRepository.save(MentoringSession.builder()
                .subject("테스트 멘토링").status(MentoringStatus.OPEN)
                .mentor(savedMentor).mentee(savedMentee).build());
    }

    @Test
    @DisplayName("GET /mentoring - 세션 목록 조회 성공")
    void list_성공() throws Exception {
        mockMvc.perform(get("/mentoring")
                        .with(user(menteeAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /mentoring - 비인증 시 401")
    void list_비인증_401() throws Exception {
        mockMvc.perform(get("/mentoring"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /mentoring - 세션 생성 성공")
    void createSession_성공() throws Exception {
        Map<String, Object> body = Map.of(
                "mentorIdx", savedMentor.getIdx(),
                "subject", "새로운 멘토링"
        );

        mockMvc.perform(post("/mentoring")
                        .with(user(menteeAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.sessionIdx").exists());
    }

    @Test
    @DisplayName("GET /mentoring/{sessionIdx} - 세션 상세 조회 성공")
    void detail_성공() throws Exception {
        mockMvc.perform(get("/mentoring/" + savedSession.getIdx())
                        .with(user(menteeAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.session.subject").value("테스트 멘토링"));
    }

    @Test
    @DisplayName("GET /mentoring/{sessionIdx}/messages - 메시지 목록 조회 성공")
    void messages_성공() throws Exception {
        mockMvc.perform(get("/mentoring/" + savedSession.getIdx() + "/messages")
                        .with(user(menteeAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /mentoring/{sessionIdx}/messages - 메시지 전송 성공")
    void sendMessage_성공() throws Exception {
        Map<String, Object> body = Map.of("message", "안녕하세요!");

        mockMvc.perform(post("/mentoring/" + savedSession.getIdx() + "/messages")
                        .with(user(menteeAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.message").value("안녕하세요!"));
    }

    @Test
    @DisplayName("PATCH /mentoring/{sessionIdx}/read - 읽음 처리 성공")
    void markAsRead_성공() throws Exception {
        mockMvc.perform(patch("/mentoring/" + savedSession.getIdx() + "/read")
                        .with(user(menteeAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /mentoring/{sessionIdx} - 세션 종료 성공")
    void close_성공() throws Exception {
        mockMvc.perform(delete("/mentoring/" + savedSession.getIdx())
                        .with(user(menteeAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
