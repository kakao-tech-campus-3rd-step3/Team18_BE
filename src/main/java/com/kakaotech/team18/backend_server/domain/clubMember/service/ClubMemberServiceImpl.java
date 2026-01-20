package com.kakaotech.team18.backend_server.domain.clubMember.service;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberServiceImpl implements ClubMemberService {

    private final ClubMemberProfileRepository clubMemberProfileRepository;

    @Override
    public List<ClubMemberResponseDto> getClubMembers(Long clubId) {
        return clubMemberProfileRepository.findAllByClubId(clubId).stream()
                .map(ClubMemberResponseDto::from)
                .toList();
    }
}
