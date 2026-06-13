package com.ddarahakit.backend.domain.course.repository;


import com.ddarahakit.backend.domain.course.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("SELECT l FROM Lecture l " +
            "JOIN FETCH l.section s " +
            "JOIN FETCH s.course c " +
            "WHERE c.idx = :courseIdx " +
            "ORDER BY l.idx ASC")
    List<Lecture> findAllByCourseIdxOrderByLectureIdxAsc(Long courseIdx);
}
