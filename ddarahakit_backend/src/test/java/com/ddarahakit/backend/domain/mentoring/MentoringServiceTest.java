package com.ddarahakit.backend.domain.mentoring;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.mentoring.model.*;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentoringServiceTest {

    @Mock MentoringSessionRepository sessionRepository;
    @Mock MentoringMessageRepository messageRepository;
    @Mock UserRepository userRepository;
    @InjectMocks MentoringService mentoringService;

    AuthUserDetails menteeDetails;
    AuthUserDetails mentorDetails;
    AuthUserDetails otherDetails;
    User menteeEntity;
    User mentorEntity;
    User otherEntity;
    MentoringSession openSession;
    MentoringSession closedSession;

    @BeforeEach
    void setUp() {
        menteeDetails = AuthUserDetails.builder()
                .idx(1L).email("mentee@test.com").name("멘티")
                .password("pw").role("ROLE_USER").enabled(true).build();
        mentorDetails = AuthUserDetails.builder()
                .idx(2L).email("mentor@test.com").name("멘토")
                .password("pw").role("ROLE_MENTOR").enabled(true).build();
        otherDetails = AuthUserDetails.builder()
                .idx(99L).email("other@test.com").name("타인")
                .password("pw").role("ROLE_USER").enabled(true).build();

        menteeEntity = User.builder().idx(1L).email("mentee@test.com").name("멘티")
                .password("pw").role("ROLE_USER").enabled(true).build();
        mentorEntity = User.builder().idx(2L).email("mentor@test.com").name("멘토")
                .password("pw").role("ROLE_MENTOR").enabled(true).build();
        otherEntity = User.builder().idx(99L).email("other@test.com").name("타인")
                .password("pw").role("ROLE_USER").enabled(true).build();

        openSession = MentoringSession.builder()
                .idx(1L).subject("Java 멘토링").status(MentoringStatus.OPEN)
                .mentor(mentorEntity).mentee(menteeEntity).build();
        closedSession = MentoringSession.builder()
                .idx(2L).subject("종료된 세션").status(MentoringStatus.CLOSED)
                .mentor(mentorEntity).mentee(menteeEntity).build();
    }

    // ============================
    // createSession
    // ============================

    @Test
    @DisplayName("멘티가 멘토에게 세션 신청 성공")
    void createSession_멘티_신청_성공() {
        MentoringDto.CreateSessionReq req = mock(MentoringDto.CreateSessionReq.class);
        when(req.getMentorIdx()).thenReturn(2L);
        when(req.getSubject()).thenReturn("Java 멘토링");
        when(req.getScheduledAt()).thenReturn(null);
        when(req.getMessage()).thenReturn(null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(mentorEntity));
        when(userRepository.findById(1L)).thenReturn(Optional.of(menteeEntity));
        when(sessionRepository.save(any(MentoringSession.class))).thenReturn(openSession);

        MentoringDto.SessionRes result = mentoringService.createSession(menteeDetails, req);

        assertNotNull(result);
        verify(sessionRepository, atLeastOnce()).save(any(MentoringSession.class));
    }

    @Test
    @DisplayName("멘토가 세션 신청 시 예외 (ROLE_MENTOR 불가)")
    void createSession_멘토_신청_예외() {
        MentoringDto.CreateSessionReq req = mock(MentoringDto.CreateSessionReq.class);

        BaseException ex = assertThrows(BaseException.class,
                () -> mentoringService.createSession(mentorDetails, req));
        assertEquals(INVALID_USER_ROLE, ex.getStatus());
    }

    @Test
    @DisplayName("멘토가 아닌 사용자를 멘토로 지정 시 예외")
    void createSession_대상이_멘토아님_예외() {
        MentoringDto.CreateSessionReq req = mock(MentoringDto.CreateSessionReq.class);
        when(req.getMentorIdx()).thenReturn(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.of(otherEntity));

        BaseException ex = assertThrows(BaseException.class,
                () -> mentoringService.createSession(menteeDetails, req));
        assertEquals(MENTORING_INVALID_MENTOR, ex.getStatus());
    }

    // ============================
    // sendMessage
    // ============================

    @Test
    @DisplayName("OPEN 세션에 메시지 전송 성공")
    void sendMessage_성공() {
        MentoringDto.SendMessageReq req = mock(MentoringDto.SendMessageReq.class);
        when(req.getMessage()).thenReturn("안녕하세요!");

        MentoringMessage message = MentoringMessage.builder()
                .idx(1L).session(openSession).sender(menteeEntity).message("안녕하세요!").build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(openSession));
        when(userRepository.findById(1L)).thenReturn(Optional.of(menteeEntity));
        when(messageRepository.save(any(MentoringMessage.class))).thenReturn(message);
        when(sessionRepository.save(any(MentoringSession.class))).thenReturn(openSession);

        MentoringDto.MessageRes result = mentoringService.sendMessage(menteeDetails, 1L, req);

        assertNotNull(result);
        verify(messageRepository).save(any(MentoringMessage.class));
    }

    @Test
    @DisplayName("CLOSED 세션에 메시지 전송 시 예외")
    void sendMessage_CLOSED세션_예외() {
        MentoringDto.SendMessageReq req = mock(MentoringDto.SendMessageReq.class);
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(closedSession));

        BaseException ex = assertThrows(BaseException.class,
                () -> mentoringService.sendMessage(menteeDetails, 2L, req));
        assertEquals(MENTORING_SESSION_CLOSED, ex.getStatus());
    }

    // ============================
    // close
    // ============================

    @Test
    @DisplayName("세션 종료 성공")
    void close_성공() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(openSession));
        when(sessionRepository.save(any(MentoringSession.class))).thenReturn(openSession);

        MentoringDto.SessionRes result = mentoringService.close(menteeDetails, 1L);

        assertNotNull(result);
    }

    // ============================
    // markAsRead
    // ============================

    @Test
    @DisplayName("멘토가 읽음 처리 성공")
    void markAsRead_멘토_성공() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(openSession));
        when(sessionRepository.save(any(MentoringSession.class))).thenReturn(openSession);

        MentoringDto.SessionRes result = mentoringService.markAsRead(mentorDetails, 1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("멘티가 읽음 처리 성공")
    void markAsRead_멘티_성공() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(openSession));
        when(sessionRepository.save(any(MentoringSession.class))).thenReturn(openSession);

        MentoringDto.SessionRes result = mentoringService.markAsRead(menteeDetails, 1L);

        assertNotNull(result);
    }

    // ============================
    // 접근 권한 검사 (findSessionForUser)
    // ============================

    @Test
    @DisplayName("세션 참여자가 아닌 사용자 접근 시 예외")
    void findSessionForUser_권한없음_예외() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(openSession));

        BaseException ex = assertThrows(BaseException.class,
                () -> mentoringService.detail(otherDetails, 1L));
        assertEquals(MENTORING_FORBIDDEN, ex.getStatus());
    }

    // ============================
    // list
    // ============================

    @Test
    @DisplayName("멘토링 세션 목록 조회 성공")
    void list_성공() {
        Page<MentoringSession> sessionPage = new PageImpl<>(List.of(openSession),
                PageRequest.of(0, 10), 1);
        when(sessionRepository.findForUser(any(User.class), isNull(), isNull(), any()))
                .thenReturn(sessionPage);

        MentoringDto.SessionListRes result = mentoringService.list(menteeDetails, 0, 10, null, null);

        assertNotNull(result);
    }
}
