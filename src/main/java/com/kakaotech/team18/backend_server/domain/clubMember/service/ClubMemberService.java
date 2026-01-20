package com.kakaotech.team18.backend_server.domain.clubMember.service;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import java.util.List;

public interface ClubMemberService {
    List<ClubMemberResponseDto> getClubMembers(Long clubId);
}
