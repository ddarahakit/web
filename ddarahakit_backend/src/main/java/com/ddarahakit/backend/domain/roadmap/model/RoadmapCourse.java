package com.ddarahakit.backend.domain.roadmap.model;

import com.ddarahakit.backend.domain.course.model.Course;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class RoadmapCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_idx")
    private Roadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_idx")
    private Course course;

    private int sortOrder;
}
