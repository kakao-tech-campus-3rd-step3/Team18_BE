package com.kakaotech.team18.backend_server.domain.admin.service;

import com.kakaotech.team18.backend_server.domain.admin.dto.LinkClubRequestDto;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;

public interface AdminService {
    SuccessResponseDto linkClubPresident(LinkClubRequestDto requestDto);
}
