package com.ddarahakit.backend.domain.course.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import com.ddarahakit.backend.domain.orders.model.OrdersItem;
import com.ddarahakit.backend.domain.review.model.Review;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;
import com.ddarahakit.backend.domain.user.model.entity.User;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Course extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 200)
    private String image;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_idx")
    private Category category;

    @Min(0)
    private int originalPrice;

    @Min(0)
    private int salePrice;

    // 코스 난이도. DB(varchar)는 한글(초급/중급/고급)로 저장 → CourseLevelConverter 가 enum 과 변환.
    // (@Enumerated 는 한글값을 enum 이름으로 valueOf 하다 500 났음). 기존 데이터 NULL 허용.
    @Convert(converter = CourseLevelConverter.class)
    @Column(length = 20)
    private CourseLevel level;

    @ColumnDefault("1")
    private Boolean isDisplay;

    @Setter
    @Min(0)
    @ColumnDefault("0")
    private int rating1;

    @Setter
    @Min(0)
    @ColumnDefault("0")
    private int rating2;

    @Setter
    @Min(0)
    @ColumnDefault("0")
    private int rating3;

    @Setter
    @Min(0)
    @ColumnDefault("0")
    private int rating4;

    @Setter
    @Min(0)
    @ColumnDefault("0")
    private int rating5;

    @Setter
    @Min(0)
    @ColumnDefault("0")
    private Integer totalReviewsCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user;

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "course")
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "course")
    private List<OrdersItem> orders = new ArrayList<>();
}
