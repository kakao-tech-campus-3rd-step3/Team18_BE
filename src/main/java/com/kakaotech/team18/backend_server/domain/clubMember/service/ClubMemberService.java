package com.kakaotech.team18.backend_server.domain.clubMember.service;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ClubMemberService {
    List<ClubMemberResponseDto> getClubMembers(Long clubId);
    ClubMemberResponseDto registerMember(Long clubId, ClubMemberSaveRequestDto requestDto);
    void registerMembersByExcel(Long clubId, MultipartFile file) throws IOException;
}
