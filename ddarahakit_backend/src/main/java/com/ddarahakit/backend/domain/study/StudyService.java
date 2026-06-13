package com.ddarahakit.backend.domain.study;

import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.course.repository.LectureCompleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 주간 학습활동 집계 (lecture_complete.completed_at 기반).
 *
 * 정의(가정 — 응답/주석에 명시):
 *  - "출석/학습일" = 그 날짜에 강의를 1개 이상 수강완료한 날.
 *  - days        = 최근 7일(오늘 포함) 일별 수강완료 강의 수.
 *  - streakDays  = 오늘(완료가 있으면) 또는 어제부터 거꾸로 연속으로 학습한 일 수.
 *  - goalRate    = min(100, 최근7일 총 완료수 / WEEKLY_GOAL * 100). WEEKLY_GOAL=5(임의 기준).
 *
 * ⚠️ completed_at 은 신규 컬럼이라 과거 수강완료(NULL)는 집계에서 제외된다(신규 데이터 기준 정확).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    /** 주간 목표 강의 수(비즈니스 정책 미정 → 임의 기본값). */
    private static final int WEEKLY_GOAL = 5;
    private static final int WEEK_DAYS = 7;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LectureCompleteRepository lectureCompleteRepository;

    public StudyDto.WeeklyRes weekly(AuthUserDetails authUserDetails) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(WEEK_DAYS - 1L).atStartOfDay(); // 최근 7일(오늘 포함)

        List<LocalDateTime> completedAts = lectureCompleteRepository
                .findCompletedAtByUserSince(authUserDetails.getIdx(), from);

        // 일별 카운트(최근 7일, 0 포함)
        Map<LocalDate, Long> countByDay = new TreeMap<>();
        for (int i = WEEK_DAYS - 1; i >= 0; i--) {
            countByDay.put(today.minusDays(i), 0L);
        }
        long weeklyCompleted = 0;
        Set<LocalDate> studiedDays = new HashSet<>(); // streak 계산용(7일 범위 내 학습일)
        for (LocalDateTime at : completedAts) {
            LocalDate d = at.toLocalDate();
            if (countByDay.containsKey(d)) {
                countByDay.merge(d, 1L, Long::sum);
                weeklyCompleted++;
            }
            studiedDays.add(d);
        }

        List<StudyDto.DayCount> days = new ArrayList<>();
        countByDay.forEach((d, c) -> days.add(StudyDto.DayCount.builder()
                .date(d.format(DATE_FMT)).count(c).build()));

        // 연속 학습일: 오늘 학습했으면 오늘부터, 아니면 어제부터 거꾸로 연속된 학습일 수
        int streak = 0;
        LocalDate cursor = studiedDays.contains(today) ? today : today.minusDays(1);
        while (studiedDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        int goalRate = (int) Math.min(100, Math.round(weeklyCompleted * 100.0 / WEEKLY_GOAL));

        return StudyDto.WeeklyRes.builder()
                .days(days)
                .streakDays(streak)
                .weeklyCompleted(weeklyCompleted)
                .goalRate(goalRate)
                .weeklyGoal(WEEKLY_GOAL)
                .build();
    }
}
