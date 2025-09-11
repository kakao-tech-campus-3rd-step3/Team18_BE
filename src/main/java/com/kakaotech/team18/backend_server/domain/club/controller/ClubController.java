package com.kakaotech.team18.backend_server.domain.club.controller;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ClubDetailResponseDto> getClub(@PathVariable long clubId) {
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


}
