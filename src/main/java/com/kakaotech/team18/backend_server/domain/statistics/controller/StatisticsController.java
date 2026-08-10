package com.kakaotech.team18.backend_server.domain.statistics.controller;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 지원자 통계 공개 API.
 * <p>
 * 지원폼 단위로 집계하며, 지원자에게 공개되므로 <strong>비로그인 상태에서도 조회할 수 있다.</strong>
 * ({@code SecurityConfig}에 permitAll로 등록되어 있다)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/club-apply-forms/{clubApplyFormId}")
@Tag(name = "Statistics", description = "지원자 통계 API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(
            summary = "지원폼 단위 지원자 통계 조회",
            description = """
                    지원폼에 접수된 지원자의 성별·학번·학부 분포와 일자별 지원 추이를 반환합니다.
                    비로그인 상태에서도 조회할 수 있습니다.

                    - `dimensions`를 생략하면 모든 항목을 반환합니다.
                    - 응답에는 지원자를 식별할 수 있는 값(이름·이메일·전화번호·6자리 학번)이 포함되지 않습니다.
                    - 재식별 방지를 위해 **전체 지원자 수가 최소 공개 기준 미만이면** 분포를 비공개합니다.
                      이때 `masked=true`, `results=[]`로 내려갑니다. 이는 다음과 구분됩니다:
                      `masked=true`(소수라 비공개) vs 버킷 `count:0`(실제 0명) vs `미입력` 버킷(값 미기입).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = StatisticsResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "모집 진행 중", value = """
                                    {
                                      "clubApplyFormId": 12,
                                      "totalApplicants": 214,
                                      "snapshot": false,
                                      "masked": false,
                                      "calculatedAt": "2026-03-14T23:59:30+09:00",
                                      "results": [
                                        {
                                          "dimension": "GENDER",
                                          "type": "CATEGORICAL",
                                          "buckets": [
                                            { "key": "MALE", "label": "남성", "count": 121, "ratio": 0.565 },
                                            { "key": "FEMALE", "label": "여성", "count": 91, "ratio": 0.425 },
                                            { "key": "UNKNOWN", "label": "미입력", "count": 2, "ratio": 0.010 }
                                          ]
                                        },
                                        {
                                          "dimension": "ADMISSION_YEAR",
                                          "type": "ORDINAL",
                                          "buckets": [
                                            { "key": "22", "label": "22학번", "count": 58, "ratio": 0.271 }
                                          ]
                                        },
                                        {
                                          "dimension": "FACULTY",
                                          "type": "CATEGORICAL",
                                          "buckets": [
                                            { "key": "ENGINEERING", "label": "공과대학", "count": 74, "ratio": 0.346 },
                                            { "key": "NATURAL_SCIENCES", "label": "자연과학대학", "count": 37, "ratio": 0.173 },
                                            { "key": "ETC", "label": "기타", "count": 20, "ratio": 0.093 }
                                          ]
                                        },
                                        {
                                          "dimension": "DAILY_APPLICATIONS",
                                          "type": "TIME_SERIES",
                                          "buckets": [
                                            { "key": "2026-03-02", "label": "3월 2일", "count": 12 }
                                          ]
                                        }
                                      ]
                                    }
                                    """),
                                    @ExampleObject(name = "최소 공개 기준 미달(비공개)", value = """
                                    {
                                      "clubApplyFormId": 12,
                                      "totalApplicants": 2,
                                      "snapshot": false,
                                      "masked": true,
                                      "calculatedAt": "2026-03-14T23:59:30+09:00",
                                      "results": []
                                    }
                                    """)
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 dimension", content = @Content),
            @ApiResponse(responseCode = "404", description = "지원폼을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponseDto> getStatistics(
            @Parameter(description = "지원폼 ID", example = "12")
            @PathVariable Long clubApplyFormId,

            @Parameter(description = "조회할 집계 항목. 생략하면 전체를 반환합니다.",
                    example = "GENDER,FACULTY")
            @RequestParam(name = "dimensions", required = false) List<String> dimensions
    ) {
        // 관리자 컨트롤러와 동일하게 dimension 파싱은 StatisticsDimension.resolve()로 일원화한다.
        List<StatisticsDimension> requested = StatisticsDimension.resolve(dimensions);

        log.info("지원자 통계 조회 clubApplyFormId={}, dimensions={}", clubApplyFormId, requested);

        return ResponseEntity.ok(statisticsService.getStatistics(clubApplyFormId, requested));
    }
}
