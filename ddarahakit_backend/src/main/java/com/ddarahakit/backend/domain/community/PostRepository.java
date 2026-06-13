package com.ddarahakit.backend.domain.community;

import com.ddarahakit.backend.domain.community.model.Post;
import com.ddarahakit.backend.domain.community.model.PostType;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 목록 조회 - user, course fetch join (comments는 목록에서 불필요)
    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course")
    Page<Post> findAllWithUser(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.postType = :postType")
    Page<Post> findAllByPostTypeWithUser(@Param("postType") PostType postType, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.title LIKE %:keyword% OR p.text LIKE %:keyword%")
    Page<Post> searchByKeywordWithUser(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.postType = :postType AND (p.title LIKE %:keyword% OR p.text LIKE %:keyword%)")
    Page<Post> searchByPostTypeAndKeywordWithUser(@Param("postType") PostType postType, @Param("keyword") String keyword, Pageable pageable);

    // 코스 필터링 포함
    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.course.idx = :courseIdx")
    Page<Post> findAllWithUserByCourse(@Param("courseIdx") Long courseIdx, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.postType = :postType AND p.course.idx = :courseIdx")
    Page<Post> findAllByPostTypeWithUserByCourse(@Param("postType") PostType postType, @Param("courseIdx") Long courseIdx, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.course.idx = :courseIdx AND (p.title LIKE %:keyword% OR p.text LIKE %:keyword%)")
    Page<Post> searchByKeywordWithUserByCourse(@Param("keyword") String keyword, @Param("courseIdx") Long courseIdx, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.postType = :postType AND p.course.idx = :courseIdx AND (p.title LIKE %:keyword% OR p.text LIKE %:keyword%)")
    Page<Post> searchByPostTypeAndKeywordWithUserByCourse(@Param("postType") PostType postType, @Param("keyword") String keyword, @Param("courseIdx") Long courseIdx, Pageable pageable);

    // 사용자의 게시글 조회 (질문 제외)
    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.user = :user AND p.postType NOT IN :excludeTypes ORDER BY p.createdAt DESC")
    List<Post> findByUserAndPostTypeNotInWithCourse(@Param("user") User user, @Param("excludeTypes") List<PostType> excludeTypes);

    // 사용자의 특정 타입 게시글 조회
    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.user = :user AND p.postType = :postType ORDER BY p.createdAt DESC")
    List<Post> findByUserAndPostTypeWithCourse(@Param("user") User user, @Param("postType") PostType postType);

    // 상세 조회 - user, comments, course, lecture 모두 fetch join
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.course " +
            "LEFT JOIN FETCH p.lecture " +
            "LEFT JOIN FETCH p.comments c " +
            "LEFT JOIN FETCH c.user " +
            "WHERE p.idx = :idx")
    Optional<Post> findByIdWithUserAndComments(@Param("idx") Long idx);

    @Query("SELECT p FROM PostScrap ps " +
            "JOIN ps.post p " +
            "JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.course " +
            "WHERE ps.user = :user " +
            "ORDER BY ps.createdAt DESC")
    Page<Post> findScrappedPostsByUser(@Param("user") User user, Pageable pageable);

    // 명예의 전당(랭킹) - 인기도(스크랩 수 + 댓글 수) 내림차순, 동점 시 최신순 상위 N개.
    // 모놀리식 Post에는 조회수가 없으므로 인기도 식에서 조회수는 제외한다.
    // 각 행: [Post, commentCount(Long), scrapCount(Long)]
    @Query("SELECT p, " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.post = p) AS commentCount, " +
            "(SELECT COUNT(s) FROM PostScrap s WHERE s.post = p) AS scrapCount " +
            "FROM Post p JOIN FETCH p.user " +
            "ORDER BY ((SELECT COUNT(c) FROM Comment c WHERE c.post = p) " +
            "+ (SELECT COUNT(s) FROM PostScrap s WHERE s.post = p)) DESC, p.createdAt DESC")
    List<Object[]> findRanking(Pageable pageable);
}
