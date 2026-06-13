package com.ddarahakit.backend.domain.course.controller;


import com.ddarahakit.backend.common.model.BaseResponse;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.course.model.LectureDto;
import com.ddarahakit.backend.domain.course.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lecture")
@Tag(name = "강의 컨트롤러")
public class LectureController {
    private final LectureService lectureService;
    @Operation(
            summary = "강의 생성",
            description = "새로운 강의를 추가한다.")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<LectureDto.LectureRes>> lectureComplete(
            @RequestBody LectureDto.LectureReq dto) {
        LectureDto.LectureRes response = lectureService.lectureCreate(dto);

        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
