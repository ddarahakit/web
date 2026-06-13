package com.ddarahakit.backend.domain.community;

import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.community.model.Post;
import com.ddarahakit.backend.domain.community.model.PostType;
import com.ddarahakit.backend.domain.image.FileUploadService;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommunityControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @MockBean FileUploadService fileUploadService;

    AuthUserDetails userAuth;
    AuthUserDetails adminAuth;
    User savedUser;
    User savedAdmin;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .email("user@test.com").name("테스터")
                .password(passwordEncoder.encode("password"))
                .role("ROLE_USER").enabled(true).build());
        savedAdmin = userRepository.save(User.builder()
                .email("admin@test.com").name("관리자")
                .password(passwordEncoder.encode("password"))
                .role("ROLE_ADMIN").enabled(true).build());

        userAuth = AuthUserDetails.builder()
                .idx(savedUser.getIdx()).email(savedUser.getEmail())
                .name(savedUser.getName()).password(savedUser.getPassword())
                .role("ROLE_USER").enabled(true).build();
        adminAuth = AuthUserDetails.builder()
                .idx(savedAdmin.getIdx()).email(savedAdmin.getEmail())
                .name(savedAdmin.getName()).password(savedAdmin.getPassword())
                .role("ROLE_ADMIN").enabled(true).build();
    }

    @Test
    @DisplayName("GET /community/list - 비인증으로 게시글 목록 조회 성공")
    void getPostList_비인증_성공() throws Exception {
        mockMvc.perform(get("/community/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /community/post - 게시글 작성 성공 (인증)")
    void createPost_성공() throws Exception {
        Map<String, Object> body = Map.of(
                "postType", "FREE",
                "title", "테스트 게시글",
                "text", "요약입니다",
                "content", "본문 내용입니다"
        );

        mockMvc.perform(post("/community/post")
                        .with(user(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.title").value("테스트 게시글"));
    }

    @Test
    @DisplayName("POST /community/post - 비인증 시 401")
    void createPost_비인증_401() throws Exception {
        Map<String, Object> body = Map.of(
                "postType", "FREE", "title", "제목", "text", "요약", "content", "내용"
        );

        mockMvc.perform(post("/community/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /community/{postIdx} - 게시글 상세 조회 성공")
    void getPostDetail_성공() throws Exception {
        Post post = postRepository.save(Post.builder()
                .postType(PostType.FREE).title("테스트").text("요약").content("내용")
                .user(savedUser).build());

        mockMvc.perform(get("/community/" + post.getIdx()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /community/post/{postIdx} - 게시글 수정 성공")
    void updatePost_성공() throws Exception {
        Post post = postRepository.save(Post.builder()
                .postType(PostType.FREE).title("원본").text("요약").content("내용")
                .user(savedUser).build());

        Map<String, Object> body = Map.of(
                "postType", "FREE", "title", "수정된 제목", "text", "수정 요약", "content", "수정 내용"
        );

        mockMvc.perform(put("/community/post/" + post.getIdx())
                        .with(user(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.title").value("수정된 제목"));
    }

    @Test
    @DisplayName("DELETE /community/post/{postIdx} - 게시글 삭제 성공")
    void deletePost_성공() throws Exception {
        Post post = postRepository.save(Post.builder()
                .postType(PostType.FREE).title("삭제 대상").text("요약").content("내용")
                .user(savedUser).build());

        mockMvc.perform(delete("/community/post/" + post.getIdx())
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /community/comment - 댓글 작성 성공")
    void createComment_성공() throws Exception {
        Post post = postRepository.save(Post.builder()
                .postType(PostType.FREE).title("게시글").text("요약").content("내용")
                .user(savedUser).build());

        Map<String, Object> body = Map.of(
                "postIdx", post.getIdx(),
                "text", "댓글 요약",
                "content", "댓글 내용"
        );

        mockMvc.perform(post("/community/comment")
                        .with(user(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /community/scrap/{postIdx} - 스크랩 성공")
    void scrap_성공() throws Exception {
        Post post = postRepository.save(Post.builder()
                .postType(PostType.FREE).title("스크랩할 글").text("요약").content("내용")
                .user(savedUser).build());

        mockMvc.perform(post("/community/scrap/" + post.getIdx())
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /community/scrap/{postIdx}/toggle - 스크랩 토글 성공")
    void toggleScrap_성공() throws Exception {
        Post post = postRepository.save(Post.builder()
                .postType(PostType.FREE).title("토글할 글").text("요약").content("내용")
                .user(savedUser).build());

        mockMvc.perform(post("/community/scrap/" + post.getIdx() + "/toggle")
                        .with(user(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.scrapped").exists());
    }
}
