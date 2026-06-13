package com.ddarahakit.backend.domain.community.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import com.ddarahakit.backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "post_scrap",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_idx", "post_idx"})
        }
)
public class PostScrap extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_idx", nullable = false)
    private Post post;
}
