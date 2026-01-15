package com.kakaotech.team18.backend_server.domain.notices.controller;

import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeCreateRequestDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.service.NoticeService;
import com.kakaotech.team18.backend_server.global.security.CustomSecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NoticeControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoticeService noticeService;

    @MockitoBean
    private CustomSecurityService customSecurityService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/notices - SYSTEM_ADMIN 권한 성공")
    void createNotice_asSystemAdmin_success() throws Exception {
        // given
        when(customSecurityService.isSystemAdmin()).thenReturn(true);

        NoticeResponseDto response = new NoticeResponseDto(
                1L,
                "Test Notice",
                "Test Content",
                LocalDateTime.now(),
                "관리자",
                "admin@test.com",
                List.of()
        );

        when(noticeService.createNotice(any(NoticeCreateRequestDto.class), anyList(), any()))
                .thenReturn(response);

        MockMultipartFile titlePart = new MockMultipartFile(
                "title",
                "",
                "text/plain",
                "Test Notice".getBytes()
        );

        MockMultipartFile contentPart = new MockMultipartFile(
                "content",
                "",
                "text/plain",
                "Test Content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/notices")
                        .file(titlePart)
                        .file(contentPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/api/notices/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Notice"))
                .andExpect(jsonPath("$.content").value("Test Content"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notices - 권한 없음 (403)")
    void createNotice_notSystemAdmin_forbidden() throws Exception {
        // given
        when(customSecurityService.isSystemAdmin()).thenReturn(false);

        MockMultipartFile titlePart = new MockMultipartFile(
                "title",
                "",
                "text/plain",
                "Test Notice".getBytes()
        );

        MockMultipartFile contentPart = new MockMultipartFile(
                "content",
                "",
                "text/plain",
                "Test Content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/notices")
                        .file(titlePart)
                        .file(contentPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/notices - 인증 없음 (401)")
    void createNotice_unauthenticated_unauthorized() throws Exception {
        // given
        MockMultipartFile titlePart = new MockMultipartFile(
                "title",
                "",
                "text/plain",
                "Test Notice".getBytes()
        );

        MockMultipartFile contentPart = new MockMultipartFile(
                "content",
                "",
                "text/plain",
                "Test Content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/notices")
                        .file(titlePart)
                        .file(contentPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notices - 파일 첨부 성공")
    void createNotice_withFiles_success() throws Exception {
        // given
        when(customSecurityService.isSystemAdmin()).thenReturn(true);

        NoticeResponseDto response = new NoticeResponseDto(
                1L,
                "Test Notice with Files",
                "Test Content",
                LocalDateTime.now(),
                "관리자",
                "admin@test.com",
                List.of(
                        new NoticeResponseDto.FileDetail(
                                1L,
                                "test.pdf",
                                "https://presigned-url.com/test.pdf",
                                "https://bucket.s3.region.amazonaws.com/attachments/uuid-test.pdf"
                        )
                )
        );

        when(noticeService.createNotice(any(NoticeCreateRequestDto.class), anyList(), any()))
                .thenReturn(response);

        MockMultipartFile titlePart = new MockMultipartFile(
                "title",
                "",
                "text/plain",
                "Test Notice with Files".getBytes()
        );

        MockMultipartFile contentPart = new MockMultipartFile(
                "content",
                "",
                "text/plain",
                "Test Content".getBytes()
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files",
                "test.pdf",
                "application/pdf",
                "test pdf content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/notices")
                        .file(titlePart)
                        .file(contentPart)
                        .file(filePart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.file").isArray())
                .andExpect(jsonPath("$.file[0].name").value("test.pdf"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notices - 필수 필드 누락 (400)")
    void createNotice_missingRequiredFields_badRequest() throws Exception {
        // given
        when(customSecurityService.isSystemAdmin()).thenReturn(true);

        // title만 있고 content 없음
        MockMultipartFile titlePart = new MockMultipartFile(
                "title",
                "",
                "text/plain",
                "Test Notice".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/notices")
                        .file(titlePart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notices - 빈 제목 (400)")
    void createNotice_blankTitle_badRequest() throws Exception {
        // given
        when(customSecurityService.isSystemAdmin()).thenReturn(true);

        MockMultipartFile titlePart = new MockMultipartFile(
                "title",
                "",
                "text/plain",
                "".getBytes()  // 빈 제목
        );

        MockMultipartFile contentPart = new MockMultipartFile(
                "content",
                "",
                "text/plain",
                "Test Content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/notices")
                        .file(titlePart)
                        .file(contentPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}
