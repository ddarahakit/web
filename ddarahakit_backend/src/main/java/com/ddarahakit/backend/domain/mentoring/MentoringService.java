package com.ddarahakit.backend.domain.mentoring;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.mentoring.model.*;
import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;

@RequiredArgsConstructor
@Service
public class MentoringService {
    private final MentoringSessionRepository sessionRepository;
    private final MentoringMessageRepository messageRepository;
    private final UserRepository userRepository;

    public MentoringDto.SessionListRes list(AuthUserDetails authUserDetails, int page, int size, String keyword, MentoringStatus status) {
        Page<MentoringSession> sessions = sessionRepository.findForUser(
                authUserDetails.toEntity(),
                status,
                normalizeKeyword(keyword),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );
        return MentoringDto.SessionListRes.of(sessions);
    }

    public MentoringDto.SessionDetailRes detail(AuthUserDetails authUserDetails, Long sessionIdx) {
        MentoringSession session = findSessionForUser(authUserDetails, sessionIdx);
        Page<MentoringMessage> messages = messageRepository.findBySessionOrderByIdxDesc(
                session, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "idx"))
        );
        return MentoringDto.SessionDetailRes.of(session, messages);
    }

    public MentoringDto.MessagePageRes messages(AuthUserDetails authUserDetails, Long sessionIdx, int page, int size, Long beforeIdx) {
        MentoringSession session = findSessionForUser(authUserDetails, sessionIdx);
        Page<MentoringMessage> messages;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "idx"));
        if (beforeIdx != null && beforeIdx > 0) {
            messages = messageRepository.findBySessionAndIdxLessThanOrderByIdxDesc(session, beforeIdx, pageable);
        } else {
            messages = messageRepository.findBySessionOrderByIdxDesc(session, pageable);
        }
        return MentoringDto.MessagePageRes.of(messages);
    }

    @Transactional
    public MentoringDto.SessionRes createSession(AuthUserDetails authUserDetails, MentoringDto.CreateSessionReq dto) {
        if ("ROLE_MENTOR".equals(authUserDetails.getRole())) {
            throw BaseException.of(INVALID_USER_ROLE);
        }

        User mentor = userRepository.findById(dto.getMentorIdx()).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );
        if (!"ROLE_MENTOR".equals(mentor.getRole())) {
            throw BaseException.of(MENTORING_INVALID_MENTOR);
        }

        User mentee = userRepository.findById(authUserDetails.getIdx()).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );

        MentoringSession session = MentoringSession.builder()
                .mentor(mentor)
                .mentee(mentee)
                .subject(dto.getSubject())
                .scheduledAt(dto.getScheduledAt())
                .status(MentoringStatus.OPEN)
                .build();

        session = sessionRepository.save(session);

        if (dto.getMessage() != null && !dto.getMessage().isBlank()) {
            MentoringMessage message = MentoringMessage.builder()
                    .session(session)
                    .sender(mentee)
                    .message(dto.getMessage())
                    .build();
            messageRepository.save(message);
            session.updateLastMessage(dto.getMessage(), LocalDateTime.now());
        }

        session.markReadByMentee(LocalDateTime.now());
        sessionRepository.save(session);

        return MentoringDto.SessionRes.of(session);
    }

    @Transactional
    public MentoringDto.SessionRes markAsRead(AuthUserDetails authUserDetails, Long sessionIdx) {
        MentoringSession session = findSessionForUser(authUserDetails, sessionIdx);
        LocalDateTime now = LocalDateTime.now();
        if (session.getMentor().getIdx().equals(authUserDetails.getIdx())) {
            session.markReadByMentor(now);
        } else {
            session.markReadByMentee(now);
        }
        sessionRepository.save(session);
        return MentoringDto.SessionRes.of(session);
    }

    @Transactional
    public MentoringDto.MessageRes sendMessage(AuthUserDetails authUserDetails, Long sessionIdx, MentoringDto.SendMessageReq dto) {
        MentoringSession session = findSessionForUser(authUserDetails, sessionIdx);

        if (session.getStatus() == MentoringStatus.CLOSED) {
            throw BaseException.of(MENTORING_SESSION_CLOSED);
        }

        User sender = userRepository.findById(authUserDetails.getIdx()).orElseThrow(
                () -> BaseException.of(RESPONSE_NULL_ERROR)
        );

        MentoringMessage message = MentoringMessage.builder()
                .session(session)
                .sender(sender)
                .message(dto.getMessage())
                .build();
        message = messageRepository.save(message);

        session.updateLastMessage(dto.getMessage(), LocalDateTime.now());
        sessionRepository.save(session);

        return MentoringDto.MessageRes.of(message);
    }

    @Transactional
    public MentoringDto.SessionRes close(AuthUserDetails authUserDetails, Long sessionIdx) {
        MentoringSession session = findSessionForUser(authUserDetails, sessionIdx);
        if (session.getStatus() != MentoringStatus.CLOSED) {
            session.close(LocalDateTime.now());
            sessionRepository.save(session);
        }
        return MentoringDto.SessionRes.of(session);
    }

    private MentoringSession findSessionForUser(AuthUserDetails authUserDetails, Long sessionIdx) {
        MentoringSession session = sessionRepository.findById(sessionIdx).orElseThrow(
                () -> BaseException.of(MENTORING_NOT_FOUND)
        );
        Long userIdx = authUserDetails.getIdx();
        if (!session.getMentor().getIdx().equals(userIdx) && !session.getMentee().getIdx().equals(userIdx)) {
            throw BaseException.of(MENTORING_FORBIDDEN);
        }
        return session;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
