package com.kakaotech.team18.backend_server.domain.clubMember.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMemberProfile;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberProfileRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDate;
import java.util.List;
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
    private ClubRepository clubRepository;

    @Test
    @DisplayName("동아리원 목록 조회 성공 - 존재하는 동아리 ID")
    void getClubMembers_Success() {
        // given
        Long clubId = 1L;
        ClubMemberProfile profile1 = createProfile("이지훈", "212121", Role.CLUB_EXECUTIVE);
        ClubMemberProfile profile2 = createProfile("김철수", "232323", Role.CLUB_MEMBER);

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

    private ClubMemberProfile createProfile(String name, String studentId, Role role) {
        return ClubMemberProfile.builder()
                .name(name)
                .studentId(studentId)
                .phoneNumber("010-1234-5678")
                .college("공과대학")
                .department("컴퓨터공학과")
                .academicStatus(AcademicStatus.ENROLLED)
                .joinDate(LocalDate.of(2024, 3, 1))
                .role(role)
                .build();
    }
}
