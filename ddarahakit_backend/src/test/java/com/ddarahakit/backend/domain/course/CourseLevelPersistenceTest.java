package com.ddarahakit.backend.domain.course;

import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.model.CourseLevel;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CourseLevel 컨버터의 DB 왕복 + JPQL 필터 동작을 H2 로 검증.
 * 운영 500(DB 한글 level 값을 enum 으로 못 읽음) 재발 방지.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CourseLevelPersistenceTest {

    @Autowired CourseRepository courseRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("저장: enum → DB 컬럼은 한글(초급) 로 저장된다")
    void save_writesKoreanLabel() {
        Course saved = courseRepository.save(Course.builder()
                .name("컨버터-저장").salePrice(1000).originalPrice(2000)
                .level(CourseLevel.BEGINNER).build());
        em.flush();

        Object dbVal = em.createNativeQuery("SELECT level FROM course WHERE idx = :idx")
                .setParameter("idx", saved.getIdx())
                .getSingleResult();
        assertEquals("초급", dbVal);
    }

    @Test
    @DisplayName("조회: DB 한글값(초급)을 가진 course 를 읽어도 500 안 나고 enum 으로 매핑")
    void read_koreanLabel_mapsToEnum() {
        // 컬럼에 한글을 직접 박아(운영 데이터 재현) 컨버터 읽기 검증
        Course saved = courseRepository.save(Course.builder()
                .name("컨버터-조회").salePrice(1000).originalPrice(2000).build());
        em.flush();
        em.createNativeQuery("UPDATE course SET level = '초급' WHERE idx = :idx")
                .setParameter("idx", saved.getIdx()).executeUpdate();
        em.clear();

        Course reloaded = courseRepository.findById(saved.getIdx()).orElseThrow();
        assertEquals(CourseLevel.BEGINNER, reloaded.getLevel());   // No enum constant 예외 없이 매핑
        assertEquals("초급", reloaded.getLevel().getDescription());
    }

    @Test
    @DisplayName("필터: findForList(BEGINNER) 가 @Convert 적용된 JPQL 비교로 한글 컬럼과 매칭")
    void filter_byLevel_matchesKoreanColumn() {
        Course beginner = courseRepository.save(Course.builder()
                .name("필터-초급").salePrice(1000).originalPrice(2000).level(CourseLevel.BEGINNER).build());
        courseRepository.save(Course.builder()
                .name("필터-고급").salePrice(1000).originalPrice(2000).level(CourseLevel.ADVANCED).build());
        em.flush();
        em.clear();

        List<Course> result = courseRepository.findForList(CourseLevel.BEGINNER, false);
        assertTrue(result.stream().anyMatch(c -> c.getIdx().equals(beginner.getIdx())),
                "BEGINNER 필터에 초급 코스가 포함돼야 함");
        assertTrue(result.stream().allMatch(c -> c.getLevel() == CourseLevel.BEGINNER),
                "BEGINNER 필터 결과는 모두 초급이어야 함");
    }

    @Test
    @DisplayName("조회: level 이 NULL 인 기존 데이터도 정상(null 매핑)")
    void read_nullLevel_ok() {
        Course saved = courseRepository.save(Course.builder()
                .name("컨버터-널").salePrice(1000).originalPrice(2000).build());
        em.flush();
        em.clear();

        Course reloaded = courseRepository.findById(saved.getIdx()).orElseThrow();
        assertNull(reloaded.getLevel());
    }
}
