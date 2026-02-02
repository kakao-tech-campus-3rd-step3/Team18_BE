package com.kakaotech.team18.backend_server.domain.clubMember.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMemberProfile;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberProfileRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CustomException;
import com.kakaotech.team18.backend_server.global.security.CustomSecurityService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClubMemberServiceTest {

    @InjectMocks
    private ClubMemberServiceImpl clubMemberService;

    @Mock
    private ClubMemberProfileRepository clubMemberProfileRepository;

    @Mock
    private ClubMemberRepository clubMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private CustomSecurityService customSecurityService;

    @Test
    @DisplayName("동아리원 목록 조회 성공 - 존재하는 동아리 ID")
    void getClubMembers_Success() {
        // given
        Long clubId = 1L;
        ClubMemberProfile profile1 = createProfile("이지훈", "20231234", Role.CLUB_EXECUTIVE);
        ClubMemberProfile profile2 = createProfile("김철수", "20245678", Role.CLUB_MEMBER);

        given(clubRepository.existsById(clubId)).willReturn(true);
        given(clubMemberProfileRepository.findAllByClubId(clubId)).willReturn(List.of(profile1, profile2));

        // when
        List<ClubMemberResponseDto> result = clubMemberService.getClubMembers(clubId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("이지훈");
        assertThat(result.get(0).role()).isEqualTo(Role.CLUB_EXECUTIVE);
        assertThat(result.get(1).name()).isEqualTo("김철수");
        
        verify(clubRepository, times(1)).existsById(clubId);
        verify(clubMemberProfileRepository, times(1)).findAllByClubId(clubId);
    }

    @Test
    @DisplayName("동아리원 목록 조회 실패 - 존재하지 않는 동아리 ID")
    void getClubMembers_Fail_ClubNotFound() {
        // given
        Long invalidClubId = 999L;
        given(clubRepository.existsById(invalidClubId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> clubMemberService.getClubMembers(invalidClubId))
                .isInstanceOf(ClubNotFoundException.class);
        
        verify(clubRepository, times(1)).existsById(invalidClubId);
        verify(clubMemberProfileRepository, times(0)).findAllByClubId(anyLong());
    }

    @Test
    @DisplayName("동아리원 수동 등록 성공 - 회장이 신규 등록 (Role 유지)")
    void registerMember_Success_New_Admin() {
        // given
        Long clubId = 1L;
        ClubMemberSaveRequestDto requestDto = createRequestDto("박신입", "241001", Role.CLUB_EXECUTIVE);
        
        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN); // 회장
        given(clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(clubId, requestDto.studentId()))
                .willReturn(Optional.empty()); // 신규
        given(clubRepository.findById(clubId)).willReturn(Optional.of(Club.builder().build()));
        given(userRepository.findByStudentId(requestDto.studentId())).willReturn(Optional.empty()); // User 없음 -> 껍데기 생성
        given(userRepository.save(any(User.class))).willReturn(User.builder().build());

        // when
        ClubMemberResponseDto result = clubMemberService.registerMember(clubId, requestDto);

        // then
        assertThat(result.name()).isEqualTo("박신입");
        assertThat(result.role()).isEqualTo(Role.CLUB_EXECUTIVE); // Role 유지됨
        
        verify(clubMemberRepository, times(1)).save(any(ClubMember.class));
    }

    @Test
    @DisplayName("동아리원 수동 등록 성공 - 운영진이 신규 등록 (Role 강등)")
    void registerMember_Success_New_Executive() {
        // given
        Long clubId = 1L;
        ClubMemberSaveRequestDto requestDto = createRequestDto("박신입", "241001", Role.CLUB_EXECUTIVE); // 운영진으로 요청했지만
        
        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_EXECUTIVE); // 요청자가 운영진
        given(clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(clubId, requestDto.studentId()))
                .willReturn(Optional.empty());
        given(clubRepository.findById(clubId)).willReturn(Optional.of(Club.builder().build()));
        given(userRepository.findByStudentId(requestDto.studentId())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(User.builder().build());

        // when
        ClubMemberResponseDto result = clubMemberService.registerMember(clubId, requestDto);

        // then
        assertThat(result.name()).isEqualTo("박신입");
        assertThat(result.role()).isEqualTo(Role.CLUB_MEMBER); // 일반부원으로 강등됨
        
        verify(clubMemberRepository, times(1)).save(any(ClubMember.class));
    }

    @Test
    @DisplayName("동아리원 수동 등록 성공 - 정보 업데이트 (이름 일치)")
    void registerMember_Success_Update() {
        // given
        Long clubId = 1L;
        ClubMemberSaveRequestDto requestDto = createRequestDto("이지훈", "20231234", Role.CLUB_MEMBER);
        ClubMemberProfile existingProfile = createProfile("이지훈", "20231234", Role.CLUB_MEMBER); // 이름 일치

        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN);
        given(clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(clubId, requestDto.studentId()))
                .willReturn(Optional.of(existingProfile));

        // when
        ClubMemberResponseDto result = clubMemberService.registerMember(clubId, requestDto);

        // then
        assertThat(result.name()).isEqualTo("이지훈");
        assertThat(result.studentId()).isEqualTo("20231234");
        
        verify(clubMemberRepository, times(0)).save(any(ClubMember.class)); // 저장이 아니라 업데이트
    }

    @Test
    @DisplayName("동아리원 수동 등록 실패 - 중복 충돌 (이름 불일치)")
    void registerMember_Fail_Conflict() {
        // given
        Long clubId = 1L;
        ClubMemberSaveRequestDto requestDto = createRequestDto("김기춘", "20231234", Role.CLUB_MEMBER); // 이름 다름
        ClubMemberProfile existingProfile = createProfile("이지훈", "20231234", Role.CLUB_MEMBER);

        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN);
        given(clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(clubId, requestDto.studentId()))
                .willReturn(Optional.of(existingProfile));

        // when & then
        assertThatThrownBy(() -> clubMemberService.registerMember(clubId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("동아리원 수동 등록 실패 - 동아리 없음")
    void registerMember_Fail_ClubNotFound() {
        // given
        Long clubId = 999L;
        ClubMemberSaveRequestDto requestDto = createRequestDto("박신입", "241001", Role.CLUB_MEMBER);

        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN);
        given(clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(clubId, requestDto.studentId()))
                .willReturn(Optional.empty());
        given(clubRepository.findById(clubId)).willReturn(Optional.empty()); // 동아리 없음

        // when & then
        assertThatThrownBy(() -> clubMemberService.registerMember(clubId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLUB_NOT_FOUND);
    }

    private ClubMemberProfile createProfile(String name, String studentId, Role role) {
        User user = User.builder().name(name).studentId(studentId).build();
        Club club = Club.builder().build();
        
        ClubMember clubMember = ClubMember.builder()
                .user(user)
                .club(club)
                .activeStatus(ActiveStatus.ACTIVE)
                .role(role)
                .build();

        ClubMemberProfile profile = ClubMemberProfile.builder()
                .clubMember(clubMember) // ClubMember 연결
                .name(name)
                .studentId(studentId)
                .phoneNumber("010-1234-5678")
                .college("공과대학")
                .department("컴퓨터공학과")
                .academicStatus(AcademicStatus.ENROLLED)
                .joinDate(LocalDate.of(2024, 3, 1))
                .role(role)
                .build();
        
        clubMember.setProfile(profile); // 양방향 연결
        return profile;
    }

    private ClubMemberSaveRequestDto createRequestDto(String name, String studentId, Role role) {
        return new ClubMemberSaveRequestDto(
                name, studentId, "010-1111-2222", "공과대학", "컴퓨터공학과",
                AcademicStatus.ENROLLED, role, "2024-03"
        );
    }
}
