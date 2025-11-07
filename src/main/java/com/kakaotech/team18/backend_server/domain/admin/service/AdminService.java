package com.kakaotech.team18.backend_server.domain.admin.service;

import com.kakaotech.team18.backend_server.domain.admin.dto.LinkClubRequestDto;

public interface AdminService {
    void linkClubPresident(LinkClubRequestDto requestDto);
}
