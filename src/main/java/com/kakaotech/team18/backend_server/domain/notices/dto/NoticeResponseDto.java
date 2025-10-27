package com.kakaotech.team18.backend_server.domain.notices.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "공지사항 상세 조회 응답 DTO")
public record NoticeResponseDto(

        @Schema(description = "공지사항 ID", example = "1")
        Long id,

        @Schema(description = "공지사항 제목", example = "동아리 모집 안내")
        String title,

        @Schema(description = "공지 내용", example = "3월 첫째 주부터 신입 부원을 모집합니다.")
        String content,

        @Schema(description = "공지 등록일시", example = "2025-03-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "작성자 이름", example = "관리자")
        String author,

        @Schema(description = "작성자 이메일", example = "admin@club.com")
        String email,

        @Schema(description = "첨부파일 이름", example = "notice_guide.pdf")
        String file
) { }