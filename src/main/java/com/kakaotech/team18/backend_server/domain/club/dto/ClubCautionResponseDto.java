package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.CautionItem;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubCaution;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.List;
import lombok.Builder;

@Schema(description = "동아리 지원 유의사항 응답 데이터")
@Builder
public record ClubCautionResponseDto(
        @Schema(description = "유의사항 순서", example = "1") Integer displayOrder,
        @Schema(description = "유의사항 제목", example = "지원 자격 확인") String title,
        @Schema(description = "유의사항 내용 목록") List<CautionItemResponseDto> items
        ) {
    public static ClubCautionResponseDto from(ClubCaution clubCaution) {
        return ClubCautionResponseDto.builder()
                .displayOrder(clubCaution.getDisplayOrder())
                .title(clubCaution.getTitle())
                .items(clubCaution.getItems().stream()
                        .sorted(Comparator.comparing(CautionItem::getDisplayOrder))
                        .map(CautionItemResponseDto::from)
                        .toList())
                .build();
    }
}
