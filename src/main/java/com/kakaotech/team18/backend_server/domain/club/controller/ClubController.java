package com.kakaotech.team18.backend_server.domain.club.controller;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class ClubController{

    private final ClubService clubService;

    @GetMapping
    public ResponseEntity<List<ClubListResponseDto>> getAllClubs() {
        List<ClubListResponseDto> response = clubService.getAllClubs();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<ClubDetailResponseDto> getClub(@PathVariable Long clubId) {
        ClubDetailResponseDto response = clubService.getClubDetail(clubId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/category")
    public ResponseEntity<List<ClubListResponseDto>> listClubsByCategory(
            @RequestParam(required=false)String category
    ){
        List<ClubListResponseDto> response = clubService.getClubByCategory(Category.valueOf(category));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}/dashboard")
    public ResponseEntity<ClubDashBoardResponseDto> getClubDashboard(@PathVariable Long clubId) {
        ClubDashBoardResponseDto response = clubService.getClubDashBoard(clubId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}/dashboard/applicants")
    public ResponseEntity<List<ApplicantResponseDto>> getClubApplicants(
            @PathVariable Long clubId,
            @RequestParam(required = false) String status) {
        List<ApplicantResponseDto> response = clubService.getApplicantsByStatus(clubId,
                Status.valueOf(status));
        return ResponseEntity.ok(response);
    }
}
