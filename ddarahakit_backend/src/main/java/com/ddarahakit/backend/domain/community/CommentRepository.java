package com.ddarahakit.backend.domain.community;

import com.ddarahakit.backend.domain.community.model.Comment;
import com.ddarahakit.backend.domain.community.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 모든 댓글 (채택 토글 시 형제 댓글 초기화용)
    List<Comment> findByPost(Post post);
}
