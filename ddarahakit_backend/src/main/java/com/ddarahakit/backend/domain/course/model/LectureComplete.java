package com.ddarahakit.backend.domain.course.model;

import com.ddarahakit.backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "lecture_complete",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_idx", "lecture_idx"})
        }
)
public class LectureComplete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_idx")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_idx")
    private Course course;

    // 수강완료 시각. 주간 학습활동/연속학습 집계용.
    // ⚠️ nullable: 기존 행은 시각 정보가 없어 NULL(과거 데이터는 주간 집계에서 제외됨).
    //    신규 완료부터 @PrePersist 로 기록 → /user/study/weekly 가 신규 데이터 기준으로 정확.
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        if (this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
