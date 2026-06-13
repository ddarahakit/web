package com.ddarahakit.backend.domain.community;

import com.ddarahakit.backend.domain.community.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
