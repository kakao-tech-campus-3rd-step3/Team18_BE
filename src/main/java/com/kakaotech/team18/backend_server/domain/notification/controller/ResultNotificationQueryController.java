package com.kakaotech.team18.backend_server.domain.notification.controller;

import com.kakaotech.team18.backend_server.domain.notification.dto.ResultNotificationRequestSummariesResponse;
import com.kakaotech.team18.backend_server.domain.notification.service.ResultNotificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs/{clubId}/result-notifications")
public class ResultNotificationQueryController {

    private final ResultNotificationQueryService queryService;

    @GetMapping
    @Operation(
            summary = "결과 알림 요청별 발송 현황 조회",
            description = "최근 결과 발표 요청의 전체·대기·접수·성공·실패·미확정 건수를 조회합니다."
    )
    @PreAuthorize("@customSecurityService.isClubAdminOrExecutive(#clubId)")
    public ResultNotificationRequestSummariesResponse findRequestSummaries(
            @Parameter(description = "동아리 ID", example = "1")
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "1 이상이어야 합니다.")
            @Max(value = 100, message = "100 이하여야 합니다.") int limit
    ) {
        return queryService.findRequestSummaries(clubId, limit);
    }
}
