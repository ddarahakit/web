package com.ddarahakit.backend.domain.course.repository;


import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.model.CourseLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c WHERE c.category.idx IN :categoryIdxList ORDER BY c.idx")
    List<Course> findCoursesBycategoryIdxList(@Param("categoryIdxList") List<Long> categoryIdxList);

    @Query("SELECT c.category.idx, COUNT(c) FROM Course c GROUP BY c.category.idx")
    List<Object[]> countByCategoryGrouped();

    /**
     * 필터/난이도 적용 코스 목록(정렬은 서비스에서 sortKey 로 처리).
     * - level: null 이면 전체, 아니면 해당 난이도.
     * - freeOnly: true 면 무료(salePrice=0)만.
     * category 는 fetch join 으로 N+1 방지(섹션/주문수는 @BatchSize 로 완화).
     */
    @Query("""
            SELECT c FROM Course c
            LEFT JOIN FETCH c.category
            WHERE (:level IS NULL OR c.level = :level)
              AND (:freeOnly = false OR c.salePrice = 0)
            """)
    List<Course> findForList(@Param("level") CourseLevel level,
                             @Param("freeOnly") boolean freeOnly);

    /**
     * 키워드 검색: 강의명(name)/한줄소개(text)/설명(description) 에 키워드 포함(대소문자 무시).
     * category 는 fetch join 으로 N+1 방지(섹션/주문수는 @BatchSize 로 완화). 최신순(idx DESC).
     */
    @Query("""
            SELECT c FROM Course c
            LEFT JOIN FETCH c.category
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.text) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY c.idx DESC
            """)
    List<Course> searchByKeyword(@Param("keyword") String keyword);
}
