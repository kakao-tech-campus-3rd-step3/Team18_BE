package com.kakaotech.team18.backend_server.domain.clubApplyForm.controller;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.service.ApplicationFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clubs/{clubId}")
public class ClubApplyFormController {

    private final ApplicationFormService applicationFormService;

    @GetMapping("/apply")
    public ResponseEntity<ClubApplyFormResponseDto> getClubById(
            @PathVariable("clubId") Long clubId
    ) {
        ClubApplyFormResponseDto response = applicationFormService.getQuestionForm(clubId);
        return ResponseEntity.ok(response);
    }
}
