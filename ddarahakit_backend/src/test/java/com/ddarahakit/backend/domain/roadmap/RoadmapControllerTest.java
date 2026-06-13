package com.ddarahakit.backend.domain.roadmap;

import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.roadmap.model.Roadmap;
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
class RoadmapControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoadmapRepository roadmapRepository;
    @Autowired PasswordEncoder passwordEncoder;

    AuthUserDetails userAuth;
    User savedUser;
    Roadmap savedRoadmap;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .email("user@test.com").name("테스터")
                .password(passwordEncoder.encode("pw"))
                .role("ROLE_USER").enabled(true).build());
        userAuth = AuthUserDetails.builder()
                .idx(savedUser.getIdx()).email(savedUser.getEmail())
                .name(savedUser.getName()).password(savedUser.getPassword())
                .role("ROLE_USER").enabled(true).build();
        savedRoadmap = roadmapRepository.save(Roadmap.builder()
                .name("테스트 로드맵").description("테스트 설명").build());
    }

    @Test
    @DisplayName("GET /roadmap/list - 비인증으로 목록 조회 성공")
    void list_비인증_성공() throws Exception {
        mockMvc.perform(get("/roadmap/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("GET /roadmap/{roadmapIdx} - 상세 조회 성공")
    void detail_성공() throws Exception {
        mockMvc.perform(get("/roadmap/" + savedRoadmap.getIdx()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.name").value("테스트 로드맵"));
    }

    @Test
    @DisplayName("POST /roadmap - 로드맵 생성 성공 (인증)")
    void create_성공() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "새로운 로드맵",
                "description", "로드맵 설명"
        );

        mockMvc.perform(post("/roadmap")
                        .with(user(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.name").value("새로운 로드맵"));
    }

    @Test
    @DisplayName("POST /roadmap - 비인증 시 401")
    void create_비인증_401() throws Exception {
        Map<String, Object> body = Map.of("name", "로드맵");

        mockMvc.perform(post("/roadmap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("PUT /roadmap/{roadmapIdx} - 로드맵 수정 성공")
    void update_성공() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "수정된 로드맵",
                "description", "수정 설명"
        );

        mockMvc.perform(put("/roadmap/" + savedRoadmap.getIdx())
                        .with(user(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.name").value("수정된 로드맵"));
    }

    @Test
    @DisplayName("DELETE /roadmap/{roadmapIdx} - 로드맵 삭제 성공")
    void delete_성공() throws Exception {
        mockMvc.perform(delete("/roadmap/" + savedRoadmap.getIdx())
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
