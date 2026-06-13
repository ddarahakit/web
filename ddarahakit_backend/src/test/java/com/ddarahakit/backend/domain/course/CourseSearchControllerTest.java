package com.ddarahakit.backend.domain.course;

import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.model.CourseLevel;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GET /course/search 포팅 검증 — /course/{idx} 와 충돌 없이 매칭, 한글 level(CourseLevelConverter) 통과.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CourseSearchControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        courseRepository.save(Course.builder()
                .name("스프링 부트 입문").text("백엔드 기초").description("자바 스프링")
                .salePrice(10000).originalPrice(20000).level(CourseLevel.BEGINNER)
                .totalReviewsCount(0).build());
        courseRepository.save(Course.builder()
                .name("리액트 실전").text("프론트엔드").description("자바스크립트 리액트")
                .salePrice(0).originalPrice(0).level(CourseLevel.INTERMEDIATE)
                .totalReviewsCount(0).build());
    }

    @Test
    @DisplayName("GET /course/search?keyword=스프링 → 매칭 코스 반환(200, /{idx} 충돌 없음)")
    void search_byKeyword() throws Exception {
        mockMvc.perform(get("/course/search").param("keyword", "스프링"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.courses[0].name").value("스프링 부트 입문"))
                .andExpect(jsonPath("$.results.courses[0].level").value("BEGINNER"))
                .andExpect(jsonPath("$.results.courses[0].levelDescription").value("초급"));
    }

    @Test
    @DisplayName("GET /course/search (keyword 없음) → 빈 목록 200")
    void search_noKeyword_empty() throws Exception {
        mockMvc.perform(get("/course/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.courses").isArray())
                .andExpect(jsonPath("$.results.courses").isEmpty());
    }

    @Test
    @DisplayName("GET /course/search?keyword=자바 → 설명 LIKE 매칭(2건)")
    void search_description() throws Exception {
        mockMvc.perform(get("/course/search").param("keyword", "자바"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.courses.length()").value(2));
    }
}
