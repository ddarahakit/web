package com.ddarahakit.backend.domain.user;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.community.PostRepository;
import com.ddarahakit.backend.domain.community.model.Post;
import com.ddarahakit.backend.domain.community.model.PostType;
import com.ddarahakit.backend.domain.course.service.CourseService;
import com.ddarahakit.backend.domain.image.FileUploadService;
import com.ddarahakit.backend.domain.orders.OrdersItemRepository;
import com.ddarahakit.backend.domain.review.ReviewRepository;
import com.ddarahakit.backend.domain.review.model.Review;
import com.ddarahakit.backend.domain.course.repository.LectureCompleteRepository;
import com.ddarahakit.backend.domain.user.model.dto.UserDto;
import com.ddarahakit.backend.domain.user.model.entity.EmailVerify;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.user.repository.EmailVerifyRepository;
import com.ddarahakit.backend.domain.user.repository.UserRepository;
import com.ddarahakit.backend.domain.user.service.EmailService;
import com.ddarahakit.backend.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.ddarahakit.backend.common.Constants.*;
import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @Mock EmailVerifyRepository emailVerifyRepository;
    @Mock OrdersItemRepository ordersItemRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock CourseService courseService;
    @Mock LectureCompleteRepository lectureCompleteRepository;
    @Mock FileUploadService fileUploadService;
    @Mock PostRepository postRepository;

    @InjectMocks UserService userService;

    AuthUserDetails userDetails;
    User userEntity;

    @BeforeEach
    void setUp() {
        userDetails = AuthUserDetails.builder()
                .idx(1L).email("user@test.com").name("테스터")
                .password("encoded_pw").role("ROLE_USER").enabled(true).build();

        userEntity = User.builder()
                .idx(1L).email("user@test.com").name("테스터")
                .password("encoded_pw").role("ROLE_USER").enabled(true)
                .provider("email").build();
    }

    // ============================
    // loadUserByUsername
    // ============================

    @Test
    @DisplayName("이메일로 유저 로드 성공")
    void loadUserByUsername_성공() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userEntity));

        UserDetails result = userService.loadUserByUsername("user@test.com");

        assertNotNull(result);
        assertEquals("user@test.com", result.getUsername());
    }

    @Test
    @DisplayName("이메일로 유저 로드 - 유저 없음 예외")
    void loadUserByUsername_유저없음_예외() {
        when(userRepository.findByEmail("notexist@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("notexist@test.com"));
    }

    // ============================
    // signup
    // ============================

    @Test
    @DisplayName("회원가입 성공 - 유저 저장 및 이메일 인증 메일 발송")
    void signup_성공() {
        UserDto.SignupReq req = mock(UserDto.SignupReq.class);
        when(req.getEmail()).thenReturn("user@test.com");
        when(req.toEntity()).thenReturn(userEntity);
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_pw");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        UserDto.SignupRes result = userService.signup(req);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmail(anyString(), anyString(), eq(EMAIL_TYPE_SIGNUP));
        verify(emailVerifyRepository).save(any(EmailVerify.class));
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복 예외")
    void signup_이메일중복_예외() {
        UserDto.SignupReq req = mock(UserDto.SignupReq.class);
        when(req.getEmail()).thenReturn("user@test.com");
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.signup(req));
        assertEquals(DUPLICATE_USER_EMAIL, ex.getStatus());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    // ============================
    // signupVerify
    // ============================

    @Test
    @DisplayName("이메일 인증 성공 - enabled=true 설정 및 EmailVerify 삭제")
    void signupVerify_성공() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("test-uuid").type(EMAIL_TYPE_SIGNUP).build();

        UserDto.SignupVerifyReq req = mock(UserDto.SignupVerifyReq.class);
        when(req.getUuid()).thenReturn("test-uuid");
        when(req.getEmail()).thenReturn("user@test.com");

        when(emailVerifyRepository.findByUuidAndType("test-uuid", EMAIL_TYPE_SIGNUP))
                .thenReturn(Optional.of(emailVerify));
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        UserDto.SignupRes result = userService.signupVerify(req);

        assertNotNull(result);
        assertTrue(userEntity.getEnabled());
        verify(emailVerifyRepository).delete(emailVerify);
    }

    @Test
    @DisplayName("이메일 인증 - UUID 없음 예외")
    void signupVerify_UUID없음_예외() {
        UserDto.SignupVerifyReq req = mock(UserDto.SignupVerifyReq.class);
        when(req.getUuid()).thenReturn("invalid-uuid");
        when(emailVerifyRepository.findByUuidAndType("invalid-uuid", EMAIL_TYPE_SIGNUP))
                .thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.signupVerify(req));
        assertEquals(RESPONSE_NULL_ERROR, ex.getStatus());
    }

    @Test
    @DisplayName("이메일 인증 - 이메일 불일치 예외")
    void signupVerify_이메일불일치_예외() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("test-uuid").type(EMAIL_TYPE_SIGNUP).build();

        UserDto.SignupVerifyReq req = mock(UserDto.SignupVerifyReq.class);
        when(req.getUuid()).thenReturn("test-uuid");
        when(req.getEmail()).thenReturn("wrong@test.com");

        when(emailVerifyRepository.findByUuidAndType("test-uuid", EMAIL_TYPE_SIGNUP))
                .thenReturn(Optional.of(emailVerify));

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.signupVerify(req));
        assertEquals(INVALID_USER_EMAIL, ex.getStatus());
    }

    // ============================
    // getUserProfile
    // ============================

    @Test
    @DisplayName("프로필 조회 성공")
    void getUserProfile_성공() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        UserDto.UserProfileRes result = userService.getUserProfile(userDetails);

        assertNotNull(result);
        assertEquals("user@test.com", result.getEmail());
        assertEquals("테스터", result.getName());
    }

    @Test
    @DisplayName("프로필 조회 - 유저 없음 예외")
    void getUserProfile_유저없음_예외() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.getUserProfile(userDetails));
        assertEquals(RESPONSE_NULL_ERROR, ex.getStatus());
    }

    // ============================
    // isEmailDuplicated
    // ============================

    @Test
    @DisplayName("이메일 중복 확인 - 중복 있음")
    void isEmailDuplicated_중복있음() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertTrue(userService.isEmailDuplicated("user@test.com"));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복 없음")
    void isEmailDuplicated_중복없음() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        assertFalse(userService.isEmailDuplicated("new@test.com"));
    }

    // ============================
    // updateUserProfile (dto)
    // ============================

    @Test
    @DisplayName("프로필 수정 성공")
    void updateUserProfile_dto_성공() {
        UserDto.UserProfileReq req = mock(UserDto.UserProfileReq.class);
        when(req.getName()).thenReturn("새이름");
        when(req.getIntroduction()).thenReturn("소개글");
        when(req.getProfileImageUrl()).thenReturn(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        UserDto.UserProfileRes result = userService.updateUserProfile(userDetails, req);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("프로필 수정 - 유저 없음 예외")
    void updateUserProfile_dto_유저없음_예외() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.updateUserProfile(userDetails, mock(UserDto.UserProfileReq.class)));
        assertEquals(RESPONSE_NULL_ERROR, ex.getStatus());
    }

    // ============================
    // updateUserProfile (multipart)
    // ============================

    @Test
    @DisplayName("프로필 이미지 수정 성공")
    void updateUserProfile_multipart_성공() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.jpg", "image/jpeg", "image-content".getBytes());

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(fileUploadService.uploadProfile(file)).thenReturn("/profile/profile.jpg");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        UserDto.UserProfileRes result = userService.updateUserProfile(userDetails, file);

        assertNotNull(result);
        verify(fileUploadService).uploadProfile(file);
        verify(userRepository).save(any(User.class));
    }

    // ============================
    // resetPasswordEmailReq
    // ============================

    @Test
    @DisplayName("비밀번호 재설정 이메일 요청 성공")
    void resetPasswordEmailReq_성공() {
        UserDto.ResetPasswordEmailReq req = mock(UserDto.ResetPasswordEmailReq.class);
        when(req.getEmail()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userEntity));

        userService.resetPasswordEmailReq(req);

        verify(emailService).sendEmail(eq("user@test.com"), anyString(), eq(EMAIL_TYPE_PASSWORD_RESET));
        verify(emailVerifyRepository).save(any(EmailVerify.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 요청 - 이메일 없음 예외")
    void resetPasswordEmailReq_이메일없음_예외() {
        UserDto.ResetPasswordEmailReq req = mock(UserDto.ResetPasswordEmailReq.class);
        when(req.getEmail()).thenReturn("notexist@test.com");
        when(userRepository.findByEmail("notexist@test.com")).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.resetPasswordEmailReq(req));
        assertEquals(RESPONSE_NULL_ERROR, ex.getStatus());
    }

    // ============================
    // resetPassword
    // ============================

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPassword_성공() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("reset-uuid").type(EMAIL_TYPE_PASSWORD_RESET).build();
        ReflectionTestUtils.setField(emailVerify, "createdAt", new Date());

        UserDto.ResetPasswordReq req = mock(UserDto.ResetPasswordReq.class);
        when(req.getUuid()).thenReturn("reset-uuid");
        when(req.getEmail()).thenReturn("user@test.com");
        when(req.getNewPassword1()).thenReturn("NewPass1!");
        when(req.getNewPassword2()).thenReturn("NewPass1!");

        when(emailVerifyRepository.findByUuidAndType("reset-uuid", EMAIL_TYPE_PASSWORD_RESET))
                .thenReturn(Optional.of(emailVerify));
        when(passwordEncoder.encode("NewPass1!")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        UserDto.SignupRes result = userService.resetPassword(req);

        assertNotNull(result);
        verify(emailVerifyRepository).delete(emailVerify);
    }

    @Test
    @DisplayName("비밀번호 재설정 - UUID 없음 예외")
    void resetPassword_UUID없음_예외() {
        UserDto.ResetPasswordReq req = mock(UserDto.ResetPasswordReq.class);
        when(req.getUuid()).thenReturn("invalid-uuid");
        when(emailVerifyRepository.findByUuidAndType("invalid-uuid", EMAIL_TYPE_PASSWORD_RESET))
                .thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.resetPassword(req));
        assertEquals(RESPONSE_NULL_ERROR, ex.getStatus());
    }

    @Test
    @DisplayName("비밀번호 재설정 - 이메일 불일치 예외")
    void resetPassword_이메일불일치_예외() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("reset-uuid").type(EMAIL_TYPE_PASSWORD_RESET).build();

        UserDto.ResetPasswordReq req = mock(UserDto.ResetPasswordReq.class);
        when(req.getUuid()).thenReturn("reset-uuid");
        when(req.getEmail()).thenReturn("wrong@test.com");

        when(emailVerifyRepository.findByUuidAndType("reset-uuid", EMAIL_TYPE_PASSWORD_RESET))
                .thenReturn(Optional.of(emailVerify));

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.resetPassword(req));
        assertEquals(INVALID_USER_EMAIL, ex.getStatus());
    }

    @Test
    @DisplayName("비밀번호 재설정 - 링크 만료 예외")
    void resetPassword_링크만료_예외() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("reset-uuid").type(EMAIL_TYPE_PASSWORD_RESET).build();
        // createdAt을 10분 전으로 설정 (EMAIL_RESET_TIMEOUT = 5분)
        ReflectionTestUtils.setField(emailVerify, "createdAt",
                new Date(System.currentTimeMillis() - 10 * 60 * 1000));

        UserDto.ResetPasswordReq req = mock(UserDto.ResetPasswordReq.class);
        when(req.getUuid()).thenReturn("reset-uuid");
        when(req.getEmail()).thenReturn("user@test.com");

        when(emailVerifyRepository.findByUuidAndType("reset-uuid", EMAIL_TYPE_PASSWORD_RESET))
                .thenReturn(Optional.of(emailVerify));

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.resetPassword(req));
        assertEquals(INVALID_EMAIL_RESET_TIMEOUT, ex.getStatus());
    }

    @Test
    @DisplayName("비밀번호 재설정 - 새 비밀번호 불일치 예외")
    void resetPassword_비밀번호불일치_예외() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("reset-uuid").type(EMAIL_TYPE_PASSWORD_RESET).build();
        ReflectionTestUtils.setField(emailVerify, "createdAt", new Date());

        UserDto.ResetPasswordReq req = mock(UserDto.ResetPasswordReq.class);
        when(req.getUuid()).thenReturn("reset-uuid");
        when(req.getEmail()).thenReturn("user@test.com");
        when(req.getNewPassword1()).thenReturn("NewPass1!");
        when(req.getNewPassword2()).thenReturn("Different1!");

        when(emailVerifyRepository.findByUuidAndType("reset-uuid", EMAIL_TYPE_PASSWORD_RESET))
                .thenReturn(Optional.of(emailVerify));

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.resetPassword(req));
        assertEquals(INVALID_USER_PASSWORD, ex.getStatus());
    }

    // ============================
    // updatePassword
    // ============================

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_성공() {
        UserDto.UpdatePasswordReq req = mock(UserDto.UpdatePasswordReq.class);
        when(req.getOriginPassword()).thenReturn("OldPass1!");
        when(req.getNewPassword1()).thenReturn("NewPass1!");
        when(req.getNewPassword2()).thenReturn("NewPass1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("OldPass1!", "encoded_pw")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        UserDto.SignupRes result = userService.updatePassword(userDetails, req);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호 불일치 예외")
    void updatePassword_현재비밀번호불일치_예외() {
        UserDto.UpdatePasswordReq req = mock(UserDto.UpdatePasswordReq.class);
        when(req.getOriginPassword()).thenReturn("WrongOld1!");
        when(req.getNewPassword1()).thenReturn("NewPass1!");
        when(req.getNewPassword2()).thenReturn("NewPass1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("WrongOld1!", "encoded_pw")).thenReturn(false);

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.updatePassword(userDetails, req));
        assertEquals(INVALID_USER_PASSWORD, ex.getStatus());
    }

    @Test
    @DisplayName("비밀번호 변경 - 새 비밀번호 불일치 예외")
    void updatePassword_새비밀번호불일치_예외() {
        UserDto.UpdatePasswordReq req = mock(UserDto.UpdatePasswordReq.class);
        when(req.getOriginPassword()).thenReturn("OldPass1!");
        when(req.getNewPassword1()).thenReturn("NewPass1!");
        when(req.getNewPassword2()).thenReturn("Different1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("OldPass1!", "encoded_pw")).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.updatePassword(userDetails, req));
        assertEquals(INVALID_USER_PASSWORD, ex.getStatus());
    }

    // ============================
    // checkUuidExpired
    // ============================

    @Test
    @DisplayName("UUID 만료 확인 - 유효한 UUID")
    void checkUuidExpired_유효() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("valid-uuid").type(EMAIL_TYPE_PASSWORD_RESET).build();
        ReflectionTestUtils.setField(emailVerify, "createdAt", new Date());

        when(emailVerifyRepository.findByUuid("valid-uuid")).thenReturn(Optional.of(emailVerify));

        boolean result = userService.checkUuidExpired("user@test.com", "valid-uuid");

        assertTrue(result);
    }

    @Test
    @DisplayName("UUID 만료 확인 - UUID 없음 예외")
    void checkUuidExpired_UUID없음_예외() {
        when(emailVerifyRepository.findByUuid("invalid-uuid")).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.checkUuidExpired("user@test.com", "invalid-uuid"));
        assertEquals(RESPONSE_NULL_ERROR, ex.getStatus());
    }

    @Test
    @DisplayName("UUID 만료 확인 - 이메일 불일치 예외")
    void checkUuidExpired_이메일불일치_예외() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("valid-uuid").type(EMAIL_TYPE_PASSWORD_RESET).build();

        when(emailVerifyRepository.findByUuid("valid-uuid")).thenReturn(Optional.of(emailVerify));

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.checkUuidExpired("wrong@test.com", "valid-uuid"));
        assertEquals(INVALID_USER_EMAIL, ex.getStatus());
    }

    @Test
    @DisplayName("UUID 만료 확인 - 링크 만료 예외")
    void checkUuidExpired_링크만료_예외() {
        EmailVerify emailVerify = EmailVerify.builder()
                .idx(1L).user(userEntity).uuid("expired-uuid").type(EMAIL_TYPE_PASSWORD_RESET).build();
        ReflectionTestUtils.setField(emailVerify, "createdAt",
                new Date(System.currentTimeMillis() - 10 * 60 * 1000));

        when(emailVerifyRepository.findByUuid("expired-uuid")).thenReturn(Optional.of(emailVerify));

        BaseException ex = assertThrows(BaseException.class,
                () -> userService.checkUuidExpired("user@test.com", "expired-uuid"));
        assertEquals(INVALID_EMAIL_RESET_TIMEOUT, ex.getStatus());
    }

    // ============================
    // getMyReviewList
    // ============================

    @Test
    @DisplayName("내 리뷰 목록 조회 성공")
    void getMyReviewList_성공() {
        when(reviewRepository.findByUser(any(User.class))).thenReturn(List.of());

        var result = userService.getMyReviewList(userDetails);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewRepository).findByUser(any(User.class));
    }

    // ============================
    // getMyQuestionList
    // ============================

    @Test
    @DisplayName("내 질문 목록 조회 성공")
    void getMyQuestionList_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.QUESTION)
                .title("질문").text("요약").content("내용").user(userEntity).build();
        ReflectionTestUtils.setField(post, "createdAt", new Date());
        ReflectionTestUtils.setField(post, "updatedAt", new Date());

        when(postRepository.findByUserAndPostTypeWithCourse(any(User.class), eq(PostType.QUESTION)))
                .thenReturn(List.of(post));

        var result = userService.getMyQuestionList(userDetails);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ============================
    // getMyPostList
    // ============================

    @Test
    @DisplayName("내 게시글 목록 조회 성공 (QUESTION, NOTICE 제외)")
    void getMyPostList_성공() {
        Post post = Post.builder().idx(1L).postType(PostType.FREE)
                .title("자유글").text("요약").content("내용").user(userEntity).build();
        ReflectionTestUtils.setField(post, "createdAt", new Date());
        ReflectionTestUtils.setField(post, "updatedAt", new Date());

        when(postRepository.findByUserAndPostTypeNotInWithCourse(any(User.class), anyList()))
                .thenReturn(List.of(post));

        var result = userService.getMyPostList(userDetails);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
