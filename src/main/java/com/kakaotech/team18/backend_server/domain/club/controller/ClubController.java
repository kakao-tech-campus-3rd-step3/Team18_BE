package com.kakaotech.team18.backend_server.domain.club.controller;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubResponse;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @GetMapping("/api/clubs/{clubId}")
    public HttpEntity<ClubResponse> getClub(@PathVariable long clubId) {
        ClubResponse response = clubService.getClubDetail(clubId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
