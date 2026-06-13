package com.ddarahakit.backend.domain.community.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostType {
    QUESTION("질문"),
    FREE("자유주제"),
    CAREER("커리어/이직"),
    NOTICE("공지사항");

    private final String description;
}
