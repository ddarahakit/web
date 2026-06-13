package com.ddarahakit.backend.domain.review.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "review",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_idx", "course_idx"})
        }
)
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 100, nullable = false)
    private String comment;

    @Column(length = 200, nullable = false)
    private int rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_idx", nullable = false)
    private Course course;

}
