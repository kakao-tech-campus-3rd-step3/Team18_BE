package com.kakaotech.team18.backend_server.domain.clubMember.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMemberProfile;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberProfileRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CustomException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberServiceImpl implements ClubMemberService {

    private final ClubMemberProfileRepository clubMemberProfileRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;

    @Override
    public List<ClubMemberResponseDto> getClubMembers(Long clubId) {
        return clubMemberProfileRepository.findAllByClubId(clubId).stream()
                .map(ClubMemberResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public ClubMemberResponseDto registerMember(Long clubId, ClubMemberSaveRequestDto requestDto) {
        // 1. 기존 프로필 조회 (학번 기준)
        Optional<ClubMemberProfile> existingProfile = clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(clubId, requestDto.studentId());

        if (existingProfile.isPresent()) {
            // 2. 존재하면 업데이트 (이름 일치 여부 확인)
            ClubMemberProfile profile = existingProfile.get();
            if (!profile.getName().equals(requestDto.name())) {
                throw new CustomException(ErrorCode.USER_ALREADY_EXISTS,
                        String.format("studentId: %s, existingName: %s, inputName: %s",
                                requestDto.studentId(), profile.getName(), requestDto.name()));
            }
            
            // 이름이 일치하면 정보 업데이트
            profile.update(
                    requestDto.name(),
                    requestDto.studentId(),
                    requestDto.phoneNumber(),
                    requestDto.college(),
                    requestDto.department(),
                    requestDto.academicStatus(),
                    parseJoinDate(requestDto.joinDate())
            );
            // Role 업데이트 (별도 메서드 사용)
            profile.updateRole(requestDto.role());
            
            return ClubMemberResponseDto.from(profile);
        } else {
            // 3. 존재하지 않으면 신규 등록
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND, "clubId: " + clubId));

            // User 조회 또는 생성 (껍데기)
            User user = userRepository.findByStudentId(requestDto.studentId())
                    .orElseGet(() -> createShellUser(requestDto));

            // ClubMember 생성
            ClubMember clubMember = ClubMember.builder()
                    .user(user)
                    .club(club)
                    .activeStatus(ActiveStatus.ACTIVE) // 기본값 ACTIVE
                    .role(requestDto.role()) // ClubMember의 role은 일단 요청값으로 설정 (Profile과 동기화)
                    .build();

            // ClubMemberProfile 생성
            ClubMemberProfile profile = ClubMemberProfile.builder()
                    .name(requestDto.name())
                    .studentId(requestDto.studentId())
                    .phoneNumber(requestDto.phoneNumber())
                    .college(requestDto.college())
                    .department(requestDto.department())
                    .academicStatus(requestDto.academicStatus())
                    .joinDate(parseJoinDate(requestDto.joinDate()))
                    .role(requestDto.role())
                    .build();

            // 연관관계 설정 (Cascade 저장)
            clubMember.setProfile(profile);
            clubMemberRepository.save(clubMember);

            return ClubMemberResponseDto.from(profile);
        }
    }

    private User createShellUser(ClubMemberSaveRequestDto requestDto) {
        User user = User.builder()
                .name(requestDto.name())
                .studentId(requestDto.studentId())
                .phoneNumber(requestDto.phoneNumber())
                .department(requestDto.department())
                .email(requestDto.studentId() + "@placeholder.com") // 이메일 필수라 임시값 설정 (추후 고민 필요)
                .build();
        return userRepository.save(user);
    }

    private LocalDate parseJoinDate(String joinDateStr) {
        // YYYY-MM 형식을 YYYY-MM-01로 변환
        return LocalDate.parse(joinDateStr + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
