package com.kakaotech.team18.backend_server.domain.clubPopularity.controller;

import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityRecordingService;
import com.kakaotech.team18.backend_server.global.security.ActivityTrackingFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "실시간 인기 동아리 API", description = "동아리 조회·활성 상태 기록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class ClubPopularityController {

    private final ClubPopularityRecordingService recordingService;

    @Operation(summary = "동아리 상세 조회 기록", description = "동아리 상세 페이지 진입을 기록합니다.")
    @PostMapping("/{clubId}/views")
    public ResponseEntity<Void> recordView(
            @PathVariable long clubId,
            Authentication authentication,
            @RequestAttribute(value = ActivityTrackingFilter.ANONYMOUS_ID_REQUEST_ATTRIBUTE, required = false)
            String anonymousId) {
        recordingService.recordView(clubId, authentication, anonymousId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "동아리 현재 활성 상태 갱신", description = "동아리 상세 페이지의 현재 활성 상태를 갱신합니다.")
    @PostMapping("/{clubId}/heartbeat")
    public ResponseEntity<Void> recordHeartbeat(
            @PathVariable long clubId,
            Authentication authentication,
            @RequestAttribute(value = ActivityTrackingFilter.ANONYMOUS_ID_REQUEST_ATTRIBUTE, required = false)
            String anonymousId) {
        recordingService.recordHeartbeat(clubId, authentication, anonymousId);
        return ResponseEntity.noContent().build();
    }
}
