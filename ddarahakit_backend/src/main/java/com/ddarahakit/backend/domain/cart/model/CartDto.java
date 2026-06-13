package com.ddarahakit.backend.domain.cart.model;

import com.ddarahakit.backend.domain.course.model.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class CartDto {

    @Builder
    @Data
    public static class CartItemReq {
        @NotNull(message = "코스 IDX는 필수 입력값입니다.")
        @Min(value = 1, message = "코스 IDX는 1 이상이어야 합니다.")
        @Schema(description = "코스 IDX", required = true, example = "1")
        private Long courseIdx;

        public CartItem toEntity(Cart cart, Course course) {
            return CartItem.builder()
                    .cart(cart)
                    .course(course)
                    .build();
        }
    }

    @Builder
    @Data
    public static class CartItemRes {
        @Schema(description = "장바구니 항목 IDX")
        private Long cartItemIdx;
        @Schema(description = "코스 IDX")
        private Long courseIdx;
        @Schema(description = "코스 이름")
        private String courseName;
        @Schema(description = "코스 이미지")
        private String courseImage;
        @Schema(description = "코스 원래 가격")
        private int originalPrice;
        @Schema(description = "코스 할인 가격")
        private int salePrice;

        // --- 메타 정보(프론트 카드 표시용, 하위호환을 위해 추가만 함) ---
        @Schema(description = "카테고리명(직속 카테고리), 미지정 시 null", example = "프론트엔드")
        private String categoryName;
        @Schema(description = "난이도 코드(BEGINNER/INTERMEDIATE/ADVANCED), 미설정 시 null", example = "BEGINNER")
        private String level;
        @Schema(description = "난이도 표시 라벨(쉬움/보통/어려움), 미설정 시 null", example = "쉬움")
        private String levelDescription;
        @Schema(description = "총 리뷰 수(캐시 컬럼)", example = "12")
        private int totalReviewsCount;
        @Schema(description = "평균 평점(0.0~5.0, 리뷰 없으면 0.0). 평점 버킷으로 계산", example = "4.5")
        private double averageRating;
        @Schema(description = "수강생 수(결제/주문 항목 수)", example = "30")
        private int totalOrderedCount;
        @Schema(description = "코스 짧은 요약(text 컬럼), 미설정 시 null", example = "입문자를 위한 강의")
        private String summary;

        private static double calcAverageRating(Course course) {
            int totalCount = course.getRating1() + course.getRating2() + course.getRating3()
                    + course.getRating4() + course.getRating5();
            if (totalCount == 0) {
                return 0.0;
            }
            int weightedSum = course.getRating1() * 1 + course.getRating2() * 2 + course.getRating3() * 3
                    + course.getRating4() * 4 + course.getRating5() * 5;
            double raw = (double) weightedSum / totalCount;
            return Math.round(raw * 10.0) / 10.0; // 소수점 1자리
        }

        public static CartItemRes of(CartItem entity) {
            Course course = entity.getCourse();
            return CartItemRes.builder()
                    .cartItemIdx(entity.getIdx())
                    .courseIdx(course.getIdx())
                    .courseName(course.getName())
                    .courseImage(course.getImage())
                    .originalPrice(course.getOriginalPrice())
                    .salePrice(course.getSalePrice())
                    .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                    .level(course.getLevel() != null ? course.getLevel().name() : null)
                    .levelDescription(course.getLevel() != null ? course.getLevel().getDisplayName() : null)
                    .totalReviewsCount(course.getTotalReviewsCount() != null ? course.getTotalReviewsCount() : 0)
                    .averageRating(calcAverageRating(course))
                    .totalOrderedCount(course.getOrders() != null ? course.getOrders().size() : 0)
                    .summary(course.getText())
                    .build();
        }
    }

    @Builder
    @Data
    public static class CartCountRes {
        @Schema(description = "장바구니 항목 수")
        private int count;

        public static CartCountRes of(int count) {
            return CartCountRes.builder()
                    .count(count)
                    .build();
        }
    }

    @Builder
    @Data
    public static class CartRes {
        @Schema(description = "장바구니 항목 목록")
        private List<CartItemRes> cartItems;
        @Schema(description = "총 원래 가격")
        private int totalOriginalPrice;
        @Schema(description = "총 할인 가격")
        private int totalSalePrice;

        public static CartRes of(List<CartItem> entities) {
            List<CartItemRes> cartItemResList = entities.stream()
                    .map(CartItemRes::of)
                    .toList();

            int totalOriginalPrice = entities.stream()
                    .mapToInt(item -> item.getCourse().getOriginalPrice())
                    .sum();

            int totalSalePrice = entities.stream()
                    .mapToInt(item -> item.getCourse().getSalePrice())
                    .sum();

            return CartRes.builder()
                    .cartItems(cartItemResList)
                    .totalOriginalPrice(totalOriginalPrice)
                    .totalSalePrice(totalSalePrice)
                    .build();
        }
    }
}
