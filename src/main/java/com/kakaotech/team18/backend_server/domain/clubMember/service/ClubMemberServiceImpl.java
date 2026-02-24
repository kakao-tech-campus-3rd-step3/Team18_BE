package com.kakaotech.team18.backend_server.domain.clubMember.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberDeleteResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateRequestDto;
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
import com.kakaotech.team18.backend_server.global.exception.exceptions.CannotDeleteSelfException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CustomException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ExcelParsingException;
import com.kakaotech.team18.backend_server.global.security.CustomSecurityService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        if (!clubRepository.existsById(clubId)) {
            throw new ClubNotFoundException("clubId: " + clubId);
        }
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
        Role targetRole;
        if (currentUserRole == Role.CLUB_EXECUTIVE) {
            targetRole = Role.CLUB_MEMBER; // 운영진은 무조건 일반부원으로 등록
        } else {
            targetRole = requestDto.role();
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
            
            // 회장일 때만 Role 업데이트 수행 (운영진에 의한 강등 방지)
            if (currentUserRole == Role.CLUB_ADMIN) {
                profile.updateRole(targetRole);
                profile.getClubMember().updateRole(targetRole);
            }
            
            return ClubMemberResponseDto.from(profile);
        } else {
            // 5. 존재하지 않으면 신규 등록 (또는 기존 ClubMember에 프로필 연결)
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND, "clubId: " + clubId));

            User user = userRepository.findByStudentId(requestDto.studentId())
                    .orElseGet(() -> createShellUser(requestDto));

            // 기존 ClubMember가 있는지 확인
            ClubMember clubMember = clubMemberRepository.findByUserIdAndClubId(user.getId(), clubId)
                    .orElseGet(() -> ClubMember.builder()
                            .user(user)
                            .club(club)
                            .activeStatus(ActiveStatus.ACTIVE)
                            .role(targetRole)
                            .build());
            
            // Profile에 저장될 최종 Role 결정
            Role finalRole = targetRole;
            
            // 기존 ClubMember가 있다면
            if (clubMember.getId() != null) {
                // 회장이면 요청받은 Role로 업데이트
                if (currentUserRole == Role.CLUB_ADMIN) {
                    clubMember.updateRole(targetRole);
                } else {
                    // 운영진이면 기존 Role 유지 (강등 방지)
                    // 만약 기존 Role이 더 높다면 Profile도 그 Role을 따라가야 함
                    if (clubMember.getRole() == Role.CLUB_EXECUTIVE || clubMember.getRole() == Role.CLUB_ADMIN) {
                        finalRole = clubMember.getRole();
                    }
                }
            }

            ClubMemberProfile profile = ClubMemberProfile.builder()
                    .clubMember(clubMember) // 연관관계 설정
                    .name(requestDto.name())
                    .studentId(requestDto.studentId())
                    .phoneNumber(requestDto.phoneNumber())
                    .college(requestDto.college())
                    .department(requestDto.department())
                    .academicStatus(requestDto.academicStatus())
                    .joinDate(parseJoinDate(requestDto.joinDate()))
                    .role(finalRole) // 보정된 Role 사용
                    .build();

            clubMember.setProfile(profile);
            clubMemberRepository.save(clubMember);

            return ClubMemberResponseDto.from(profile);
        }
    }

    @Override
    @Transactional
    public void registerMembersByExcel(Long clubId, MultipartFile file) {
        // 1. 파일 유효성 검사
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FILE, "파일이 비어있습니다.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new CustomException(ErrorCode.INVALID_FILE, "엑셀 파일(.xlsx, .xls)만 업로드 가능합니다.");
        }

        // 2. 엑셀 파싱 (형식 오류 수집)
        ExcelParseResult parseResult;
        try {
            parseResult = ExcelUtils.parseExcel(file);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_FILE, "파일을 읽는 도중 오류가 발생했습니다.");
        }

        // 3. 파싱된 DTO 리스트를 순회하며 DB 저장 (비즈니스 오류 수집)
        // 형식 오류가 있어도, 성공한 데이터들에 대해서는 비즈니스 검증을 계속 진행하여 에러를 한 번에 모음
        List<ClubMemberSaveRequestDto> successList = parseResult.getSuccessList();
        
        for (ClubMemberSaveRequestDto dto : successList) {
            try {
                registerMember(clubId, dto);
            } catch (CustomException e) {
                // registerMember 내부에서 발생한 비즈니스 예외를 수집
                parseResult.addError(String.format("학번 %s: %s", dto.studentId(), e.getMessage()));
            } catch (Exception e) {
                // 그 외 예상치 못한 예외 수집
                parseResult.addError(String.format("학번 %s: 알 수 없는 오류가 발생했습니다. (%s)", dto.studentId(), e.getMessage()));
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

    @Override
    @Transactional
    public ClubMemberRoleUpdateResponseDto updateMemberRole(Long clubId, Long profileId, ClubMemberRoleUpdateRequestDto requestDto) {
        // 1. 요청자 권한 확인 (회장만 가능)
        Role currentUserRole = customSecurityService.getUserRoleInClub(clubId);
        if (currentUserRole != Role.CLUB_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN, "동아리원 직책 변경은 회장만 가능합니다.");
        }

        // 2. 프로필 조회 (대상)
        ClubMemberProfile profile = clubMemberProfileRepository.findByIdAndClubId(profileId, clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_MEMBER_NOT_FOUND, "profileId: " + profileId));

        Role previousRole = profile.getRole();
        Role newRole = requestDto.role();

        // 3. 회장직 이양 로직 (새로운 역할이 회장인 경우)
        if (newRole == Role.CLUB_ADMIN) {
            // ClubMember 테이블 기준으로 현재 회장 조회 (프로필 유무 상관없이)
            Optional<ClubMember> currentAdminMember = clubMemberRepository.findClubAdminByClubIdAndRole(clubId, Role.CLUB_ADMIN);
            
            // 기존 회장이 존재하고, 그 사람이 이번에 임명되는 사람이 아니라면 강등
            if (currentAdminMember.isPresent() && !currentAdminMember.get().getId().equals(profile.getClubMember().getId())) {
                ClubMember oldAdmin = currentAdminMember.get();
                oldAdmin.updateRole(Role.CLUB_EXECUTIVE); // 운영진으로 강등
                
                // 프로필이 있다면 프로필도 동기화
                if (oldAdmin.getProfile() != null) {
                    oldAdmin.getProfile().updateRole(Role.CLUB_EXECUTIVE);
                }
            }
        }

        // 4. Role 업데이트 (Profile & ClubMember 동기화)
        profile.updateRole(newRole);
        profile.getClubMember().updateRole(newRole);

        // 5. 메시지 생성
        String message;
        if (newRole == Role.CLUB_MEMBER) {
            message = "일반 부원으로 역할이 변경되었습니다.";
        } else if (newRole == Role.CLUB_EXECUTIVE) {
            message = "운영진으로 역할이 변경되었습니다.";
        } else {
            message = "회장으로 역할이 변경되었습니다.";
        }

        return ClubMemberRoleUpdateResponseDto.builder()
                .clubId(clubId)
                .clubName(profile.getClubMember().getClub().getName())
                .clubMemberProfileId(profile.getId())
                .studentId(profile.getStudentId())
                .name(profile.getName())
                .previousRole(previousRole)
                .newRole(newRole)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public ClubMemberDeleteResponseDto deleteMember(Long clubId, Long profileId) {
        // 1. 요청자 권한 확인 (회장만 가능)
        Role currentUserRole = customSecurityService.getUserRoleInClub(clubId);
        if (currentUserRole != Role.CLUB_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN, "동아리원 삭제는 회장만 가능합니다.");
        }

        // 2. 프로필 조회
        ClubMemberProfile profile = clubMemberProfileRepository.findByIdAndClubId(profileId, clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_MEMBER_NOT_FOUND, "profileId: " + profileId));

        // 3. 자기 자신 삭제 방지
        Long currentUserId = customSecurityService.getCurrentUserId();
        if (profile.isOwner(currentUserId)) {
            throw new CannotDeleteSelfException();
        }

        // 4. 삭제 (Cascade로 Profile도 함께 삭제됨)
        clubMemberRepository.delete(profile.getClubMember());

        return new ClubMemberDeleteResponseDto(
                "해당 동아리원이 목록에서 삭제되었습니다.",
                profileId
        );
    }


    private User createShellUser(ClubMemberSaveRequestDto requestDto) {
        if (userRepository.existsByPhoneNumber(requestDto.phoneNumber())) {
            throw new CustomException(ErrorCode.EXISTING_USER_PHONE_NUMBER, "이미 사용 중인 전화번호입니다.");
        }

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
        try {
            return LocalDate.parse(joinDateStr + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "가입일자 형식이 올바르지 않습니다. (YYYY-MM)");
        }
    }
}
