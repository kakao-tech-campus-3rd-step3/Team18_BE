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
                    지원폼에 접수된 지원자의 성별·학번·학과 분포와 일자별 지원 추이를 반환합니다.
                    비로그인 상태에서도 조회할 수 있습니다.

                    - `dimensions`를 생략하면 모든 항목을 반환합니다.
                    - 소수 버킷은 재식별 방지를 위해 '기타'로 병합되거나 비공개 처리됩니다.
                    - 누적 지원자 수가 최소 공개 기준에 미달하면 `results`가 빈 배열로 나갑니다.
                    - **마감 직전 5분 동안은 지원 현황을 공개하지 않습니다.** 이 구간에는 `totalApplicants`가
                      생략되고 `results`가 빈 배열이며 `notice`에 사유가 담깁니다. 마감 후에는 다시 공개됩니다.
                    - 응답에는 지원자를 식별할 수 있는 값(이름·이메일·전화번호·6자리 학번)이 포함되지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = StatisticsResponseDto.class),
                            examples = @ExampleObject(name = "모집 진행 중", value = """
                                    {
                                      "clubApplyFormId": 12,
                                      "totalApplicants": 214,
                                      "snapshot": false,
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
                                            { "key": "2022", "label": "22학번", "count": 58, "ratio": 0.271 }
                                          ]
                                        },
                                        {
                                          "dimension": "DEPARTMENT",
                                          "type": "CATEGORICAL",
                                          "truncated": true,
                                          "notice": "학과는 자유 입력이라 같은 학과가 표기 차이로 나뉘어 집계될 수 있습니다. 순위와 비율은 참고용으로만 사용하세요.",
                                          "buckets": [
                                            { "key": "컴퓨터공학과", "label": "컴퓨터공학과", "count": 37, "ratio": 0.173 },
                                            { "key": "OTHERS", "label": "기타", "count": 44, "distinctValues": 23 }
                                          ]
                                        },
                                        {
                                          "dimension": "DAILY_APPLICATIONS",
                                          "type": "TIME_SERIES",
                                          "buckets": [
                                            { "key": "2026-03-02", "label": "3월 2일", "count": 4 }
                                          ]
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "마감 직전 비공개 구간",
                    content = @Content(
                            schema = @Schema(implementation = StatisticsResponseDto.class),
                            examples = @ExampleObject(name = "마감 5분 전", value = """
                                    {
                                      "clubApplyFormId": 12,
                                      "snapshot": false,
                                      "calculatedAt": "2026-03-14T23:56:00+09:00",
                                      "results": [],
                                      "notice": "마감 직전에는 지원 현황을 공개하지 않습니다. 마감 후 최종 결과가 공개됩니다."
                                    }
                                    """)
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
                    example = "GENDER,DEPARTMENT")
            @RequestParam(name = "dimensions", required = false) List<String> dimensions
    ) {
        List<StatisticsDimension> requested = resolveDimensions(dimensions);

        log.info("지원자 통계 조회 clubApplyFormId={}, dimensions={}", clubApplyFormId, requested);

        return ResponseEntity.ok(statisticsService.getStatistics(clubApplyFormId, requested));
    }

    /**
     * 쿼리 파라미터를 dimension 목록으로 변환합니다. 지정하지 않으면 전체를 조회합니다.
     * <p>
     * 문자열을 직접 파싱하는 이유는, 정의되지 않은 값이 들어왔을 때 사용 가능한 목록까지 담은 400을 돌려주기
     * 위해서다. 컨트롤러 파라미터를 Enum으로 바로 바인딩하면 어떤 값이 왜 틀렸는지 알기 어렵다.
     */
    private List<StatisticsDimension> resolveDimensions(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return StatisticsDimension.defaults();
        }
        return raw.stream()
                .map(StatisticsDimension::from)
                .distinct()
                .toList();
    }
}
