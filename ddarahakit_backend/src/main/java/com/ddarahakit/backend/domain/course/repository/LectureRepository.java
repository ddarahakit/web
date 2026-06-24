package com.ddarahakit.backend.domain.course.repository;


import com.ddarahakit.backend.domain.course.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("SELECT l FROM Lecture l " +
            "JOIN FETCH l.section s " +
            "JOIN FETCH s.course c " +
            "WHERE c.idx = :courseIdx " +
            "ORDER BY l.idx ASC")
    List<Lecture> findAllByCourseIdxOrderByLectureIdxAsc(Long courseIdx);

    /**
     * 여러 코스의 전체 강의를 코스별·강의 idx 순으로 한 번에 조회(코스 목록의 다음 강의 계산용).
     * 코스마다 강의를 따로 조회하던 N+1 을 단일 쿼리로 대체한다.
     */
    @Query("SELECT l FROM Lecture l " +
            "JOIN FETCH l.section s " +
            "JOIN FETCH s.course c " +
            "WHERE c.idx IN :courseIdxList " +
            "ORDER BY c.idx ASC, l.idx ASC")
    List<Lecture> findAllByCourseIdxInOrderByLectureIdxAsc(@Param("courseIdxList") List<Long> courseIdxList);
}
