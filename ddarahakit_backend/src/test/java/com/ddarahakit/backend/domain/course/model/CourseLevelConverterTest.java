package com.ddarahakit.backend.domain.course.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * CourseLevel ↔ DB 한글 라벨 변환기 단위 테스트.
 * 운영 500(No enum constant ...초급) 재발 방지: 한글값 매핑 + 방어 케이스.
 */
class CourseLevelConverterTest {

    private final CourseLevelConverter converter = new CourseLevelConverter();

    @Test
    @DisplayName("DB→enum: 한글 라벨이 enum 으로 매핑된다 (운영 500 케이스)")
    void toEntity_korean() {
        assertEquals(CourseLevel.BEGINNER, converter.convertToEntityAttribute("초급"));
        assertEquals(CourseLevel.INTERMEDIATE, converter.convertToEntityAttribute("중급"));
        assertEquals(CourseLevel.ADVANCED, converter.convertToEntityAttribute("고급"));
    }

    @Test
    @DisplayName("enum→DB: enum 이 한글 라벨로 저장된다")
    void toDb_korean() {
        assertEquals("초급", converter.convertToDatabaseColumn(CourseLevel.BEGINNER));
        assertEquals("중급", converter.convertToDatabaseColumn(CourseLevel.INTERMEDIATE));
        assertEquals("고급", converter.convertToDatabaseColumn(CourseLevel.ADVANCED));
    }

    @Test
    @DisplayName("왕복: 초급 ↔ BEGINNER")
    void roundTrip() {
        CourseLevel e = converter.convertToEntityAttribute("초급");
        assertEquals("초급", converter.convertToDatabaseColumn(e));
        assertEquals(CourseLevel.BEGINNER, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(CourseLevel.BEGINNER)));
    }

    @Test
    @DisplayName("방어: null/빈문자/공백/미지값 → null (예외로 500 안 남)")
    void toEntity_defensive_null() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute("   "));
        assertNull(converter.convertToEntityAttribute("최상급"));   // 미지의 한글
        assertNull(converter.convertToEntityAttribute("EXPERT"));   // 미지의 영문
    }

    @Test
    @DisplayName("방어: 영문 코드(BEGINNER)나 공백 포함도 수용")
    void toEntity_defensive_englishAndTrim() {
        assertEquals(CourseLevel.BEGINNER, converter.convertToEntityAttribute("BEGINNER"));
        assertEquals(CourseLevel.BEGINNER, converter.convertToEntityAttribute("beginner"));
        assertEquals(CourseLevel.BEGINNER, converter.convertToEntityAttribute(" 초급 "));
    }

    @Test
    @DisplayName("enum→DB: null 은 null 유지")
    void toDb_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }
}
