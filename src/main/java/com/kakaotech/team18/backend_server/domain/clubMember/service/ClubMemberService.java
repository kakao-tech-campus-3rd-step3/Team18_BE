package com.kakaotech.team18.backend_server.domain.clubMember.service;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import java.util.List;

public interface ClubMemberService {
    List<ClubMemberResponseDto> getClubMembers(Long clubId);
    ClubMemberResponseDto registerMember(Long clubId, ClubMemberSaveRequestDto requestDto);
}
