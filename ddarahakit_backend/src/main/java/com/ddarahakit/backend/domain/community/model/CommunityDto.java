package com.ddarahakit.backend.domain.community.model;

import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.model.Lecture;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.utils.TimeAgoUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommunityDto {

    private CommunityDto() {
    }

    // ================================
    // Request DTOs
    // ================================

    @Getter
    @Schema(description = "게시글 작성 요청")
    public static class PostCreateRequest {

        @Schema(description = "게시글 타입", example = "QUESTION", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "게시글 타입을 선택해주세요.")
        private PostType postType;

        @Schema(description = "게시글 제목", example = "스프링 관련 질문있어요.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "게시글 제목을 입력해주세요.")
        @Size(min = 1, max = 100, message = "게시글 제목은 1자 이상 100자 이하로 입력해주세요.")
        private String title;

        @Schema(description = "게시글 요약 (미리보기용)", example = "스프링 부트 설정 관련...")
        @Size(max = 500, message = "게시글 요약은 500자 이하로 입력해주세요.")
        private String text;

        @Schema(description = "게시글 본문 내용 (HTML)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "게시글 내용을 입력해주세요.")
        private String content;

        @Schema(description = "관련 코스 ID (질문 타입일 때 선택사항)", example = "1")
        private Long courseIdx;

        @Schema(description = "관련 강의 ID (질문 타입일 때 선택사항)", example = "1")
        private Long lectureIdx;

        public Post toEntity(User user, Course course, Lecture lecture) {
            return Post.builder()
                    .postType(this.postType)
                    .title(this.title)
                    .text(this.text)
                    .content(this.content)
                    .user(user)
                    .course(course)
                    .lecture(lecture)
                    .build();
        }
    }

    @Getter
    @Schema(description = "게시글 수정 요청")
    public static class PostUpdateRequest {

        @Schema(description = "게시글 타입", example = "QUESTION", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "게시글 타입을 선택해주세요.")
        private PostType postType;

        @Schema(description = "게시글 제목", example = "스프링 관련 질문있어요.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "게시글 제목을 입력해주세요.")
        @Size(min = 1, max = 100, message = "게시글 제목은 1자 이상 100자 이하로 입력해주세요.")
        private String title;

        @Schema(description = "게시글 요약 (미리보기용)", example = "스프링 부트 설정 관련...")
        @Size(max = 500, message = "게시글 요약은 500자 이하로 입력해주세요.")
        private String text;

        @Schema(description = "게시글 본문 내용 (HTML)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "게시글 내용을 입력해주세요.")
        private String content;

        @Schema(description = "관련 코스 ID (질문 타입일 때 선택사항)", example = "1")
        private Long courseIdx;

        @Schema(description = "관련 강의 ID (질문 타입일 때 선택사항)", example = "1")
        private Long lectureIdx;
    }

    @Getter
    @Schema(description = "댓글 수정 요청")
    public static class CommentUpdateRequest {

        @Schema(description = "댓글 내용 (텍스트)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 2000, message = "댓글은 2000자 이하로 입력해주세요.")
        private String text;

        @Schema(description = "댓글 내용 (HTML)")
        private String content;
    }

    @Getter
    @Schema(description = "댓글 작성 요청")
    public static class CommentCreateRequest {

        @Schema(description = "댓글 내용 (텍스트)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 2000, message = "댓글은 2000자 이하로 입력해주세요.")
        private String text;

        @Schema(description = "댓글 내용 (HTML)")
        private String content;

        @Schema(description = "게시글 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "게시글을 선택해주세요.")
        private Long postIdx;

        public Comment toEntity(User user, Post post) {
            return Comment.builder()
                    .text(this.text)
                    .content(this.content)
                    .user(user)
                    .post(post)
                    .build();
        }
    }

    // ================================
    // Response DTOs
    // ================================

    @Getter
    @Builder
    @Schema(description = "게시글 목록 페이징 응답")
    public static class PostPageResponse {
        @Schema(description = "현재 페이지 번호 (0부터 시작)")
        private final int page;

        @Schema(description = "페이지 크기")
        private final int size;

        @Schema(description = "다음 페이지 존재 여부")
        private final boolean hasNext;

        @Schema(description = "이전 페이지 존재 여부")
        private final boolean hasPrev;

        @Schema(description = "전체 페이지 수")
        private final int totalPages;

        @Schema(description = "전체 게시글 수")
        private final long totalPosts;

        @Schema(description = "게시글 목록")
        private final List<PostSummaryResponse> posts;

        public static PostPageResponse from(Page<Post> postPage) {
            List<PostSummaryResponse> posts = postPage.getContent().stream()
                    .map(PostSummaryResponse::from)
                    .toList();

            return PostPageResponse.builder()
                    .page(postPage.getNumber())
                    .size(postPage.getSize())
                    .hasNext(postPage.hasNext())
                    .hasPrev(postPage.hasPrevious())
                    .totalPages(postPage.getTotalPages())
                    .totalPosts(postPage.getTotalElements())
                    .posts(posts)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "게시글 목록용 요약 응답 (댓글 미포함)")
    public static class PostSummaryResponse {
        @Schema(description = "게시글 ID")
        private final Long idx;

        @Schema(description = "게시글 타입")
        private final PostType postType;

        @Schema(description = "게시글 타입 설명")
        private final String postTypeDescription;

        @Schema(description = "게시글 제목")
        private final String title;

        @Schema(description = "게시글 요약")
        private final String text;

        @Schema(description = "작성자 이름")
        private final String userName;

        @Schema(description = "작성자 ID")
        private final Long userIdx;

        @Schema(description = "관련 코스명 (질문 타입일 때)")
        private final String courseName;

        @Schema(description = "댓글 수")
        private final int commentCount;

        @Schema(description = "스크랩 수")
        private final long scrapCount;

        @Schema(description = "스크랩 여부")
        private final boolean scrapped;

        @Schema(description = "작성 시간")
        private final String createdAt;

        public static PostSummaryResponse from(Post post) {
            return from(post, 0L, false);
        }

        public static PostSummaryResponse from(Post post, long scrapCount, boolean scrapped) {
            User user = post.getUser();
            Course course = post.getCourse();

            return PostSummaryResponse.builder()
                    .idx(post.getIdx())
                    .postType(post.getPostType())
                    .postTypeDescription(post.getPostType().getDescription())
                    .title(post.getTitle())
                    .text(post.getText())
                    .userName(user != null ? user.getName() : "알 수 없음")
                    .userIdx(user != null ? user.getIdx() : null)
                    .courseName(course != null ? course.getName() : null)
                    .commentCount(Optional.ofNullable(post.getComments()).map(List::size).orElse(0))
                    .scrapCount(scrapCount)
                    .scrapped(scrapped)
                    .createdAt(TimeAgoUtil.timeAgo(post.getCreatedAt()))
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "게시글 상세 응답 (댓글 포함)")
    public static class PostDetailResponse {
        @Schema(description = "게시글 ID")
        private final Long idx;

        @Schema(description = "게시글 타입")
        private final PostType postType;

        @Schema(description = "게시글 타입 설명")
        private final String postTypeDescription;

        @Schema(description = "게시글 제목")
        private final String title;

        @Schema(description = "게시글 요약")
        private final String text;

        @Schema(description = "게시글 본문")
        private final String content;

        @Schema(description = "작성자 이름")
        private final String userName;

        @Schema(description = "작성자 ID")
        private final Long userIdx;

        @Schema(description = "작성자 프로필 이미지")
        private final String userProfileImageUrl;

        @Schema(description = "관련 코스 ID")
        private final Long courseIdx;

        @Schema(description = "관련 코스명")
        private final String courseName;

        @Schema(description = "관련 강의 ID")
        private final Long lectureIdx;

        @Schema(description = "관련 강의명")
        private final String lectureName;

        @Schema(description = "댓글 목록")
        private final List<CommentResponse> comments;

        @Schema(description = "스크랩 수")
        private final long scrapCount;

        @Schema(description = "스크랩 여부")
        private final boolean scrapped;

        @Schema(description = "작성 시간")
        private final String createdAt;

        @Schema(description = "수정 시간")
        private final String updatedAt;

        public static PostDetailResponse from(Post post) {
            return from(post, 0L, false);
        }

        public static PostDetailResponse from(Post post, long scrapCount, boolean scrapped) {
            User user = post.getUser();
            Course course = post.getCourse();
            Lecture lecture = post.getLecture();

            List<CommentResponse> comments = Optional.ofNullable(post.getComments())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(CommentResponse::from)
                    .toList();

            return PostDetailResponse.builder()
                    .idx(post.getIdx())
                    .postType(post.getPostType())
                    .postTypeDescription(post.getPostType().getDescription())
                    .title(post.getTitle())
                    .text(post.getText())
                    .content(post.getContent())
                    .userName(user != null ? user.getName() : "알 수 없음")
                    .userIdx(user != null ? user.getIdx() : null)
                    .userProfileImageUrl(user != null ? user.getProfileImageUrl() : null)
                    .courseIdx(course != null ? course.getIdx() : null)
                    .courseName(course != null ? course.getName() : null)
                    .lectureIdx(lecture != null ? lecture.getIdx() : null)
                    .lectureName(lecture != null ? lecture.getName() : null)
                    .comments(comments)
                    .scrapCount(scrapCount)
                    .scrapped(scrapped)
                    .createdAt(TimeAgoUtil.timeAgo(post.getCreatedAt()))
                    .updatedAt(TimeAgoUtil.timeAgo(post.getUpdatedAt()))
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "명예의 전당(랭킹) 항목 응답")
    public static class PostRankingResponse {
        @Schema(description = "게시글 ID")
        private final Long idx;

        @Schema(description = "게시글 타입")
        private final PostType postType;

        @Schema(description = "게시글 타입 설명")
        private final String postTypeDescription;

        @Schema(description = "게시글 제목")
        private final String title;

        @Schema(description = "작성자 이름")
        private final String userName;

        @Schema(description = "조회 수 (모놀리식은 조회수 미지원으로 항상 0)")
        private final long viewCount;

        @Schema(description = "댓글 수")
        private final long commentCount;

        @Schema(description = "스크랩 수")
        private final long scrapCount;

        @Schema(description = "작성 시간")
        private final String createdAt;

        public static PostRankingResponse from(Post post, long commentCount, long scrapCount) {
            User user = post.getUser();

            return PostRankingResponse.builder()
                    .idx(post.getIdx())
                    .postType(post.getPostType())
                    .postTypeDescription(post.getPostType().getDescription())
                    .title(post.getTitle())
                    .userName(user != null ? user.getName() : "알 수 없음")
                    .viewCount(0L)
                    .commentCount(commentCount)
                    .scrapCount(scrapCount)
                    .createdAt(TimeAgoUtil.timeAgo(post.getCreatedAt()))
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "댓글 응답")
    public static class CommentResponse {
        @Schema(description = "댓글 ID")
        private final Long idx;

        @Schema(description = "댓글 내용 (텍스트)")
        private final String text;

        @Schema(description = "댓글 내용 (HTML)")
        private final String content;

        @Schema(description = "작성자 이름")
        private final String userName;

        @Schema(description = "작성자 ID")
        private final Long userIdx;

        @Schema(description = "작성자 프로필 이미지")
        private final String userProfileImageUrl;

        @Schema(description = "작성 시간")
        private final String createdAt;

        public static CommentResponse from(Comment comment) {
            User user = comment.getUser();

            return CommentResponse.builder()
                    .idx(comment.getIdx())
                    .text(comment.getText())
                    .content(comment.getContent())
                    .userName(user != null ? user.getName() : "알 수 없음")
                    .userIdx(user != null ? user.getIdx() : null)
                    .userProfileImageUrl(user != null ? user.getProfileImageUrl() : null)
                    .createdAt(TimeAgoUtil.timeAgo(comment.getCreatedAt()))
                    .build();
        }
    }
}
