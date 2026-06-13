package com.ddarahakit.backend.domain.stats;

import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.orders.OrdersRepository;
import com.ddarahakit.backend.domain.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서비스 요약 통계.
 * 가정:
 *  - studentCount = 결제 완료(환불 제외) 주문을 가진 고유 사용자 수.
 *  - satisfactionRate = (4점 이상 리뷰 수 / 전체 리뷰 수) * 100, 리뷰 0건이면 0.
 *  세션/방문자 기반 지표는 별도 분석 인프라가 없어 도입하지 않음.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final CourseRepository courseRepository;
    private final OrdersRepository ordersRepository;
    private final ReviewRepository reviewRepository;

    public StatsDto.SummaryRes summary() {
        long courseCount = courseRepository.count();
        long studentCount = ordersRepository.countDistinctPaidStudents();

        long total = reviewRepository.countAllReviews();
        long satisfied = reviewRepository.countSatisfiedReviews();
        int satisfactionRate = total == 0 ? 0 : (int) Math.round(satisfied * 100.0 / total);

        return StatsDto.SummaryRes.builder()
                .courseCount(courseCount)
                .studentCount(studentCount)
                .satisfactionRate(satisfactionRate)
                .build();
    }
}
