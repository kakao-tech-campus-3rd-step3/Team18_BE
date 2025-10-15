package com.kakaotech.team18.backend_server.domain.clubReview;

import com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewRequestDto;
import com.kakaotech.team18.backend_server.domain.clubReview.service.ClubReviewService;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "동아리 후기 API", description = "동아리 후기 정보 관련 API")
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubReviewController {

    private final ClubReviewService clubReviewService;

    @Operation(summary = "동아리 후기 등록", description = "사용자가 동아리 후기를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "후기 등록 성공"),
            @ApiResponse(responseCode = "401", description = "동아리에 등록된 학번이 일치하지 않습니다.")
    })
    @PostMapping("/{clubId}/reviews")
    public ResponseEntity<SuccessResponseDto> createClubReview(
            @PathVariable Long clubId,
            @Valid @RequestBody ClubReviewRequestDto request
    ) {
        SuccessResponseDto response = clubReviewService.createClubReview(clubId, request);
        return ResponseEntity.created(null).body(response); //TODO 조회 URI로 변경
    }
}
