package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashboardApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import java.util.List;

public interface ClubService {

    ClubListResponseDto getClubByCategory(String category);

    ClubListResponseDto getClubByName(String name);

    ClubListResponseDto getAllClubs();

    ClubDetailResponseDto getClubDetail(Long clubId);

    ClubDashBoardResponseDto getClubDashBoard(Long clubId);

    ClubDashboardApplicantResponseDto getApplicantsByStatusAndStage(Long clubId, Status status, Stage stage);
}
