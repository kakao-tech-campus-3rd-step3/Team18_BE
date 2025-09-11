package com.kakaotech.team18.backend_server.domain.applicationForm.controller;

import com.kakaotech.team18.backend_server.domain.applicationForm.dto.ApplicationFormResponse;
import com.kakaotech.team18.backend_server.domain.applicationForm.service.ApplicationFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs/{clubId}")
public class ApplicationFormController {

    private ApplicationFormService applicationFormService;

    @Autowired
    public void setApplicationFormService(ApplicationFormService applicationFormService) {
        this.applicationFormService = applicationFormService;
    }

    @GetMapping("/apply")
    public ResponseEntity<ApplicationFormResponse> getClubById(
            @PathVariable("clubId") Long clubId
    ) {
        ApplicationFormResponse response = applicationFormService.getQuestionForm(clubId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
