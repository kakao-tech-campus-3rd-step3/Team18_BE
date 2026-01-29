package com.kakaotech.team18.backend_server.domain.clubMember.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMemberProfile;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberProfileRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.util.ExcelParseResult;
import com.kakaotech.team18.backend_server.domain.clubMember.util.ExcelUtils;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CustomException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ExcelParsingException;
import com.kakaotech.team18.backend_server.global.security.CustomSecurityService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberServiceImpl implements ClubMemberService {

    private final ClubMemberProfileRepository clubMemberProfileRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final CustomSecurityService customSecurityService;

    @Override
    public List<ClubMemberResponseDto> getClubMembers(Long clubId) {
        return clubMemberProfileRepository.findAllByClubId(clubId).stream()
                .map(ClubMemberResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public ClubMemberResponseDto registerMember(Long clubId, ClubMemberSaveRequestDto requestDto) {
        // 1. 요청자 권한 확인 (DB 조회 없이 토큰에서 바로 확인)
        Role currentUserRole = customSecurityService.getUserRoleInClub(clubId);

        // 2. Role 결정
        Role targetRole = requestDto.role();
        if (currentUserRole == Role.CLUB_EXECUTIVE) {
            targetRole = Role.CLUB_MEMBER; // 운영진은 무조건 일반부원으로 등록
        }

        // 3. 기존 프로필 조회 (학번 기준)
        Optional<ClubMemberProfile> existingProfile = clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(clubId, requestDto.studentId());

        if (existingProfile.isPresent()) {
            // 4. 존재하면 업데이트
            ClubMemberProfile profile = existingProfile.get();
            if (!profile.getName().equals(requestDto.name())) {
                throw new CustomException(ErrorCode.USER_ALREADY_EXISTS,
                        String.format("studentId: %s, existingName: %s, inputName: %s",
                                requestDto.studentId(), profile.getName(), requestDto.name()));
            }
            
            profile.update(
                    requestDto.name(),
                    requestDto.studentId(),
                    requestDto.phoneNumber(),
                    requestDto.college(),
                    requestDto.department(),
                    requestDto.academicStatus(),
                    parseJoinDate(requestDto.joinDate())
            );
            profile.updateRole(targetRole); // 결정된 Role로 업데이트
            
            return ClubMemberResponseDto.from(profile);
        } else {
            // 5. 존재하지 않으면 신규 등록
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND, "clubId: " + clubId));

            User user = userRepository.findByStudentId(requestDto.studentId())
                    .orElseGet(() -> createShellUser(requestDto));

            ClubMember clubMember = ClubMember.builder()
                    .user(user)
                    .club(club)
                    .activeStatus(ActiveStatus.ACTIVE)
                    .role(targetRole) // ClubMember의 role도 결정된 Role로 설정
                    .build();

            ClubMemberProfile profile = ClubMemberProfile.builder()
                    .name(requestDto.name())
                    .studentId(requestDto.studentId())
                    .phoneNumber(requestDto.phoneNumber())
                    .college(requestDto.college())
                    .department(requestDto.department())
                    .academicStatus(requestDto.academicStatus())
                    .joinDate(parseJoinDate(requestDto.joinDate()))
                    .role(targetRole)
                    .build();

            clubMember.setProfile(profile);
            clubMemberRepository.save(clubMember);

            return ClubMemberResponseDto.from(profile);
        }
    }

    @Override
    @Transactional
    public void registerMembersByExcel(Long clubId, MultipartFile file) throws IOException {
        // 1. 파일 유효성 검사
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FILE, "파일이 비어있습니다.");
        }
        if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")) {
            throw new CustomException(ErrorCode.INVALID_FILE, "엑셀 파일(.xlsx, .xls)만 업로드 가능합니다.");
        }

        // 2. 엑셀 파싱 (형식 오류 수집)
        ExcelParseResult parseResult = ExcelUtils.parseExcel(file);

        // 3. 파싱된 DTO 리스트를 순회하며 DB 저장 (비즈니스 오류 수집)
        // 형식 오류가 있어도, 성공한 데이터들에 대해서는 비즈니스 검증을 계속 진행하여 에러를 한 번에 모음
        List<ClubMemberSaveRequestDto> successList = parseResult.getSuccessList();
        
        for (ClubMemberSaveRequestDto dto : successList) {
            try {
                registerMember(clubId, dto);
            } catch (CustomException e) {
                // registerMember 내부에서 발생한 비즈니스 예외를 수집
                parseResult.addError(String.format("학번 %s: %s", dto.studentId(), e.getMessage()));
            }
        }

        // 4. 에러가 하나라도 있으면 전체 롤백
        if (parseResult.hasErrors()) {
            throw new ExcelParsingException("엑셀 데이터 검증 실패로 인해 전체 등록이 취소되었습니다.", parseResult.getErrorMessages());
        }
    }

    @Override
    @Transactional
    public ClubMemberResponseDto updateMember(Long clubId, Long profileId, ClubMemberUpdateRequestDto requestDto) {
        // 1. 프로필 조회
        ClubMemberProfile profile = clubMemberProfileRepository.findByIdAndClubId(profileId, clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_MEMBER_NOT_FOUND, "profileId: " + profileId));

        // 2. 중복 학번 검사 (나를 제외한 다른 멤버가 해당 학번을 사용 중인지)
        if (requestDto.studentId() != null && 
            clubMemberProfileRepository.existsByClubMember_Club_IdAndStudentIdAndIdNot(clubId, requestDto.studentId(), profileId)) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS, "해당 학번(" + requestDto.studentId() + ")은 이미 다른 멤버가 사용 중입니다.");
        }

        // 3. 정보 업데이트
        profile.update(
                requestDto.name(),
                requestDto.studentId(),
                requestDto.phoneNumber(),
                requestDto.college(),
                requestDto.department(),
                requestDto.academicStatus(),
                requestDto.joinDate() != null ? parseJoinDate(requestDto.joinDate()) : null
        );

        return ClubMemberResponseDto.from(profile);
    }


    private User createShellUser(ClubMemberSaveRequestDto requestDto) {
        User user = User.builder()
                .name(requestDto.name())
                .studentId(requestDto.studentId())
                .phoneNumber(requestDto.phoneNumber())
                .department(requestDto.department())
                .email(requestDto.studentId() + "@placeholder.com")
                .build();
        return userRepository.save(user);
    }

    private LocalDate parseJoinDate(String joinDateStr) {
        return LocalDate.parse(joinDateStr + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
