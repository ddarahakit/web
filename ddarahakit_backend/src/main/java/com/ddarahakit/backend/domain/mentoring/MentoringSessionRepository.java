package com.ddarahakit.backend.domain.mentoring;

import com.ddarahakit.backend.domain.mentoring.model.MentoringSession;
import com.ddarahakit.backend.domain.mentoring.model.MentoringStatus;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {
    @Query(value = """
            select s from MentoringSession s
            join fetch s.mentor
            join fetch s.mentee
            where (s.mentor = :user or s.mentee = :user)
              and (:status is null or s.status = :status)
              and (:keyword is null or s.subject like %:keyword%)
            order by s.updatedAt desc
            """,
            countQuery = """
            select count(s) from MentoringSession s
            where (s.mentor = :user or s.mentee = :user)
              and (:status is null or s.status = :status)
              and (:keyword is null or s.subject like %:keyword%)
            """)
    Page<MentoringSession> findForUser(
            @Param("user") User user,
            @Param("status") MentoringStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
