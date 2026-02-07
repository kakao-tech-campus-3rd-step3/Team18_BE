package com.kakaotech.team18.backend_server.domain.user.service;

import com.kakaotech.team18.backend_server.domain.user.dto.MyProfileResponseDto;
import com.kakaotech.team18.backend_server.domain.user.dto.UserActivityDto;

public interface UserService {
    MyProfileResponseDto getMyProfile(Long userId);

    UserActivityDto getMyActivities(Long userId);
}
