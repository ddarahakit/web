package com.ddarahakit.backend.domain.course.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 코스 난이도.
 * DB(ddarahakit.course.level, varchar)는 한글 라벨(초급/중급/고급)로 저장된다.
 * description = DB 저장값(한글 초급/중급/고급) 이며, {@code CourseLevelConverter} 가 enum ↔ 한글 라벨을 변환한다.
 * displayName = 화면 표시용 라벨(쉬움/보통/어려움) — DTO 의 levelDescription 으로 노출된다.
 * (영문 enum 이름은 API 계약의 코드 값으로만 노출)
 */
@Getter
@RequiredArgsConstructor
public enum CourseLevel {
    BEGINNER("초급", "쉬움"),
    INTERMEDIATE("중급", "보통"),
    ADVANCED("고급", "어려움");

    private final String description; // DB 저장 라벨(초급/중급/고급) — 컨버터 전용
    private final String displayName; // 화면 표시 라벨(쉬움/보통/어려움)

    /**
     * DB 한글 라벨 → enum. 미지/널/이미 영문 코드까지 방어적으로 처리해 조회가 500 나지 않게 한다.
     * 매칭 우선순위: DB 라벨(description)·표시 라벨(displayName) → enum 이름(BEGINNER 등) → 매칭 실패 시 null.
     */
    public static CourseLevel fromDbLabel(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        for (CourseLevel level : values()) {
            if (level.description.equals(value) || level.displayName.equals(value)) {
                return level;
            }
        }
        // 방어: DB 에 영문 코드(BEGINNER 등)나 대소문자 변형이 들어와 있어도 수용
        for (CourseLevel level : values()) {
            if (level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }
        return null; // 미지의 값은 예외 대신 null (조회 실패 방지)
    }
}
