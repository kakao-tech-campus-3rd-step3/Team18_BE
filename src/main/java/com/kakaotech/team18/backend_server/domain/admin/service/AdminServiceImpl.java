package com.kakaotech.team18.backend_server.domain.admin.service;

import com.kakaotech.team18.backend_server.domain.admin.dto.LinkClubRequestDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.DuplicateClubAdminException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.DuplicateClubMemberException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidClubNameException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidStudentIdException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;

    @Override
    public SuccessResponseDto linkClubPresident(LinkClubRequestDto requestDto) {

        User user = userRepository.findByStudentId(requestDto.studentId())
                .orElseThrow(() -> new InvalidStudentIdException("등록되지 않은 학번입니다. 학번 : "+ requestDto.studentId()));
        log.info("User checked successfully");

        Club club = clubRepository.findByName(requestDto.clubName())
                .orElseThrow(() -> new InvalidClubNameException("해당 이름의 동아리가 존재하지 않습니다. 동아리 :"+ requestDto.clubName()));
        log.info("Club checked successfully");

        if (clubMemberRepository.existsByUserAndClubAndClubRoleAndActiveStatus(user, club, requestDto.role(), ActiveStatus.ACTIVE)) {
             throw new DuplicateClubMemberException("이미 해당 동아리원이 존재합니다. 이름 : "+user.getName()+"동아리 : "+club.getName());
        }
        log.info("duplicate ClubMember checked successfully");

        if (requestDto.role() == Role.CLUB_ADMIN){
            if (clubMemberRepository.existsByClubAndRole(club, requestDto.role())) {
                throw new DuplicateClubAdminException("이미 해당 동아리의 회장이 존재합니다. 동아리 : "+club.getName());
            }
        }
        log.info("duplicate president checked successfully");

        ClubMember clubMember = ClubMember.builder()
                .user(user)
                .club(club)
                .application(null)
                .role(requestDto.role())
                .activeStatus(ActiveStatus.ACTIVE)
                .build();

        clubMemberRepository.save(clubMember);
        log.info("ClubMember added successfully");

        return new SuccessResponseDto(true);
    }
}