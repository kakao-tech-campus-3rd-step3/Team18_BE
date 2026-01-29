package com.kakaotech.team18.backend_server.domain.clubMember.service;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberDeleteResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberUpdateRequestDto;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ClubMemberService {
    List<ClubMemberResponseDto> getClubMembers(Long clubId);
    ClubMemberResponseDto registerMember(Long clubId, ClubMemberSaveRequestDto requestDto);
    void registerMembersByExcel(Long clubId, MultipartFile file) throws IOException;
    ClubMemberResponseDto updateMember(Long clubId, Long profileId, ClubMemberUpdateRequestDto requestDto);
    ClubMemberRoleUpdateResponseDto updateMemberRole(Long clubId, Long profileId, ClubMemberRoleUpdateRequestDto requestDto);
    ClubMemberDeleteResponseDto deleteMember(Long clubId, Long profileId);
}
