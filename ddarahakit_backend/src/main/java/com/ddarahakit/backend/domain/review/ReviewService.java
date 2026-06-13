package com.ddarahakit.backend.domain.review;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.review.model.Review;
import com.ddarahakit.backend.domain.review.model.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.RESPONSE_NULL_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;

    public ReviewDto.ReviewPageRes readReview(AuthUserDetails authUserDetails, Long courseIdx, Pageable pageable) {
        Course course = courseRepository.findById(courseIdx).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );

        Slice<Review> reviewPage;
        if (authUserDetails != null) {
            reviewPage = reviewRepository.findByUserNotAndCourseOrderByCreatedAtDesc(
                    authUserDetails.toEntity(), course, pageable
            );
        } else {
            reviewPage = reviewRepository.findByCourse(course, pageable);
        }

        return ReviewDto.ReviewPageRes.of(reviewPage);
    }

    /**
     * 리뷰 작성.
     * 평점 분포/총 리뷰 수 카운터는 원자적 UPDATE 로 증가시켜 동시 작성 시 lost update 를 방지한다.
     * 카운터 UPDATE 는 영속성 컨텍스트를 비우므로(@Modifying clearAutomatically),
     * 엔티티 변경(리뷰 저장)을 먼저 수행한 뒤 카운터를 조정한다.
     */
    @Transactional
    public ReviewDto.ReviewRes createReview(AuthUserDetails authUserDetails, Long courseIdx, ReviewDto.ReviewReq dto) {
        Course course = courseRepository.findById(courseIdx).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );

        Review review = reviewRepository.save(dto.toEntity(authUserDetails.toEntity(), course));

        // 원자적 카운터 증가 (read-modify-write 제거)
        reviewRepository.adjustRatingBucket(courseIdx, dto.getRating(), +1);
        reviewRepository.adjustTotalReviewsCount(courseIdx, +1);

        return ReviewDto.ReviewRes.of(review);
    }

    /**
     * 리뷰 수정.
     * "이전 평점 -1, 새 평점 +1" 을 한 트랜잭션 안에서 원자적 UPDATE 로 처리한다.
     */
    @Transactional
    public ReviewDto.ReviewRes updateReview(AuthUserDetails authUserDetails, Long courseIdx, ReviewDto.ReviewReq dto) {
        Course course = courseRepository.findById(courseIdx).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );
        Review review = reviewRepository.findByUserAndCourse(authUserDetails.toEntity(), course).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );

        int previousRating = review.getRating();
        int newRating = dto.getRating();

        review.setRating(newRating);
        review.setComment(dto.getComment());
        review = reviewRepository.save(review);

        // 평점 변경 시에만 분포 카운터를 원자적으로 이동 (동시 수정 시에도 정합 유지)
        if (previousRating != newRating) {
            reviewRepository.adjustRatingBucket(courseIdx, previousRating, -1);
            reviewRepository.adjustRatingBucket(courseIdx, newRating, +1);
        }

        return ReviewDto.ReviewRes.of(review);
    }

    /**
     * 리뷰 삭제.
     * 삭제 전 평점을 보관한 뒤 카운터를 원자적으로 감소시킨다.
     */
    @Transactional
    public ReviewDto.ReviewRes remove(AuthUserDetails authUserDetails, Long courseIdx) {
        Course course = courseRepository.findById(courseIdx).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );
        Review review = reviewRepository.findByUserAndCourse(authUserDetails.toEntity(), course).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );

        int removedRating = review.getRating();
        ReviewDto.ReviewRes response = ReviewDto.ReviewRes.of(review);

        reviewRepository.delete(review);

        // 원자적 카운터 감소
        reviewRepository.adjustRatingBucket(courseIdx, removedRating, -1);
        reviewRepository.adjustTotalReviewsCount(courseIdx, -1);

        return response;
    }
}
