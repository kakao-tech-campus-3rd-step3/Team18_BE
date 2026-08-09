package com.kakaotech.team18.backend_server.domain.statistics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kakaotech.team18.backend_server.domain.statistics.entity.DimensionType;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 지원자 통계 공개 응답.
 * <p>
 * <strong>이 DTO에는 지원자를 식별할 수 있는 값이 절대 들어가서는 안 된다.</strong> 이름·이메일·전화번호·지원서 ID는
 * 물론, 6자리 학번 전체도 포함하지 않는다. 학번은 앞 2자리에서 유도한 입학연도만 노출한다.
 */
@Schema(description = "지원폼 단위 지원자 통계")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StatisticsResponseDto(

        @Schema(description = "통계 대상 지원폼 ID", example = "12")
        Long clubApplyFormId,

        @Schema(description = "누적 지원자 수", example = "214")
        long totalApplicants,

        @Schema(description = "모집 종료 후 확정된 스냅샷이면 true, 진행 중 집계본이면 false", example = "false")
        boolean snapshot,

        @Schema(description = "집계가 수행된 시각", example = "2026-03-14T23:59:30+09:00")
        OffsetDateTime calculatedAt,

        @Schema(description = "dimension별 집계 결과. 최소 공개 기준에 미달하면 빈 배열이다.")
        List<DimensionResult> results
) {

    @Schema(description = "단일 dimension의 집계 결과")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DimensionResult(

            @Schema(description = "집계 항목", example = "GENDER")
            StatisticsDimension dimension,

            @Schema(description = "데이터 성격. 표현 방식은 프론트엔드가 정한다.", example = "CATEGORICAL")
            DimensionType type,

            @Schema(description = "상위 N개로 잘렸으면 true. 잘리지 않았으면 응답에서 생략된다.", example = "true")
            Boolean truncated,

            @Schema(description = "표기 파편화 등 해석 시 주의사항. 없으면 생략된다.")
            String notice,

            @Schema(description = "집계 버킷")
            List<Bucket> buckets
    ) {
    }

    @Schema(description = "집계 버킷 하나")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Bucket(

            @Schema(description = "표기 변경에 영향받지 않는 코드값", example = "MALE")
            String key,

            @Schema(description = "화면 표시용 문자열", example = "남성")
            String label,

            @Schema(description = "해당 버킷의 지원자 수", example = "121")
            long count,

            @Schema(description = "전체 대비 비율(소수점 3자리 고정). 시계열 등 비율이 의미 없는 버킷에서는 생략된다.", example = "0.565")
            BigDecimal ratio,

            @Schema(description = "'기타' 버킷에 병합된 원래 값의 종류 수. 그 외 버킷에서는 생략된다.", example = "23")
            Integer distinctValues
    ) {
    }
}
