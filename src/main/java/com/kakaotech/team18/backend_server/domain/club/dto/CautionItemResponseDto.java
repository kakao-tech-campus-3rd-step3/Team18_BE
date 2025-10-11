package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.CautionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "동아리 지원 유의사항 내용 응답 데이터")
@Builder
public record CautionItemResponseDto(
        @Schema(description = "유의사항 내용 순서", example = "1")Integer displayOrder,
        @Schema(description = "유의사항 내용", example = "해당 학기 재학중인 학생만 지원 가능합니다.") String content
) {
    public static CautionItemResponseDto from(CautionItem cautionItem) {
        return CautionItemResponseDto.builder()
                .content(cautionItem.getContent())
                .displayOrder(cautionItem.getDisplayOrder())
                .build();
    }
}
