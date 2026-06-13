package com.ddarahakit.backend.domain.review;

import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.review.model.Review;
import com.ddarahakit.backend.domain.review.model.ReviewDto;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 평점/총 리뷰 수 카운터가 원자적 UPDATE(adjustRatingBucket / adjustTotalReviewsCount)로
 * 처리되는지(= read-modify-write 가 제거됐는지) 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock CourseRepository courseRepository;
    @InjectMocks ReviewService reviewService;

    AuthUserDetails userDetails;
    User userEntity;
    Course course;

    @BeforeEach
    void setUp() {
        userDetails = AuthUserDetails.builder()
                .idx(1L).email("user@test.com").name("테스터")
                .password("pw").role("ROLE_USER").enabled(true).build();
        userEntity = User.builder().idx(1L).email("user@test.com").name("테스터")
                .password("pw").role("ROLE_USER").enabled(true).build();
        course = Course.builder().idx(10L).name("테스트 코스").salePrice(10000).originalPrice(15000).build();
    }

    private ReviewDto.ReviewReq req(int rating, String comment) {
        ReviewDto.ReviewReq dto = mock(ReviewDto.ReviewReq.class);
        when(dto.getRating()).thenReturn(rating);
        lenient().when(dto.getComment()).thenReturn(comment);
        lenient().when(dto.toEntity(any(), any())).thenAnswer(inv ->
                Review.builder().comment(comment).rating(rating)
                        .user(inv.getArgument(0)).course(inv.getArgument(1)).build());
        return dto;
    }

    @Test
    @DisplayName("리뷰 작성 - 카운터를 원자적 UPDATE 로 증가시킨다")
    void createReview_원자적_카운터_증가() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDto.ReviewRes res = reviewService.createReview(userDetails, 10L, req(5, "좋아요"));

        assertNotNull(res);
        assertEquals(5, res.getRating());
        // read-modify-write(setRatingN/setTotalReviewsCount) 대신 원자적 UPDATE 호출 검증
        verify(reviewRepository).adjustRatingBucket(10L, 5, +1);
        verify(reviewRepository).adjustTotalReviewsCount(10L, +1);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("리뷰 수정 - 평점 변경 시 이전 -1, 새 평점 +1 을 원자적으로 이동")
    void updateReview_평점변경_원자적이동() {
        Review existing = Review.builder().idx(100L).comment("기존").rating(3)
                .user(userEntity).course(course).build();
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(reviewRepository.findByUserAndCourse(any(User.class), eq(course)))
                .thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDto.ReviewRes res = reviewService.updateReview(userDetails, 10L, req(5, "수정됨"));

        assertEquals(5, res.getRating());
        verify(reviewRepository).adjustRatingBucket(10L, 3, -1);
        verify(reviewRepository).adjustRatingBucket(10L, 5, +1);
        verify(reviewRepository, never()).adjustTotalReviewsCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("리뷰 수정 - 평점이 그대로면 분포 카운터를 건드리지 않는다")
    void updateReview_평점동일_카운터미변경() {
        Review existing = Review.builder().idx(100L).comment("기존").rating(4)
                .user(userEntity).course(course).build();
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(reviewRepository.findByUserAndCourse(any(User.class), eq(course)))
                .thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewService.updateReview(userDetails, 10L, req(4, "내용만 수정"));

        verify(reviewRepository, never()).adjustRatingBucket(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("리뷰 삭제 - 삭제된 평점과 총 리뷰 수를 원자적으로 감소")
    void remove_원자적_카운터_감소() {
        Review existing = Review.builder().idx(100L).comment("삭제대상").rating(2)
                .user(userEntity).course(course).build();
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(reviewRepository.findByUserAndCourse(any(User.class), eq(course)))
                .thenReturn(Optional.of(existing));

        reviewService.remove(userDetails, 10L);

        verify(reviewRepository).delete(existing);
        verify(reviewRepository).adjustRatingBucket(10L, 2, -1);
        verify(reviewRepository).adjustTotalReviewsCount(10L, -1);
        verify(courseRepository, never()).save(any());
    }
}
