package com.ddarahakit.backend.domain.community;

import com.ddarahakit.backend.domain.community.model.Comment;
import com.ddarahakit.backend.domain.community.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 모든 댓글 (채택 토글 시 형제 댓글 초기화용)
    List<Comment> findByPost(Post post);

    // 게시글별 댓글 수 일괄 집계(목록 응답용). 각 행: [postIdx, count].
    // 게시글마다 comments 컬렉션을 로딩해 size 로 세던 낭비(LONGTEXT 본문까지 적재)를 COUNT 로 대체.
    @Query("SELECT c.post.idx, COUNT(c) FROM Comment c WHERE c.post IN :posts GROUP BY c.post.idx")
    List<Object[]> countByPostIn(Collection<Post> posts);
}
