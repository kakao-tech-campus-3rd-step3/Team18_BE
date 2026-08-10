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

        @Schema(description = """
                재식별 방지 최소 공개 기준(전체 지원자 수)에 미달해 분포를 비공개 처리했으면 true.
                true이면 results는 빈 배열이다. '지원자 0명'(results는 존재하고 버킷 count가 0)과는 구분된다.""",
                example = "false")
        boolean masked,

        @Schema(description = "집계가 수행된 시각", example = "2026-03-14T23:59:30+09:00")
        OffsetDateTime calculatedAt,

        @Schema(description = "dimension별 집계 결과. 최소 공개 기준에 미달(masked=true)하면 빈 배열이다.")
        List<DimensionResult> results
) {

    @Schema(description = "단일 dimension의 집계 결과")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DimensionResult(

            @Schema(description = "집계 항목", example = "GENDER")
            StatisticsDimension dimension,

            @Schema(description = "데이터 성격. 표현 방식은 프론트엔드가 정한다.", example = "CATEGORICAL")
            DimensionType type,

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

            @Schema(description = "해당 버킷의 지원자 수. 0이면 실제로 0명임을 뜻한다(비공개와 구분).", example = "121")
            long count,

            @Schema(description = "전체 대비 비율(소수점 3자리 고정). 시계열 등 비율이 의미 없는 버킷에서는 생략된다.", example = "0.565")
            BigDecimal ratio
    ) {
    }
}
