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
import com.kakaotech.team18.backend_server.global.exception.exceptions.DuplicateClubAdminException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.DuplicateClubMemberException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidClubNameException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidStudentIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubMemberRepository clubMemberRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @DisplayName("동아리장/임원 연결 - 성공")
    @Test
    void linkClubPresident_success() {
        // given
        String studentId = "202312345";
        String clubName = "인터엑스";
        Role role = Role.CLUB_EXECUTIVE;

        LinkClubRequestDto requestDto = new LinkClubRequestDto(studentId, clubName, role);

        User user = User.builder()
                .studentId(studentId)
                .name("홍길동")
                .build();

        Club club = Club.builder()
                .name(clubName)
                .build();

        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.of(user));
        when(clubRepository.findByName(clubName)).thenReturn(Optional.of(club));
        when(clubMemberRepository.existsByUserAndClubAndClubRoleAndActiveStatus(
                user, club, role, ActiveStatus.ACTIVE
        )).thenReturn(false);

        // when
        adminService.linkClubPresident(requestDto);

        // then
        ArgumentCaptor<ClubMember> captor = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubMemberRepository).save(captor.capture());

        ClubMember saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getClub()).isEqualTo(club);
        assertThat(saved.getRole()).isEqualTo(role);
        assertThat(saved.getActiveStatus()).isEqualTo(ActiveStatus.ACTIVE);
        assertThat(saved.getApplication()).isNull();

        verify(clubMemberRepository, never()).existsByClubAndRole(club, Role.CLUB_ADMIN);
    }

    @DisplayName("동아리장/임원 연결 - 학번이 존재하지 않으면 InvalidStudentIdException 발생")
    @Test
    void linkClubPresident_invalidStudentId() {
        // given
        String studentId = "999999999";
        String clubName = "인터엑스";

        LinkClubRequestDto requestDto = new LinkClubRequestDto(studentId, clubName, Role.CLUB_ADMIN);

        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.linkClubPresident(requestDto))
                .isInstanceOf(InvalidStudentIdException.class)
                .hasMessageContaining("등록되지 않은 학번입니다.");

        verify(clubRepository, never()).findByName(clubName);
        verify(clubMemberRepository, never()).save(any());
    }

    @DisplayName("동아리장/임원 연결 - 동아리 이름이 존재하지 않으면 InvalidClubNameException 발생")
    @Test
    void linkClubPresident_invalidClubName() {
        // given
        String studentId = "202312345";
        String clubName = "없는동아리";

        LinkClubRequestDto requestDto = new LinkClubRequestDto(studentId, clubName, Role.CLUB_ADMIN);

        User user = User.builder()
                .studentId(studentId)
                .name("홍길동")
                .build();

        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.of(user));
        when(clubRepository.findByName(clubName)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.linkClubPresident(requestDto))
                .isInstanceOf(InvalidClubNameException.class)
                .hasMessageContaining("해당 이름의 동아리가 존재하지 않습니다.");

        verify(clubMemberRepository, never()).save(any());
    }

    @DisplayName("동아리장/임원 연결 - 이미 동일한 ACTIVE 동아리원이 존재하면 DuplicateClubMemberException 발생")
    @Test
    void linkClubPresident_duplicateClubMember() {
        // given
        String studentId = "202312345";
        String clubName = "인터엑스";
        Role role = Role.CLUB_EXECUTIVE;

        LinkClubRequestDto requestDto = new LinkClubRequestDto(studentId, clubName, role);

        User user = User.builder()
                .studentId(studentId)
                .name("홍길동")
                .build();

        Club club = Club.builder()
                .name(clubName)
                .build();

        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.of(user));
        when(clubRepository.findByName(clubName)).thenReturn(Optional.of(club));
        when(clubMemberRepository.existsByUserAndClubAndClubRoleAndActiveStatus(
                user, club, role, ActiveStatus.ACTIVE
        )).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.linkClubPresident(requestDto))
                .isInstanceOf(DuplicateClubMemberException.class)
                .hasMessageContaining("이미 해당 동아리원이 존재합니다.");

        verify(clubMemberRepository, never()).save(any());
    }

    @DisplayName("동아리장 연결 - 회장(CLUB_ADMIN) 추가 시 이미 회장이 있으면 DuplicateClubAdminException 발생")
    @Test
    void linkClubPresident_duplicateClubAdmin() {
        // given
        String studentId = "202312345";
        String clubName = "인터엑스";
        Role role = Role.CLUB_ADMIN;

        LinkClubRequestDto requestDto = new LinkClubRequestDto(studentId, clubName, role);

        User user = User.builder()
                .studentId(studentId)
                .name("홍길동")
                .build();

        Club club = Club.builder()
                .name(clubName)
                .build();

        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.of(user));
        when(clubRepository.findByName(clubName)).thenReturn(Optional.of(club));
        when(clubMemberRepository.existsByUserAndClubAndClubRoleAndActiveStatus(
                user, club, role, ActiveStatus.ACTIVE
        )).thenReturn(false);
        when(clubMemberRepository.existsByClubAndRole(club, role)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.linkClubPresident(requestDto))
                .isInstanceOf(DuplicateClubAdminException.class)
                .hasMessageContaining("이미 해당 동아리의 회장이 존재합니다.");

        verify(clubMemberRepository, never()).save(any());
    }
}