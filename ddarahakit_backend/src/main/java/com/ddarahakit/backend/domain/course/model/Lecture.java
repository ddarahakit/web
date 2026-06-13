package com.ddarahakit.backend.domain.course.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Lecture extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, length = 100)
    private String name;

    @ColumnDefault("false")
    private boolean free;

    @Column(nullable = true)
    private int playTime;

    @Column(length = 200)
    private String videoUrl;

    @Column(columnDefinition = "LONGTEXT")
    private String text;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "section_idx")
    private Section section;
}
