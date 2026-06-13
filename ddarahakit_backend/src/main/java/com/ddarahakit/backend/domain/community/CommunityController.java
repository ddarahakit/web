package com.ddarahakit.backend.domain.community;

import com.ddarahakit.backend.common.model.BaseResponse;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.community.model.CommunityDto.CommentCreateRequest;
import com.ddarahakit.backend.domain.community.model.CommunityDto.CommentResponse;
import com.ddarahakit.backend.domain.community.model.CommunityDto.CommentUpdateRequest;
import com.ddarahakit.backend.domain.community.model.CommunityDto.PostCreateRequest;
import com.ddarahakit.backend.domain.community.model.CommunityDto.PostDetailResponse;
import com.ddarahakit.backend.domain.community.model.CommunityDto.PostPageResponse;
import com.ddarahakit.backend.domain.community.model.CommunityDto.PostRankingResponse;
import com.ddarahakit.backend.domain.community.model.CommunityDto.PostUpdateRequest;
import com.ddarahakit.backend.domain.community.model.PostType;
import com.ddarahakit.backend.domain.image.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Community", description = "커뮤니티 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/community")
public class CommunityController {

    private final CommunityService communityService;
    private final FileUploadService fileUploadService;

    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이징으로 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<PostPageResponse>> getPostList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "게시글 타입 (QUESTION, FREE, CAREER, NOTICE)")
            @RequestParam(required = false) PostType postType,
            @Parameter(description = "검색 키워드 (제목, 내용)")
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long courseIdx,
            @PageableDefault(size = 10, sort = "idx", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostPageResponse response = communityService.getPostList(authUserDetails, postType, keyword, courseIdx, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보와 댓글을 조회합니다.")
    @GetMapping("/{postIdx}")
    public ResponseEntity<BaseResponse<PostDetailResponse>> getPostDetail(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "게시글 ID") @PathVariable Long postIdx
    ) {
        PostDetailResponse response = communityService.getPostDetail(authUserDetails, postIdx);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다. 공지사항은 관리자만 작성 가능합니다.")
    @PostMapping("/post")
    public ResponseEntity<BaseResponse<PostDetailResponse>> createPost(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody PostCreateRequest request
    ) {
        PostDetailResponse response = communityService.createPost(authUserDetails, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다. 작성자 본인만 수정 가능합니다.")
    @PutMapping("/post/{postIdx}")
    public ResponseEntity<BaseResponse<PostDetailResponse>> updatePost(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "게시글 ID") @PathVariable Long postIdx,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        PostDetailResponse response = communityService.updatePost(authUserDetails, postIdx, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. 작성자 본인 또는 관리자만 삭제 가능합니다.")
    @DeleteMapping("/post/{postIdx}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "게시글 ID") @PathVariable Long postIdx
    ) {
        communityService.deletePost(authUserDetails, postIdx);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다. 작성자 본인만 수정 가능합니다.")
    @PutMapping("/comment/{commentIdx}")
    public ResponseEntity<BaseResponse<CommentResponse>> updateComment(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "댓글 ID") @PathVariable Long commentIdx,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        CommentResponse response = communityService.updateComment(authUserDetails, commentIdx, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 작성자 본인 또는 관리자만 삭제 가능합니다.")
    @DeleteMapping("/comment/{commentIdx}")
    public ResponseEntity<BaseResponse<Void>> deleteComment(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "댓글 ID") @PathVariable Long commentIdx
    ) {
        communityService.deleteComment(authUserDetails, commentIdx);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @PostMapping("/comment")
    public ResponseEntity<BaseResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = communityService.createComment(authUserDetails, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "이미지 업로드", description = "커뮤니티 게시글용 이미지를 업로드합니다.")
    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<Map<String, String>>> uploadImage(
            @Parameter(description = "업로드할 이미지 파일") MultipartFile image
    ) {
        String savedFileName = fileUploadService.uploadCommunityImage(image);
        return ResponseEntity.ok(BaseResponse.success(Map.of("uploadPath", savedFileName)));
    }

    @Operation(summary = "이미지 조회", description = "업로드된 이미지를 조회합니다.")
    @GetMapping("/display")
    public ResponseEntity<byte[]> displayImage(
            @Parameter(description = "파일명") @RequestParam("fileName") String fileName
    ) {
        return fileUploadService.display(fileName);
    }

    @Operation(summary = "게시글 스크랩", description = "게시글을 스크랩합니다.")
    @PostMapping("/scrap/{postIdx}")
    public ResponseEntity<BaseResponse<Void>> scrap(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long postIdx
    ) {
        communityService.scrap(authUserDetails, postIdx);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @Operation(summary = "게시글 스크랩 취소", description = "게시글 스크랩을 취소합니다.")
    @DeleteMapping("/scrap/{postIdx}")
    public ResponseEntity<BaseResponse<Void>> unscrap(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long postIdx
    ) {
        communityService.unscrap(authUserDetails, postIdx);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @Operation(summary = "게시글 스크랩 토글", description = "게시글 스크랩을 토글합니다.")
    @PostMapping("/scrap/{postIdx}/toggle")
    public ResponseEntity<BaseResponse<Map<String, Object>>> toggleScrap(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long postIdx
    ) {
        boolean scrapped = communityService.toggleScrap(authUserDetails, postIdx);
        return ResponseEntity.ok(BaseResponse.success(Map.of("scrapped", scrapped)));
    }

    @Operation(summary = "스크랩한 게시글 목록", description = "스크랩한 게시글 목록을 조회합니다.")
    @GetMapping("/scrap")
    public ResponseEntity<BaseResponse<PostPageResponse>> getScrapList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PageableDefault(size = 10, sort = "idx", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostPageResponse response = communityService.getScrapList(authUserDetails, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "명예의 전당(랭킹) 조회", description = "인기도(스크랩 수 + 댓글 수)가 높은 게시글 상위 N개를 조회합니다. 모놀리식은 조회수를 지원하지 않아 viewCount는 0으로 응답합니다.")
    @GetMapping("/ranking")
    public ResponseEntity<BaseResponse<List<PostRankingResponse>>> getRanking(
            @Parameter(description = "조회할 게시글 수 (기본값 10)")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<PostRankingResponse> response = communityService.getRanking(limit);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "게시글 타입 목록", description = "사용 가능한 게시글 타입 목록을 조회합니다.")
    @GetMapping("/types")
    public ResponseEntity<BaseResponse<PostType[]>> getPostTypes() {
        return ResponseEntity.ok(BaseResponse.success(PostType.values()));
    }
}
