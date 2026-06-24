package com.ddarahakit.backend.domain.course.repository;

import com.ddarahakit.backend.domain.course.model.LectureComplete;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LectureCompleteRepository extends JpaRepository<LectureComplete, Long> {
    Optional<LectureComplete> findTopByUserIdxAndCourseIdxOrderByLectureIdxDesc(Long userIdx, Long courseIdx);

    Optional<LectureComplete> findByUserIdxAndCourseIdxAndLectureIdx(Long userIdx, Long courseIdx, Long lectureIdx);



    List<LectureComplete> findByUserAndCourseIdx(User user, Long courseIdx);

    /**
     * 사용자의 여러 코스 수강완료를 한 번에 조회(코스 목록의 진도/다음 강의 계산용).
     * 코스마다 따로 조회하던 N+1 을 단일 쿼리로 대체한다.
     */
    @Query("SELECT lc FROM LectureComplete lc WHERE lc.user = :user AND lc.course.idx IN :courseIdxList")
    List<LectureComplete> findByUserAndCourseIdxIn(@Param("user") User user, @Param("courseIdxList") List<Long> courseIdxList);

    /** 사용자의 총 수강완료 강의 수(회원 레벨/활동점수 산출용). */
    long countByUserIdx(Long userIdx);

    /**
     * 주간 학습활동: 지정 시각(from) 이후 완료된 수강완료의 '완료 시각' 목록(최신순).
     * 일별 집계/연속학습 계산은 서비스에서 날짜 단위로 처리한다. completedAt NULL(과거 데이터)은 제외.
     */
    @Query("""
            SELECT lc.completedAt FROM LectureComplete lc
            WHERE lc.user.idx = :userIdx AND lc.completedAt IS NOT NULL AND lc.completedAt >= :from
            ORDER BY lc.completedAt DESC
            """)
    List<LocalDateTime> findCompletedAtByUserSince(@Param("userIdx") Long userIdx,
                                                   @Param("from") LocalDateTime from);
}
