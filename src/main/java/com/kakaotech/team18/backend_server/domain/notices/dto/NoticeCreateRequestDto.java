package com.kakaotech.team18.backend_server.domain.notices.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "공지사항 생성 요청 DTO")
public record NoticeCreateRequestDto(

    @Schema(description = "공지사항 제목", example = "2025년 1학기 동아리 모집 안내")
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @Schema(description = "공지사항 내용", example = "2025년 1학기 신입 부원 모집을 시작합니다...")
    @NotBlank(message = "내용은 필수입니다.")
    String content
) {}
