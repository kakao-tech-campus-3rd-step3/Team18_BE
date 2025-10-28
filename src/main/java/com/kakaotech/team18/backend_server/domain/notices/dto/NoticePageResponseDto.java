package com.kakaotech.team18.backend_server.domain.notices.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "공지사항 페이지 응답 DTO")
public record NoticePageResponseDto(

        @Schema(description = "공지사항 목록")
        List<NoticeBriefResponseDto> content,

        @Schema(description = "페이징 정보")
        PageInfo pageInfo
) {
        @Schema(description = "공지사항 간략 정보 DTO")
        public record NoticeBriefResponseDto(

                @Schema(description = "공지사항 ID", example = "1")
                Long id,

                @Schema(description = "공지사항 제목", example = "동아리 모집 안내")
                String title,

                @Schema(description = "공지 등록일시", example = "2025-03-01T10:00:00")
                LocalDateTime createdAt,

                @Schema(description = "작성자 이름", example = "관리자")
                String author
        ){}
        @Schema(description = "페이징 정보 DTO")
        public record PageInfo(

                @Schema(description = "현재 페이지", example = "1")
                Long currentPage,

                @Schema(description = "페이지 크기", example = "10")
                Long pageSize,

                @Schema(description = "전체 페이지 수", example = "5")
                Long totalPages,

                @Schema(description = "전체 공지 수", example = "47")
                Long totalElements
        ){}
}
