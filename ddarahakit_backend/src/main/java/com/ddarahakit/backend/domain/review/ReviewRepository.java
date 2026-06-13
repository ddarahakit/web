package com.ddarahakit.backend.domain.review;

import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.review.model.Review;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByUserAndCourse(User user, Course course);

    Slice<Review> findByCourseOrderByCreatedAtDesc(Course result, Pageable pageable);

    Slice<Review> findByCourse(Course course, Pageable pageable);

    Slice<Review> findByUserNotAndCourseOrderByCreatedAtDesc(User user, Course course, Pageable pageable);

    List<Review> findByUser(User user);

    /**
     * 평점 분포 카운터 원자적 증감.
     * read-modify-write 대신 DB 단일 UPDATE 로 처리해 동시 리뷰 시 lost update 를 방지한다.
     * rating(1~5)에 해당하는 컬럼만 :delta(+1/-1) 만큼 조정한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Course c SET
                c.rating1 = c.rating1 + (CASE WHEN :rating = 1 THEN :delta ELSE 0 END),
                c.rating2 = c.rating2 + (CASE WHEN :rating = 2 THEN :delta ELSE 0 END),
                c.rating3 = c.rating3 + (CASE WHEN :rating = 3 THEN :delta ELSE 0 END),
                c.rating4 = c.rating4 + (CASE WHEN :rating = 4 THEN :delta ELSE 0 END),
                c.rating5 = c.rating5 + (CASE WHEN :rating = 5 THEN :delta ELSE 0 END)
            WHERE c.idx = :courseIdx
            """)
    int adjustRatingBucket(@Param("courseIdx") Long courseIdx,
                           @Param("rating") int rating,
                           @Param("delta") int delta);

    /**
     * 총 리뷰 수 카운터 원자적 증감.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Course c SET c.totalReviewsCount = c.totalReviewsCount + :delta WHERE c.idx = :courseIdx")
    int adjustTotalReviewsCount(@Param("courseIdx") Long courseIdx, @Param("delta") int delta);

    /** 통계용: 전체 리뷰 수. */
    @Query("SELECT COUNT(r) FROM Review r")
    long countAllReviews();

    /** 통계용: 만족(4점 이상) 리뷰 수. */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.rating >= 4")
    long countSatisfiedReviews();
}
