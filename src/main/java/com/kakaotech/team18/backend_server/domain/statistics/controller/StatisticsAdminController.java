package com.kakaotech.team18.backend_server.domain.statistics.controller;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 지원자 통계 관리자 API.
 * <p>
 * 지원자를 직접 관리하는 동아리 관리자(CLUB_ADMIN/CLUB_EXECUTIVE)만 접근할 수 있으며, 공개 통계와 달리
 * 재식별 방지 마스킹·최소 공개 기준·마감 blackout을 적용하지 않고 원본 수치를 실시간으로 반환한다.
 * <p>
 * 경로가 {@code /statistics/admin}이라 공개 permitAll 매처({@code GET /api/club-apply-forms/*&#47;statistics},
 * 세그먼트 하나만 매치)에 걸리지 않고 인증 대상으로 떨어진다. 세부 인가는 {@link PreAuthorize}로 검사한다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/club-apply-forms/{clubApplyFormId}")
@Tag(name = "Statistics Admin", description = "지원자 통계 관리자 API")
public class StatisticsAdminController {

    private final StatisticsService statisticsService;

    @Operation(
            summary = "지원폼 단위 지원자 통계 조회 (관리자)",
            description = """
                    지원폼에 접수된 지원자의 성별·학번·학부 분포와 일자별 지원 추이를 마스킹 없이 반환합니다.
                    해당 동아리의 관리자(CLUB_ADMIN/CLUB_EXECUTIVE)만 조회할 수 있습니다.

                    - `dimensions`를 생략하면 모든 항목을 반환합니다.
                    - 공개 통계와 응답 형태는 동일하나, 재식별 방지 제약을 적용하지 않은 원본 수치입니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 dimension", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "해당 동아리 관리자 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "지원폼을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/statistics/admin")
    @PreAuthorize("@customSecurityService.isClubAdminOrExecutiveForApplyForm(#clubApplyFormId)")
    public ResponseEntity<StatisticsResponseDto> getStatisticsForAdmin(
            @Parameter(description = "지원폼 ID", example = "12")
            @PathVariable Long clubApplyFormId,

            @Parameter(description = "조회할 집계 항목. 생략하면 전체를 반환합니다.",
                    example = "GENDER,FACULTY")
            @RequestParam(name = "dimensions", required = false) List<String> dimensions
    ) {
        List<StatisticsDimension> requested = StatisticsDimension.resolve(dimensions);

        log.info("관리자 지원자 통계 조회 clubApplyFormId={}, dimensions={}", clubApplyFormId, requested);

        return ResponseEntity.ok(statisticsService.getStatisticsForAdmin(clubApplyFormId, requested));
    }
}
