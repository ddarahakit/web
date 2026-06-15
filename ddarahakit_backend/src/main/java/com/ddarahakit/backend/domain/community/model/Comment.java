package com.ddarahakit.backend.domain.community.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import com.ddarahakit.backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(columnDefinition = "LONGTEXT")
    private String text;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // 질문 작성자가 채택한 베스트 답변 여부
    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private boolean accepted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_idx")
    private Post post;

    public void update(String text, String content) {
        this.text = text;
        this.content = content;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
