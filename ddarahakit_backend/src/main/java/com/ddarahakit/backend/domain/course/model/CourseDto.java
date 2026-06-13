package com.ddarahakit.backend.domain.course.model;

import com.ddarahakit.backend.domain.review.model.Review;
import com.ddarahakit.backend.domain.review.model.ReviewDto;
import com.ddarahakit.backend.utils.TimeAgoUtil;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.*;

public class CourseDto {

    @Getter
    @Builder
    public static class CourseListRes {
        private List<CategoryRes> category;
        List<CourseRes> courses;
        public static CourseListRes of(List<CourseRes> courseResList) {
            return CourseListRes.builder()
                    .courses(courseResList)
                    .build();
        }

        public static CourseListRes of(Category category, List<CourseRes> courseResList) {
            List<CategoryRes> categoryResList = new ArrayList<>();

            categoryResList.add(CategoryRes.of(category));
            while(category.getParent() != null) {
                category = category.getParent();
                categoryResList.add(CategoryRes.of(category));
            }

            Collections.reverse(categoryResList);
            return CourseListRes.builder()
                    .category(categoryResList)
                    .courses(courseResList)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CourseRes {
        private Long idx;
        private String name;
        private String image;
        private String text;
        private String description;
        private List<CategoryRes> category;
        private int originalPrice;
        private int salePrice;
        private String level;            // 난이도 코드(BEGINNER/INTERMEDIATE/ADVANCED), 미설정 시 null
        private String levelDescription; // 난이도 한글명, 미설정 시 null
        private int totalOrderedCount;
        private List<SectionRes> sections;
        private int totalReviewsCount;
        private int rating1;
        private int rating2;
        private int rating3;
        private int rating4;
        private int rating5;
        private boolean isReviewed;
        private boolean isOrdered;
        private ReviewDto.ReviewPageRes reviews;
        private Long nextLectureIdx;
        private String updatedAt;


        // ✅ 공통 빌더 메서드 (기본 CourseRes 생성)
        private static CourseRes.CourseResBuilder buildCommon(Course entity) {
            List<CategoryRes> path = new ArrayList<>();
            buildPath(entity.getCategory(), path);

            return CourseRes.builder()
                    .idx(entity.getIdx())
                    .name(entity.getName())
                    .image(entity.getImage())
                    .text(entity.getText())
                    .description(entity.getDescription())
                    .category(path)
                    .originalPrice(entity.getOriginalPrice())
                    .salePrice(entity.getSalePrice())
                    .level(entity.getLevel() != null ? entity.getLevel().name() : null)
                    .levelDescription(entity.getLevel() != null ? entity.getLevel().getDescription() : null)
                    .totalOrderedCount(entity.getOrders().size())
                    .totalReviewsCount(entity.getTotalReviewsCount())
                    .rating1(entity.getRating1())
                    .rating2(entity.getRating2())
                    .rating3(entity.getRating3())
                    .rating4(entity.getRating4())
                    .rating5(entity.getRating5());
        }

        private static void buildPath(Category category, List<CategoryRes> path) {
            if (category == null) {
                return; // 카테고리 미지정 코스는 빈 path (NPE 방지)
            }
            if (category.getParent() != null) {
                buildPath(category.getParent(), path);
            }
            path.add(CategoryRes.of(category));
        }

        // ✅ 공통 섹션 리스트 변환 메서드
        private static List<SectionRes> mapSections(Course entity, List<Long> lectureCompletes) {
            return entity.getSections() != null
                    ? entity.getSections().stream()
                    .map(section -> SectionRes.of(section, lectureCompletes))
                    .toList()
                    : Collections.emptyList();
        }

        public static CourseRes of(Course entity) {
            return buildCommon(entity)
                    .sections(mapSections(entity, Collections.emptyList()))
                    .nextLectureIdx(0L)
                    .build();
        }

        public static CourseRes of(Course entity, Long nextLectureIdx, List<Long> lectureCompletes) {
            return buildCommon(entity)
                    .sections(mapSections(entity, lectureCompletes))
                    .nextLectureIdx(nextLectureIdx)
                    .build();
        }

        public static CourseRes of(Course entity, List<Long> lectureCompletes) {
            return buildCommon(entity)
                    .sections(mapSections(entity, lectureCompletes))
                    .build();
        }

        public static CourseRes of(Course entity, Slice<Review> reviewPage, boolean isReviewed, boolean isOrdered, Long nextLectureIdx, List<Long> lectureCompletes) {
            return buildCommon(entity)
                    .sections(mapSections(entity, lectureCompletes))
                    .reviews(ReviewDto.ReviewPageRes.of(reviewPage))
                    .isReviewed(isReviewed)
                    .isOrdered(isOrdered)
                    .nextLectureIdx(nextLectureIdx)
                    .updatedAt(TimeAgoUtil.timeAgo(entity.getUpdatedAt()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CategoryTreeRes {
        private Long idx;
        private String name;
        private String slug;
        private int courseCount;
        private List<CategoryTreeRes> children;

        public static CategoryTreeRes of(Category entity, Map<Long, Long> courseCountMap) {
            return CategoryTreeRes.builder()
                    .idx(entity.getIdx())
                    .name(entity.getName())
                    .slug(entity.getSlug())
                    .courseCount(courseCountMap.getOrDefault(entity.getIdx(), 0L).intValue())
                    .children(entity.getChildren().stream()
                            .map(child -> CategoryTreeRes.of(child, courseCountMap))
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CategoryRes {
        private Long idx;
        private String name;
        private String slug;

        public static CategoryRes of(Category entity) {
            return CategoryRes.builder()
                    .idx(entity.getIdx())
                    .name(entity.getName())
                    .slug(entity.getSlug())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class SectionRes {
        private Long idx;
        private String name;
        private List<LectureRes> lectures;

        private static List<LectureRes> mapLectures(Section entity, List<Long> lectureCompletes) {
            return entity.getLectures() != null
                    ? entity.getLectures().stream()
                    .map(lecture -> LectureRes.of(lecture, lectureCompletes))
                    .toList()
                    : Collections.emptyList();
        }


        public static SectionRes of(Section entity, List<Long> lectureCompletes) {
            return SectionRes.builder()
                    .idx(entity.getIdx())
                    .name(entity.getName())
                    .lectures(mapLectures(entity, lectureCompletes))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class LectureRes {
        private Long idx;
        private String name;
        private boolean free;
        private int playTime;
        private String videoUrl;
        private String content;
        private boolean isComplete;

        public static LectureRes of(Lecture entity, List<Long> lectureCompletes) {
            return LectureRes.builder()
                    .idx(entity.getIdx())
                    .name(entity.getName())
                    .free(entity.isFree())
                    .playTime(entity.getPlayTime())
                    // 미리보기(코스 상세) 응답에서는 무료 강의만 영상 URL 노출. 유료 강의는 구매 후 수강 페이지(readLecture)에서만 시청.
                    .videoUrl(entity.isFree() ? entity.getVideoUrl() : null)
                    .content(entity.getContent())
                    .isComplete(lectureCompletes.contains(entity.getIdx()))
                    .build();
        }

        public static LectureRes of(Lecture entity) {
            return LectureRes.builder()
                    .idx(entity.getIdx())
                    .name(entity.getName())
                    .playTime(entity.getPlayTime())
                    // 미리보기(코스 상세) 응답에서는 무료 강의만 영상 URL 노출. 유료 강의는 구매 후 수강 페이지(readLecture)에서만 시청.
                    .videoUrl(entity.isFree() ? entity.getVideoUrl() : null)
                    .content(entity.getContent())
                    .isComplete(false)
                    .build();
        }
    }
}
