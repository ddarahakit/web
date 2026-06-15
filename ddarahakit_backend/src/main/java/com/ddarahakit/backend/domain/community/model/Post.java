package com.ddarahakit.backend.domain.community.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.model.Lecture;
import com.ddarahakit.backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;

    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String text;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // 조회수
    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private int viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user;

    // 질문 타입일 때 선택적으로 연결 가능
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_idx")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_idx")
    private Lecture lecture;

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    // 게시글 태그 (검색·관련 게시글 매칭용)
    @Builder.Default
    @BatchSize(size = 100)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_idx"))
    @Column(name = "tag", length = 30)
    private Set<String> tags = new LinkedHashSet<>();

    public void update(PostType postType, String title, String text, String content, Course course, Lecture lecture, Set<String> tags) {
        this.postType = postType;
        this.title = title;
        this.text = text;
        this.content = content;
        this.course = course;
        this.lecture = lecture;
        // @ElementCollection은 참조 교체보다 in-place 변경이 안전
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }
}
