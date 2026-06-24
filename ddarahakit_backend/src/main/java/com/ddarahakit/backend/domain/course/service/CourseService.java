package com.ddarahakit.backend.domain.course.service;


import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.course.repository.CategoryRepository;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.course.repository.LectureCompleteRepository;
import com.ddarahakit.backend.domain.course.repository.LectureRepository;
import com.ddarahakit.backend.domain.course.model.*;
import com.ddarahakit.backend.domain.orders.OrdersItemRepository;
import com.ddarahakit.backend.domain.review.ReviewRepository;
import com.ddarahakit.backend.domain.review.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;
    private final ReviewRepository reviewRepository;
    private final OrdersItemRepository ordersItemRepository;
    private final LectureCompleteRepository lectureCompleteRepository;
    private final CategoryRepository categoryRepository;

    public List<CourseDto.CategoryTreeRes> categoryList() {
        Map<Long, Long> courseCountMap = courseRepository.countByCategoryGrouped().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return categoryRepository.findByParentIsNull().stream()
                .map(category -> CourseDto.CategoryTreeRes.of(category, courseCountMap))
                .toList();
    }

    public CourseDto.CourseListRes list() {
        List<Course> result = courseRepository.findAll();

        return CourseDto.CourseListRes.of(result.stream().map(CourseDto.CourseSummaryRes::of).toList());

    }

    /**
     * 키워드 검색(강의명/소개/설명). 키워드가 비어있으면 빈 목록. 응답은 기존 코스 목록 DTO 동일.
     */
    public CourseDto.CourseListRes search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return CourseDto.CourseListRes.of(List.of());
        }
        List<Course> result = courseRepository.searchByKeyword(keyword.trim());

        return CourseDto.CourseListRes.of(result.stream().map(CourseDto.CourseSummaryRes::of).toList());
    }

    /**
     * 정렬/필터/난이도 적용 코스 목록.
     * - sort: popular(인기, 주문수)/latest(최신, idx)/rating(평점). 기본 latest.
     * - filter: free(무료)/new(최근 8개)/subscribed(구매한 코스, 로그인 필요). 기본 없음.
     * - level: BEGINNER/INTERMEDIATE/ADVANCED. 기본 전체.
     *
     * 가정: '구독(subscribed)'은 별도 구독 모델이 없어 '구매한 코스'로 해석한다.
     *       '신규(new)'는 createdAt 신뢰도가 낮아 idx 역순 상위 8개로 정의한다.
     */
    public CourseDto.CourseListRes list(String sort, String filter, CourseLevel level, AuthUserDetails authUserDetails) {
        boolean freeOnly = "free".equalsIgnoreCase(filter);
        List<Course> result = new ArrayList<>(courseRepository.findForList(level, freeOnly));

        // filter=subscribed → 구매한 코스만 (로그인 시에만 의미)
        if ("subscribed".equalsIgnoreCase(filter) && authUserDetails != null) {
            java.util.Set<Long> purchased = ordersItemRepository
                    .findByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalse(authUserDetails.toEntity())
                    .stream().map(oi -> oi.getCourse().getIdx()).collect(Collectors.toSet());
            result = result.stream().filter(c -> purchased.contains(c.getIdx())).collect(Collectors.toList());
        }

        // 정렬
        String sortKey = sort == null ? "latest" : sort.toLowerCase();
        switch (sortKey) {
            case "popular" -> {
                // 코스별 주문수를 단일 집계 쿼리로 조회(코스마다 orders 컬렉션 적재 방지).
                java.util.Map<Long, Long> orderCountMap = ordersItemRepository.countGroupByCourse().stream()
                        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
                result.sort((a, b) -> Long.compare(
                        orderCountMap.getOrDefault(b.getIdx(), 0L),
                        orderCountMap.getOrDefault(a.getIdx(), 0L)));
            }
            case "rating" -> result.sort((a, b) -> Integer.compare(
                    b.getTotalReviewsCount() == null ? 0 : b.getTotalReviewsCount(),
                    a.getTotalReviewsCount() == null ? 0 : a.getTotalReviewsCount()));
            default -> result.sort((a, b) -> Long.compare(b.getIdx(), a.getIdx())); // latest
        }

        // filter=new → 최신 상위 8개
        if ("new".equalsIgnoreCase(filter)) {
            result.sort((a, b) -> Long.compare(b.getIdx(), a.getIdx()));
            if (result.size() > 8) {
                result = new ArrayList<>(result.subList(0, 8));
            }
        }

        return CourseDto.CourseListRes.of(result.stream().map(CourseDto.CourseSummaryRes::of).toList());
    }


    public CourseDto.CourseListRes list(String slug) {
        Category category = categoryRepository.findBySlug(slug).orElseThrow(
                () -> BaseException.of(CATEGORY_NOT_FOUND)
        );
        List<Long> categoryIdxList = categoryRepository.findSubCategoryIdxList(category.getMaterializedPath());
        List<Course> result = courseRepository.findCoursesBycategoryIdxList(categoryIdxList);

        return CourseDto.CourseListRes.of(category, result.stream().map(CourseDto.CourseSummaryRes::of).toList());
    }


    public CourseDto.CourseRes readCourse(AuthUserDetails authUserDetails, Long courseIdx) {
        Slice<Review> reviewPage;
        boolean isReviewed = false;
        boolean isOrdered = false;
        List<Review> reviews = new ArrayList<>();
        Long nextLectureIdx = 0L;
        List<Long> lectureCompleteList = List.of();

        Course course = courseRepository.findById(courseIdx).orElseThrow(
                () -> BaseException.of(COURSE_NOT_FOUND)
        );

        if (authUserDetails != null) {
            boolean isOrderedByUser = ordersItemRepository
                    .existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(authUserDetails.toEntity(), course);

            if (isOrderedByUser) {
                isOrdered = true;
                nextLectureIdx = readNextLecture(authUserDetails, courseIdx).getIdx();

                Optional<Review> userReview = reviewRepository.findByUserAndCourse(authUserDetails.toEntity(), course);

                if (userReview.isPresent()) {
                    isReviewed = true;
                    reviews.add(userReview.get());
                    reviewPage = reviewRepository.findByUserNotAndCourseOrderByCreatedAtDesc(
                            authUserDetails.toEntity(), course, PageRequest.of(0, 3)
                    );
                    reviews.addAll(reviewPage.getContent());
                } else {
                    reviewPage = reviewRepository.findByUserNotAndCourseOrderByCreatedAtDesc(
                            authUserDetails.toEntity(), course, PageRequest.of(0, 4)
                    );
                    reviews.addAll(reviewPage.getContent());
                }

                reviewPage = new SliceImpl<>(reviews, reviewPage.getPageable(), reviewPage.hasNext());

                lectureCompleteList = lectureCompleteRepository.findByUserAndCourseIdx(authUserDetails.toEntity(), courseIdx)
                        .stream()
                        .map(lc -> lc.getLecture().getIdx())
                        .toList();
            } else {
                reviewPage = reviewRepository.findByCourseOrderByCreatedAtDesc(course, PageRequest.of(0, 4));
            }
        } else {
            reviewPage = reviewRepository.findByCourseOrderByCreatedAtDesc(course, PageRequest.of(0, 4));
        }


        return CourseDto.CourseRes.of(course, reviewPage, isReviewed, isOrdered, nextLectureIdx, lectureCompleteList);
    }

    public LectureDto.LectureRes readLecture(AuthUserDetails authUserDetails, Long courseIdx, Long lectureIdx) {
        Course course = courseRepository.findById(courseIdx).orElseThrow(
                () -> BaseException.of(COURSE_NOT_FOUND)
        );

        Lecture lecture = lectureRepository.findById(lectureIdx).orElseThrow(
                () -> BaseException.of(LECTURE_NOT_FOUND)
        );

        // 강의가 해당 코스에 속해있는지 확인
        if (!lecture.getSection().getCourse().getIdx().equals(course.getIdx())) {
            throw BaseException.of(LECTURE_NOT_IN_COURSE);
        }

        List<Long> lectureCompleteList = List.of(); // 기본 빈 리스트

        // 로그인한 사용자일 경우에만 확인
        if (authUserDetails != null) {
            // 해당 유저가 해당 코스를 구매했는지 확인
            boolean hasPurchased = ordersItemRepository
                    .existsByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalseAndCourse(authUserDetails.toEntity(), course);

            if (!hasPurchased) {
                throw BaseException.of(ORDERS_NOT_ORDERED);
            }

            // 해당 유저가 수강 완료한 강의 리스트 조회
            lectureCompleteList = lectureCompleteRepository.findByUserAndCourseIdx(authUserDetails.toEntity(), courseIdx)
                    .stream()
                    .map(lc -> lc.getLecture().getIdx())
                    .toList();
        }

        return LectureDto.LectureRes.of(lecture, lectureCompleteList);
    }


    public LectureDto.LectureCompleteRes readNextLecture(AuthUserDetails authUserDetails, Long courseIdx) {
        Long lectureIdx = lectureCompleteRepository
                .findTopByUserIdxAndCourseIdxOrderByLectureIdxDesc(authUserDetails.getIdx(), courseIdx)
                .map(lectureComplete -> lectureComplete.getLecture().getIdx())
                .orElse(0L);

        List<Lecture> lectureList = lectureRepository.findAllByCourseIdxOrderByLectureIdxAsc(courseIdx);

        // 강의 목록이 비어 있으면 기본값 반환
        if (lectureList.isEmpty()) {
            return LectureDto.LectureCompleteRes.builder().idx(0L).build();
        }

        // 완료한 강의가 없거나 마지막 강의인 경우 첫 번째 강의 반환
        if (lectureIdx == 0L || lectureList.get(lectureList.size() - 1).getIdx().equals(lectureIdx)) {
            return LectureDto.LectureCompleteRes.of(lectureList.get(0));
        }

        // 현재 완료한 강의의 다음 강의 찾기
        return lectureList.stream()
                .filter(lecture -> lecture.getIdx() > lectureIdx)
                .findFirst()
                .map(LectureDto.LectureCompleteRes::of)
                .orElse(LectureDto.LectureCompleteRes.of(lectureList.get(0))); // 예외 처리용 기본값 반환
    }

    @Transactional
    public LectureDto.LectureCompleteRes lectureComplete(AuthUserDetails authUserDetails, LectureDto.LectureCompleteReq dto) {
        Optional<LectureComplete> result = lectureCompleteRepository.findByUserIdxAndCourseIdxAndLectureIdx(authUserDetails.getIdx(), dto.getCourseIdx(),dto.getLectureIdx());

        if(result.isPresent()) {
            throw BaseException.of(ALREADY_LECTURE_COMPLETE);
        }

        LectureComplete lectureComplete = lectureCompleteRepository.save(dto.toEntity(authUserDetails.toEntity()));

        return LectureDto.LectureCompleteRes.of(lectureComplete.getLecture());
    }

}
