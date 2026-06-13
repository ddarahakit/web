package com.ddarahakit.backend.domain.course.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * CourseLevel(enum, 영문 코드) ↔ DB varchar(한글 라벨 초급/중급/고급) 변환기.
 *
 * 도입 배경: DB course.level 은 한글로 저장돼 있는데 {@code @Enumerated(STRING)} 은
 * 컬럼 문자열을 enum 이름(BEGINNER 등)으로 valueOf 하려다 IllegalArgumentException 으로
 * 모든 course 조회가 500 이 났다. 컨버터로 한글 라벨을 안전하게 매핑한다.
 *
 * - DB 저장:   enum → description(한글). null 은 null 유지.
 * - DB 조회:   한글 라벨/영문 코드/미지값을 방어적으로 enum(or null) 으로 매핑(예외 미발생).
 */
@Converter
public class CourseLevelConverter implements AttributeConverter<CourseLevel, String> {

    @Override
    public String convertToDatabaseColumn(CourseLevel attribute) {
        return attribute == null ? null : attribute.getDescription();
    }

    @Override
    public CourseLevel convertToEntityAttribute(String dbData) {
        return CourseLevel.fromDbLabel(dbData);
    }
}
