package com.kakaotech.team18.backend_server.domain.applicationForm.controller;

import com.kakaotech.team18.backend_server.domain.applicationForm.dto.ApplicationFormResponseDto;
import com.kakaotech.team18.backend_server.domain.applicationForm.service.ApplicationFormService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/clubs/{clubId}")
public class ApplicationFormController {

    private final ApplicationFormService applicationFormService;

    @GetMapping("/apply")
    public ResponseEntity<ApplicationFormResponseDto> getClubById(
            @PathVariable("clubId") Long clubId
    ) {
        ApplicationFormResponseDto response = applicationFormService.getQuestionForm(clubId);
        return ResponseEntity.ok(response);
    }
}
