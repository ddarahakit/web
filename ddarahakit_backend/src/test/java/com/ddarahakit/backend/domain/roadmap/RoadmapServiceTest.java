package com.ddarahakit.backend.domain.roadmap;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.domain.course.repository.CourseRepository;
import com.ddarahakit.backend.domain.roadmap.model.Roadmap;
import com.ddarahakit.backend.domain.roadmap.model.RoadmapDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapServiceTest {

    @Mock RoadmapRepository roadmapRepository;
    @Mock CourseRepository courseRepository;
    @InjectMocks RoadmapService roadmapService;

    Roadmap roadmap;

    @BeforeEach
    void setUp() {
        roadmap = Roadmap.builder().idx(1L).name("자바 로드맵").description("자바 학습 경로").build();
    }

    @Test
    @DisplayName("로드맵 목록 조회 성공")
    void list_성공() {
        when(roadmapRepository.findAll()).thenReturn(List.of(roadmap));

        List<RoadmapDto.RoadmapListRes> result = roadmapService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("자바 로드맵", result.get(0).getName());
    }

    @Test
    @DisplayName("로드맵 상세 조회 성공")
    void detail_성공() {
        when(roadmapRepository.findByIdWithCourses(1L)).thenReturn(Optional.of(roadmap));

        RoadmapDto.RoadmapDetailRes result = roadmapService.detail(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("없는 ID 상세 조회 시 예외")
    void detail_없는ID_예외() {
        when(roadmapRepository.findByIdWithCourses(99L)).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> roadmapService.detail(99L));
        assertEquals(ROADMAP_NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("로드맵 생성 성공")
    void create_성공() {
        RoadmapDto.CreateReq dto = RoadmapDto.CreateReq.builder()
                .name("신규 로드맵").description("설명").courses(null).build();
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(roadmap);

        RoadmapDto.RoadmapDetailRes result = roadmapService.create(dto);

        assertNotNull(result);
        verify(roadmapRepository).save(any(Roadmap.class));
    }

    @Test
    @DisplayName("로드맵 수정 성공")
    void update_성공() {
        RoadmapDto.UpdateReq dto = RoadmapDto.UpdateReq.builder()
                .name("수정된 로드맵").description("수정 설명").courses(null).build();
        when(roadmapRepository.findByIdWithCourses(1L)).thenReturn(Optional.of(roadmap));
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(roadmap);

        RoadmapDto.RoadmapDetailRes result = roadmapService.update(1L, dto);

        assertNotNull(result);
    }

    @Test
    @DisplayName("없는 ID 수정 시 예외")
    void update_없는ID_예외() {
        RoadmapDto.UpdateReq dto = RoadmapDto.UpdateReq.builder()
                .name("수정").courses(null).build();
        when(roadmapRepository.findByIdWithCourses(99L)).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> roadmapService.update(99L, dto));
        assertEquals(ROADMAP_NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("로드맵 삭제 성공")
    void delete_성공() {
        when(roadmapRepository.findById(1L)).thenReturn(Optional.of(roadmap));

        roadmapService.delete(1L);

        verify(roadmapRepository).delete(roadmap);
    }

    @Test
    @DisplayName("없는 ID 삭제 시 예외")
    void delete_없는ID_예외() {
        when(roadmapRepository.findById(99L)).thenReturn(Optional.empty());

        BaseException ex = assertThrows(BaseException.class,
                () -> roadmapService.delete(99L));
        assertEquals(ROADMAP_NOT_FOUND, ex.getStatus());
    }
}
