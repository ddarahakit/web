package com.ddarahakit.backend.domain.user.service;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.community.CommentRepository;
import com.ddarahakit.backend.domain.community.PostRepository;
import com.ddarahakit.backend.domain.community.model.CommunityDto;
import com.ddarahakit.backend.domain.community.model.Post;
import com.ddarahakit.backend.domain.community.model.PostType;
import com.ddarahakit.backend.domain.course.model.Course;
import com.ddarahakit.backend.domain.course.model.CourseDto;
import com.ddarahakit.backend.domain.course.model.Lecture;
import com.ddarahakit.backend.domain.course.repository.LectureCompleteRepository;
import com.ddarahakit.backend.domain.course.repository.LectureRepository;
import com.ddarahakit.backend.domain.image.FileUploadService;
import com.ddarahakit.backend.domain.orders.OrdersItemRepository;
import com.ddarahakit.backend.domain.orders.model.OrdersItem;
import com.ddarahakit.backend.domain.review.ReviewRepository;
import com.ddarahakit.backend.domain.review.model.Review;
import com.ddarahakit.backend.domain.review.model.ReviewDto;
import com.ddarahakit.backend.domain.user.model.dto.UserDto;
import com.ddarahakit.backend.domain.user.model.entity.EmailVerify;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.user.repository.EmailVerifyRepository;
import com.ddarahakit.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ddarahakit.backend.common.Constants.*;
import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerifyRepository emailVerifyRepository;
    private final OrdersItemRepository ordersItemRepository;
    private final ReviewRepository reviewRepository;
    private final LectureCompleteRepository lectureCompleteRepository;
    private final LectureRepository lectureRepository;
    private final FileUploadService fileUploadService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 로그인
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return AuthUserDetails.of(user);
    }

    /**
     * 회원 가입
     */
    @Transactional
    public UserDto.SignupRes signup(UserDto.SignupReq dto) {
        /// 이메일 중복 확인
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw BaseException.of(DUPLICATE_USER_EMAIL);
        }

        /// 회원 정보 DB에 저장
        User user = dto.toEntity();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);

        /// 이메일 인증 메일 전송
        String uuid = UUID.randomUUID().toString();
        emailService.sendEmail(user.getEmail(), uuid, EMAIL_TYPE_SIGNUP);

        /// 이메일 인증 메일 정보 DB에 저장
        EmailVerify emailVerify = EmailVerify.builder()
                .user(user)
                .uuid(uuid)
                .type(EMAIL_TYPE_SIGNUP)
                .build();
        emailVerifyRepository.save(emailVerify);

        return UserDto.SignupRes.of(user);
    }

    /**
     * 회원 가입 후 이메일 인증
     */
    @Transactional
    public UserDto.SignupRes signupVerify(UserDto.SignupVerifyReq dto) {
        EmailVerify emailVerify = emailVerifyRepository.findByUuidAndType(dto.getUuid(), EMAIL_TYPE_SIGNUP).orElseThrow(
                () -> BaseException.of(EMAIL_VERIFY_NOT_FOUND)
        );

        User user = emailVerify.getUser();
        if (!user.getEmail().equals(dto.getEmail())) {
            throw BaseException.of(INVALID_USER_EMAIL);
        }

        user.setEnabled(true);
        userRepository.save(user);


        emailVerifyRepository.delete(emailVerify);

        return UserDto.SignupRes.of(user);
    }

    /**
     * 프로필
     */
    public UserDto.UserProfileRes getUserProfile(AuthUserDetails authUserDetails) {
        User user = userRepository.findById(authUserDetails.getIdx()).orElseThrow(
                () -> BaseException.of(USER_NOT_FOUND)
        );

        return UserDto.UserProfileRes.of(user);
    }

    /**
     * 주문 코스 조회
     */
    public List<CourseDto.CourseRes> getOrderedCourseList(AuthUserDetails authUserDetails) {
        User user = authUserDetails.toEntity();
        List<OrdersItem> items = ordersItemRepository.findByOrdersUserAndOrdersPaidTrueAndOrdersRefundedFalse(user);

        // 구매 코스 중복 제거(주문 순서 유지)
        List<Course> courses = items.stream()
                .collect(java.util.stream.Collectors.toMap(
                        item -> item.getCourse().getIdx(),
                        OrdersItem::getCourse,
                        (existing, replacement) -> existing,
                        java.util.LinkedHashMap::new
                ))
                .values().stream().toList();

        if (courses.isEmpty()) {
            return List.of();
        }

        List<Long> courseIdxList = courses.stream().map(Course::getIdx).toList();

        // 코스마다 (수강완료 1 + 최신완료 1 + 강의목록 1) 쿼리를 돌리던 N+1 을, 아래 2개의 일괄 쿼리로 대체.
        // 1) 사용자의 수강완료를 코스별 완료 강의 idx 목록으로 (단일 쿼리)
        java.util.Map<Long, List<Long>> completedLectureIdxByCourse = lectureCompleteRepository
                .findByUserAndCourseIdxIn(user, courseIdxList).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        lc -> lc.getCourse().getIdx(),
                        java.util.stream.Collectors.mapping(lc -> lc.getLecture().getIdx(), java.util.stream.Collectors.toList())
                ));

        // 2) 코스별 전체 강의를 idx 순으로 (단일 쿼리)
        java.util.Map<Long, List<Lecture>> lecturesByCourse = lectureRepository
                .findAllByCourseIdxInOrderByLectureIdxAsc(courseIdxList).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        lecture -> lecture.getSection().getCourse().getIdx(),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        return courses.stream()
                .map(course -> {
                    List<Long> completes = completedLectureIdxByCourse.getOrDefault(course.getIdx(), List.of());
                    Long nextLectureIdx = resolveNextLectureIdx(
                            lecturesByCourse.getOrDefault(course.getIdx(), List.of()), completes);
                    return CourseDto.CourseRes.of(course, nextLectureIdx, completes);
                })
                .toList();
    }

    /**
     * 이어듣기(다음 강의) idx 계산. CourseService.readNextLecture 와 동일 규칙을 메모리에서 처리한다.
     * - 강의가 없으면 0, 완료 없음/마지막까지 완료면 첫 강의, 그 외엔 최신 완료 다음 강의(없으면 첫 강의).
     */
    private Long resolveNextLectureIdx(List<Lecture> orderedLectures, List<Long> completedLectureIdxList) {
        if (orderedLectures.isEmpty()) {
            return 0L;
        }
        long maxCompleted = completedLectureIdxList.stream().mapToLong(Long::longValue).max().orElse(0L);
        Long lastIdx = orderedLectures.get(orderedLectures.size() - 1).getIdx();
        if (maxCompleted == 0L || lastIdx.equals(maxCompleted)) {
            return orderedLectures.get(0).getIdx();
        }
        return orderedLectures.stream()
                .map(Lecture::getIdx)
                .filter(idx -> idx > maxCompleted)
                .findFirst()
                .orElse(orderedLectures.get(0).getIdx());
    }

    /**
     * 내 리뷰 조회
     */
    public List<ReviewDto.MyReviewRes> getMyReviewList(AuthUserDetails authUserDetails) {
        List<Review> reviews = reviewRepository.findByUser(authUserDetails.toEntity());


        return reviews.stream().map(ReviewDto.MyReviewRes::of).toList();
    }

    /**
     * 내 질문 조회
     */
    public List<CommunityDto.PostSummaryResponse> getMyQuestionList(AuthUserDetails authUserDetails) {
        List<Post> posts = postRepository.findByUserAndPostTypeWithCourse(authUserDetails.toEntity(), PostType.QUESTION);
        return toSummariesWithCommentCount(posts);
    }

    /**
     * 내 게시글 조회
     */
    public List<CommunityDto.PostSummaryResponse> getMyPostList(AuthUserDetails authUserDetails) {
        List<PostType> excludeTypes = List.of(PostType.QUESTION, PostType.NOTICE);

        List<Post> posts = postRepository.findByUserAndPostTypeNotInWithCourse(authUserDetails.toEntity(), excludeTypes);
        return toSummariesWithCommentCount(posts);
    }

    /**
     * 게시글 목록 → 요약 DTO 변환. 댓글 수는 일괄 COUNT 로 집계해
     * 게시글마다 comments 컬렉션(LONGTEXT 본문 포함)을 로딩하던 낭비를 제거한다.
     */
    private List<CommunityDto.PostSummaryResponse> toSummariesWithCommentCount(List<Post> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }
        java.util.Map<Long, Long> commentCountMap = commentRepository.countByPostIn(posts).stream()
                .collect(java.util.stream.Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        return posts.stream()
                .map(post -> CommunityDto.PostSummaryResponse.from(
                        post, 0L, false, commentCountMap.getOrDefault(post.getIdx(), 0L)))
                .toList();
    }

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserDto.UserProfileRes updateUserProfile(AuthUserDetails authUserDetails, UserDto.UserProfileReq dto) {
        User user = userRepository.findById(authUserDetails.getIdx()).orElseThrow(
                () -> BaseException.of(USER_NOT_FOUND)
        );

        user.updateProfile(dto);
        userRepository.save(user);

        return UserDto.UserProfileRes.of(user);
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserDto.UserProfileRes updateUserProfile(AuthUserDetails authUserDetails, MultipartFile multipartFile) {
        User user = userRepository.findById(authUserDetails.getIdx()).orElseThrow(
                () -> BaseException.of(USER_NOT_FOUND)
        );

        String savedFileName = fileUploadService.uploadProfile(multipartFile);

        user.updateProfileImage(savedFileName);
        userRepository.save(user);

        return UserDto.UserProfileRes.of(user);
    }

    /**
     * 비밀번호 찾기 이메일 요청
     */
    @Transactional
    public void resetPasswordEmailReq(UserDto.ResetPasswordEmailReq dto) {
        // 사용자 열거(enumeration) 방지: 미가입 이메일도 동일하게 성공 응답을 반환하고,
        // 실제 가입된 경우에만 메일 발송/토큰 저장을 수행한다.
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();

        String uuid = UUID.randomUUID().toString();
        emailService.sendEmail(dto.getEmail(), uuid, EMAIL_TYPE_PASSWORD_RESET);
        EmailVerify emailVerify = EmailVerify.builder()
                .user(user)
                .uuid(uuid)
                .type(EMAIL_TYPE_PASSWORD_RESET)
                .build();
        emailVerifyRepository.save(emailVerify);
    }

    /**
     * 비밀번호 찾기 후 비밀번호 변경
     */
    @Transactional
    public UserDto.SignupRes resetPassword(UserDto.ResetPasswordReq dto) {
        EmailVerify emailVerify = emailVerifyRepository.findByUuidAndType(dto.getUuid(), EMAIL_TYPE_PASSWORD_RESET).orElseThrow(
                () -> BaseException.of(EMAIL_VERIFY_NOT_FOUND)
        );

        User user = emailVerify.getUser();
        if (!user.getEmail().equals(dto.getEmail())) {
            throw BaseException.of(INVALID_USER_EMAIL);
        }

        Date createdAt = emailVerify.getCreatedAt();
        Date now = new Date();

        if (now.getTime() - createdAt.getTime() > EMAIL_RESET_TIMEOUT) {
            throw BaseException.of(INVALID_EMAIL_RESET_TIMEOUT);
        }

        if (!dto.getNewPassword1().equals(dto.getNewPassword2())) {
            throw BaseException.of(INVALID_USER_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword1()));
        User updatedUser = userRepository.save(user);

        emailVerifyRepository.delete(emailVerify);

        return UserDto.SignupRes.of(updatedUser);
    }

    /**
     * 로그인 한 상태에서 비밀번호 변경
     */
    @Transactional
    public UserDto.SignupRes updatePassword(AuthUserDetails authUserDetails, UserDto.UpdatePasswordReq dto) {
        User user = userRepository.findById(authUserDetails.getIdx()).orElseThrow(
                () -> BaseException.of(USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(dto.getOriginPassword(), user.getPassword()) ||
                !dto.getNewPassword1().equals(dto.getNewPassword2())
        ) {
            throw BaseException.of(INVALID_USER_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword1()));
        User updatedUser = userRepository.save(user);

        return UserDto.SignupRes.of(updatedUser);
    }

    /**
     * UUID 만료 시간 확인
     */
    public boolean checkUuidExpired(String email, String uuid) {
        EmailVerify emailVerify = emailVerifyRepository.findByUuid(uuid).orElseThrow(
                () -> BaseException.of(EMAIL_VERIFY_NOT_FOUND)
        );

        if (!emailVerify.getUser().getEmail().equals(email)) {
            throw BaseException.of(INVALID_USER_EMAIL);
        }

        Date createdAt = emailVerify.getCreatedAt();
        Date now = new Date();

        if (now.getTime() - createdAt.getTime() > EMAIL_RESET_TIMEOUT) {
            throw BaseException.of(INVALID_EMAIL_RESET_TIMEOUT);
        }

        return true;
    }
}
