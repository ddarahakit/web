package com.ddarahakit.backend.domain.community;

import com.ddarahakit.backend.domain.community.model.Post;
import com.ddarahakit.backend.domain.community.model.PostScrap;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    boolean existsByUserAndPost(User user, Post post);

    Optional<PostScrap> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    @Query("select ps.post.idx, count(ps) from PostScrap ps where ps.post in :posts group by ps.post.idx")
    List<Object[]> countByPostIn(@Param("posts") List<Post> posts);

    @Query("select ps.post.idx from PostScrap ps where ps.user = :user and ps.post in :posts")
    List<Long> findPostIdxByUserAndPostIn(@Param("user") User user, @Param("posts") List<Post> posts);

    void deleteAllByPost(Post post);
}
