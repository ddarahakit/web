package com.ddarahakit.backend.domain.community;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.community.model.*;
import com.ddarahakit.backend.domain.community.model.CommunityDto.*;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.course.repository.LectureRepository;
import com.ddarahakit.backend.domain.user.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommunityServiceTest {

    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;
    @Mock PostScrapRepository postScrapRepository;
    @Mock CourseRepository courseRepository;
    @Mock LectureRepository lectureRepository;
    @InjectMocks CommunityService communityService;

    AuthUserDetails userDetails;
    AuthUserDetails adminDetails;
    AuthUserDetails otherUserDetails;
    User userEntity;
    User adminEntity;
    User otherUserEntity;

    @BeforeEach
    void setUp() {
        userDetails = AuthUserDetails.builder()
                .idx(1L).email("user@test.com").name("테스터")
                .password("password").role("ROLE_USER").enabled(true).build();
        adminDetails = AuthUserDetails.builder()
                .idx(2L).email("admin@test.com").name("관리자")
                .password("password").role("ROLE_ADMIN").enabled(true).build();
        otherUserDetails = AuthUserDetails.builder()
                .idx(99L).email("other@test.com").name("타인")
                .password("password").role("ROLE_USER").enabled(true).build();
        userEntity = User.builder().idx(1L).email("user@test.com").name("테스터")
                .password("pw").role("ROLE_USER").enabled(true).build();
        adminEntity = User.builder().idx(2L).email("admin@test.com").name("관리자")
                .password("pw").role("ROLE_ADMIN").enabled(true).build();
        otherUserEntity = User.builder().idx(99L).email("other@test.com").name("타인")
                .password("pw").role("ROLE_USER").enabled(true).build();
    }

    /** BaseEntity의 createdAt/updatedAt은 @PrePersist로만 설정되므로 테스트에서 직접 주입 */
    private void setTimestamps(Object entity) {
        Date now = new Date();
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "updatedAt", now);
    }

    // ============================
    // getPostDetail
    // ============================

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostDetail_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE)
                .title("제목").text("요약").content("본문").user(userEntity).build();
        setTimestamps(post);
        when(postRepository.findByIdWithUserAndComments(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.countByPost(post)).thenReturn(0L);
        when(postScrapRepository.existsByUserAndPost(any(User.class), eq(post))).thenReturn(false);

        PostDetailResponse result = communityService.getPostDetail(userDetails, 1L);

        assertNotNull(result);
        verify(postRepository).findByIdWithUserAndComments(1L);
    }

    @Test
    @DisplayName("게시글 상세 조회 - 게시글 없음 예외")
    void getPostDetail_게시글없음_예외() {
        when(postRepository.findByIdWithUserAndComments(99L)).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> communityService.getPostDetail(userDetails, 99L));
        assertEquals(POST_NOT_FOUND, ex.getStatus());
    }

    // ============================
    // createPost
    // ============================

    @Test
    @DisplayName("일반 사용자가 NOTICE 작성 시 예외")
    void createPost_일반사용자_NOTICE_예외() {
        PostCreateRequest req = mock(PostCreateRequest.class);
        when(req.getPostType()).thenReturn(PostType.NOTICE);

        BaseException ex = assertThrows(BaseException.class,
                () -> communityService.createPost(userDetails, req));
        assertEquals(INVALID_USER_ROLE, ex.getStatus());
    }

    @Test
    @DisplayName("관리자가 NOTICE 작성 성공")
    void createPost_관리자_NOTICE_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.NOTICE)
                .title("공지").text("요약").content("내용").user(adminEntity).build();
        setTimestamps(post);

        PostCreateRequest req = mock(PostCreateRequest.class);
        when(req.getPostType()).thenReturn(PostType.NOTICE);
        when(req.getCourseIdx()).thenReturn(null);
        when(req.getLectureIdx()).thenReturn(null);
        when(req.toEntity(any(User.class), isNull(), isNull())).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostDetailResponse result = communityService.createPost(adminDetails, req);

        assertNotNull(result);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("일반 게시글 작성 성공")
    void createPost_일반_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE)
                .title("자유글").text("요약").content("내용").user(userEntity).build();
        setTimestamps(post);

        PostCreateRequest req = mock(PostCreateRequest.class);
        when(req.getPostType()).thenReturn(PostType.FREE);
        when(req.getCourseIdx()).thenReturn(null);
        when(req.getLectureIdx()).thenReturn(null);
        when(req.toEntity(any(User.class), isNull(), isNull())).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostDetailResponse result = communityService.createPost(userDetails, req);

        assertNotNull(result);
    }

    // ============================
    // updatePost
    // ============================

    @Test
    @DisplayName("게시글 작성자가 수정 성공")
    void updatePost_작성자_수정_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE)
                .title("원본").text("요약").content("내용").user(userEntity).build();
        setTimestamps(post);

        PostUpdateRequest req = mock(PostUpdateRequest.class);
        when(req.getPostType()).thenReturn(PostType.FREE);
        when(req.getCourseIdx()).thenReturn(null);
        when(req.getLectureIdx()).thenReturn(null);
        when(postRepository.findByIdWithUserAndComments(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.countByPost(post)).thenReturn(0L);
        when(postScrapRepository.existsByUserAndPost(any(User.class), eq(post))).thenReturn(false);

        PostDetailResponse result = communityService.updatePost(userDetails, 1L, req);

        assertNotNull(result);
    }

    @Test
    @DisplayName("타인이 게시글 수정 시 예외")
    void updatePost_타인_수정_예외() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE)
                .title("원본").user(userEntity).build();
        when(postRepository.findByIdWithUserAndComments(1L)).thenReturn(Optional.of(post));

        BaseException ex = assertThrows(BaseException.class,
                () -> communityService.updatePost(otherUserDetails, 1L, mock(PostUpdateRequest.class)));
        assertEquals(POST_UNAUTHORIZED, ex.getStatus());
    }

    @Test
    @DisplayName("관리자가 NOTICE 게시글 수정 성공")
    void updatePost_관리자_NOTICE_수정_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.NOTICE)
                .title("공지").text("요약").content("내용").user(userEntity).build();
        setTimestamps(post);

        PostUpdateRequest req = mock(PostUpdateRequest.class);
        when(req.getPostType()).thenReturn(PostType.NOTICE);
        when(req.getCourseIdx()).thenReturn(null);
        when(req.getLectureIdx()).thenReturn(null);
        when(postRepository.findByIdWithUserAndComments(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.countByPost(post)).thenReturn(0L);
        when(postScrapRepository.existsByUserAndPost(any(User.class), eq(post))).thenReturn(false);

        PostDetailResponse result = communityService.updatePost(adminDetails, 1L, req);

        assertNotNull(result);
    }

    // ============================
    // deletePost
    // ============================

    @Test
    @DisplayName("게시글 작성자가 삭제 성공")
    void deletePost_작성자_삭제_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE)
                .title("제목").user(userEntity).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        communityService.deletePost(userDetails, 1L);

        verify(postScrapRepository).deleteAllByPost(post);
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("타인이 게시글 삭제 시 예외")
    void deletePost_타인_삭제_예외() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE)
                .title("제목").user(userEntity).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        BaseException ex = assertThrows(BaseException.class,
                () -> communityService.deletePost(otherUserDetails, 1L));
        assertEquals(POST_UNAUTHORIZED, ex.getStatus());
    }

    // ============================
    // createComment
    // ============================

    @Test
    @DisplayName("댓글 작성 성공")
    void createComment_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE).user(userEntity).build();
        Comment comment = Comment.builder().idx(1L).text("댓글 요약").content("댓글 내용")
                .user(userEntity).post(post).build();
        setTimestamps(comment);

        CommentCreateRequest req = mock(CommentCreateRequest.class);
        when(req.getPostIdx()).thenReturn(1L);
        when(req.toEntity(any(User.class), eq(post))).thenReturn(comment);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse result = communityService.createComment(userDetails, req);

        assertNotNull(result);
        verify(commentRepository).save(any(Comment.class));
    }

    // ============================
    // updateComment
    // ============================

    @Test
    @DisplayName("댓글 타인 수정 시 예외")
    void updateComment_타인_수정_예외() {
        Comment comment = Comment.builder().idx(1L).text("원본").user(userEntity).build();
        CommentUpdateRequest req = mock(CommentUpdateRequest.class);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        BaseException ex = assertThrows(BaseException.class,
                () -> communityService.updateComment(otherUserDetails, 1L, req));
        assertEquals(COMMENT_UNAUTHORIZED, ex.getStatus());
    }

    // ============================
    // deleteComment
    // ============================

    @Test
    @DisplayName("작성자가 댓글 삭제 성공")
    void deleteComment_성공() {
        Comment comment = Comment.builder().idx(1L).text("댓글").user(userEntity).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        communityService.deleteComment(userDetails, 1L);

        verify(commentRepository).delete(comment);
    }

    // ============================
    // scrap / unscrap / toggleScrap
    // ============================

    @Test
    @DisplayName("스크랩 성공")
    void scrap_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE).user(userEntity).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.existsByUserAndPost(any(User.class), eq(post))).thenReturn(false);

        communityService.scrap(userDetails, 1L);

        verify(postScrapRepository).save(any(PostScrap.class));
    }

    @Test
    @DisplayName("중복 스크랩 시 예외")
    void scrap_중복_예외() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE).user(userEntity).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.existsByUserAndPost(any(User.class), eq(post))).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class,
                () -> communityService.scrap(userDetails, 1L));
        assertEquals(SCRAP_ALREADY_EXISTS, ex.getStatus());
    }

    @Test
    @DisplayName("스크랩 취소 성공")
    void unscrap_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE).user(userEntity).build();
        PostScrap scrap = PostScrap.builder().idx(1L).user(userEntity).post(post).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByUserAndPost(any(User.class), eq(post))).thenReturn(Optional.of(scrap));

        communityService.unscrap(userDetails, 1L);

        verify(postScrapRepository).delete(scrap);
    }

    @Test
    @DisplayName("toggleScrap - 스크랩 없을 때 추가 (true 반환)")
    void toggleScrap_추가() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE).user(userEntity).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByUserAndPost(any(User.class), eq(post))).thenReturn(Optional.empty());

        boolean result = communityService.toggleScrap(userDetails, 1L);

        assertTrue(result);
        verify(postScrapRepository).save(any(PostScrap.class));
    }

    @Test
    @DisplayName("toggleScrap - 스크랩 있을 때 취소 (false 반환)")
    void toggleScrap_취소() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE).user(userEntity).build();
        PostScrap scrap = PostScrap.builder().idx(1L).user(userEntity).post(post).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByUserAndPost(any(User.class), eq(post))).thenReturn(Optional.of(scrap));

        boolean result = communityService.toggleScrap(userDetails, 1L);

        assertFalse(result);
        verify(postScrapRepository).delete(scrap);
    }
}
