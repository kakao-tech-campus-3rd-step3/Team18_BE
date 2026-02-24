package com.kakaotech.team18.backend_server.domain.clubMember.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberUpdateRequestDto;
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
import com.kakaotech.team18.backend_server.global.exception.exceptions.CannotDeleteSelfException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CustomException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ExcelParsingException;
import com.kakaotech.team18.backend_server.global.security.CustomSecurityService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

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
        ClubMemberProfile profile1 = createProfile(10L, 100L, "이지훈", "20231234", Role.CLUB_EXECUTIVE);
        ClubMemberProfile profile2 = createProfile(20L, 200L, "김철수", "20245678", Role.CLUB_MEMBER);

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
        ClubMemberProfile existingProfile = createProfile(10L, 100L, "이지훈", "20231234", Role.CLUB_MEMBER); // 이름 일치

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
        ClubMemberProfile existingProfile = createProfile(10L, 100L, "이지훈", "20231234", Role.CLUB_MEMBER);

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

    @Test
    @DisplayName("동아리원 일괄 등록 성공 - 정상 파일")
    void registerMembersByExcel_Success() throws IOException {
        // given
        Long clubId = 1L;
        List<List<String>> data = Arrays.asList(
                Arrays.asList("이름", "학번", "전화번호", "단과대학", "학과", "학적상태", "직책", "가입일자"), // 헤더
                Arrays.asList("박신입", "241001", "010-1111-2222", "공대", "컴공", "재학", "일반부원", "2024-03"),
                Arrays.asList("김신입", "241002", "010-3333-4444", "공대", "컴공", "재학", "일반부원", "2024-03")
        );
        MockMultipartFile file = createExcelFile("members.xlsx", data);

        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN);
        given(clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(anyLong(), anyString()))
                .willReturn(Optional.empty()); // 모두 신규
        given(clubRepository.findById(clubId)).willReturn(Optional.of(Club.builder().build()));
        given(userRepository.findByStudentId(anyString())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(User.builder().build());

        // when
        clubMemberService.registerMembersByExcel(clubId, file);

        // then
        verify(clubMemberRepository, times(2)).save(any(ClubMember.class)); // 2명 저장됨
    }

    @Test
    @DisplayName("동아리원 일괄 등록 실패 - 엑셀 파싱 에러 (형식 오류)")
    void registerMembersByExcel_Fail_ParsingError() throws IOException {
        // given
        Long clubId = 1L;
        List<List<String>> data = Arrays.asList(
                Arrays.asList("이름", "학번", "전화번호", "단과대학", "학과", "학적상태", "직책", "가입일자"),
                Arrays.asList("박신입", "241001", "잘못된번호", "공대", "컴공", "재학", "일반부원", "2024-03") // 전화번호 오류
        );
        MockMultipartFile file = createExcelFile("members.xlsx", data);

        // when & then
        assertThatThrownBy(() -> clubMemberService.registerMembersByExcel(clubId, file))
                .isInstanceOf(ExcelParsingException.class)
                .hasMessageContaining("엑셀 데이터 검증 실패");
    }

    @Test
    @DisplayName("동아리원 일괄 등록 실패 - 비즈니스 에러 (중복 충돌)")
    void registerMembersByExcel_Fail_BusinessError() throws IOException {
        // given
        Long clubId = 1L;
        List<List<String>> data = Arrays.asList(
                Arrays.asList("이름", "학번", "전화번호", "단과대학", "학과", "학적상태", "직책", "가입일자"),
                Arrays.asList("김기춘", "20231234", "010-1111-2222", "공대", "컴공", "재학", "일반부원", "2024-03") // 이름 불일치
        );
        MockMultipartFile file = createExcelFile("members.xlsx", data);

        ClubMemberProfile existingProfile = createProfile(10L, 100L, "이지훈", "20231234", Role.CLUB_MEMBER);
        
        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN);
        given(clubMemberProfileRepository.findByClubMember_Club_IdAndStudentId(clubId, "20231234"))
                .willReturn(Optional.of(existingProfile));

        // when & then
        assertThatThrownBy(() -> clubMemberService.registerMembersByExcel(clubId, file))
                .isInstanceOf(ExcelParsingException.class)
                .hasMessageContaining("엑셀 데이터 검증 실패");
    }

    @Test
    @DisplayName("동아리원 일괄 등록 실패 - 파일 확장자 오류")
    void registerMembersByExcel_Fail_InvalidExtension() {
        // given
        Long clubId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "members.txt", "text/plain", "test".getBytes());

        // when & then
        assertThatThrownBy(() -> clubMemberService.registerMembersByExcel(clubId, file))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE);
    }

    @Test
    @DisplayName("동아리원 정보 수정 성공")
    void updateMember_Success() {
        // given
        Long clubId = 1L;
        Long profileId = 501L;
        ClubMemberUpdateRequestDto requestDto = new ClubMemberUpdateRequestDto(
                "박개명", null, null, null, null, null, null
        );
        ClubMemberProfile profile = createProfile(profileId, 100L, "박원래", "212121", Role.CLUB_MEMBER);

        given(clubMemberProfileRepository.findByIdAndClubId(profileId, clubId)).willReturn(Optional.of(profile));
        // 중복 검사 통과 (학번 변경 없음)

        // when
        ClubMemberResponseDto result = clubMemberService.updateMember(clubId, profileId, requestDto);

        // then
        assertThat(result.name()).isEqualTo("박개명"); // 이름 변경됨
        assertThat(result.studentId()).isEqualTo("212121"); // 학번 유지됨
    }

    @Test
    @DisplayName("동아리원 정보 수정 실패 - 중복 학번")
    void updateMember_Fail_DuplicateStudentId() {
        // given
        Long clubId = 1L;
        Long profileId = 501L;
        ClubMemberUpdateRequestDto requestDto = new ClubMemberUpdateRequestDto(
                null, "20245678", null, null, null, null, null // 다른 사람 학번으로 변경 시도
        );
        ClubMemberProfile profile = createProfile(profileId, 100L, "이지훈", "20231234", Role.CLUB_MEMBER);

        given(clubMemberProfileRepository.findByIdAndClubId(profileId, clubId)).willReturn(Optional.of(profile));
        given(clubMemberProfileRepository.existsByClubMember_Club_IdAndStudentIdAndIdNot(clubId, "20245678", profileId))
                .willReturn(true); // 이미 존재함

        // when & then
        assertThatThrownBy(() -> clubMemberService.updateMember(clubId, profileId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("동아리원 직책 변경 성공 - 회장 요청")
    void updateMemberRole_Success() {
        // given
        Long clubId = 1L;
        Long profileId = 501L;
        ClubMemberRoleUpdateRequestDto requestDto = new ClubMemberRoleUpdateRequestDto(Role.CLUB_EXECUTIVE);
        ClubMemberProfile profile = createProfile(profileId, 100L, "이지훈", "20231234", Role.CLUB_MEMBER);

        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN); // 회장
        given(clubMemberProfileRepository.findByIdAndClubId(profileId, clubId)).willReturn(Optional.of(profile));

        // when
        ClubMemberRoleUpdateResponseDto result = clubMemberService.updateMemberRole(clubId, profileId, requestDto);

        // then
        assertThat(result.newRole()).isEqualTo(Role.CLUB_EXECUTIVE);
        assertThat(profile.getClubMember().getRole()).isEqualTo(Role.CLUB_EXECUTIVE); // 동기화 확인
    }

    @Test
    @DisplayName("동아리원 직책 변경 실패 - 운영진 요청")
    void updateMemberRole_Fail_Forbidden() {
        // given
        Long clubId = 1L;
        Long profileId = 501L;
        ClubMemberRoleUpdateRequestDto requestDto = new ClubMemberRoleUpdateRequestDto(Role.CLUB_EXECUTIVE);

        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_EXECUTIVE); // 운영진

        // when & then
        assertThatThrownBy(() -> clubMemberService.updateMemberRole(clubId, profileId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("동아리원 직책 변경 성공 - 회장직 이양")
    void updateMemberRole_Success_TransferAdmin() {
        // given
        Long clubId = 1L;
        Long oldAdminProfileId = 10L;
        Long newAdminProfileId = 20L;
        
        ClubMemberProfile oldAdmin = createProfile(oldAdminProfileId, 100L, "나회장", "111111", Role.CLUB_ADMIN);
        ClubMemberProfile newAdmin = createProfile(newAdminProfileId, 200L, "너회장", "222222", Role.CLUB_MEMBER);
        
        // oldAdmin의 ClubMember ID 설정 (비교 로직에서 사용)
        ReflectionTestUtils.setField(oldAdmin.getClubMember(), "id", 1000L);
        ReflectionTestUtils.setField(newAdmin.getClubMember(), "id", 2000L);

        ClubMemberRoleUpdateRequestDto requestDto = new ClubMemberRoleUpdateRequestDto(Role.CLUB_ADMIN);

        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN);
        given(clubMemberProfileRepository.findByIdAndClubId(newAdminProfileId, clubId)).willReturn(Optional.of(newAdmin));
        
        // ★ 수정됨: ClubMemberRepository Mocking
        given(clubMemberRepository.findClubAdminByClubIdAndRole(clubId, Role.CLUB_ADMIN))
                .willReturn(Optional.of(oldAdmin.getClubMember()));

        // when
        ClubMemberRoleUpdateResponseDto result = clubMemberService.updateMemberRole(clubId, newAdminProfileId, requestDto);

        // then
        assertThat(result.newRole()).isEqualTo(Role.CLUB_ADMIN);
        assertThat(newAdmin.getRole()).isEqualTo(Role.CLUB_ADMIN); // 새 회장 승격 확인
        assertThat(oldAdmin.getRole()).isEqualTo(Role.CLUB_EXECUTIVE); // 기존 회장 강등 확인
    }

    @Test
    @DisplayName("동아리원 삭제 성공")
    void deleteMember_Success() {
        // given
        Long clubId = 1L;
        Long profileId = 501L;
        Long currentUserId = 100L;
        ClubMemberProfile profile = createProfile(profileId, 999L, "이지훈", "20231234", Role.CLUB_MEMBER);
        
        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN);
        given(clubMemberProfileRepository.findByIdAndClubId(profileId, clubId)).willReturn(Optional.of(profile));
        given(customSecurityService.getCurrentUserId()).willReturn(currentUserId);
        // isOwner -> false (100 != 999)

        // when
        clubMemberService.deleteMember(clubId, profileId);

        // then
        verify(clubMemberRepository, times(1)).delete(any(ClubMember.class));
    }

    @Test
    @DisplayName("동아리원 삭제 실패 - 자기 자신 삭제")
    void deleteMember_Fail_SelfDelete() {
        // given
        Long clubId = 1L;
        Long profileId = 501L;
        Long currentUserId = 100L;
        
        // 내 프로필 생성 (User ID = 100)
        User user = User.builder().name("나회장").studentId("20231234").build();
        ReflectionTestUtils.setField(user, "id", currentUserId);

        Club club = Club.builder().build();
        ClubMember clubMember = ClubMember.builder().user(user).club(club).role(Role.CLUB_ADMIN).build();
        ClubMemberProfile profile = ClubMemberProfile.builder().clubMember(clubMember).name("나회장").role(Role.CLUB_ADMIN).build();
        ReflectionTestUtils.setField(profile, "id", profileId);
        clubMember.setProfile(profile);

        given(customSecurityService.getUserRoleInClub(clubId)).willReturn(Role.CLUB_ADMIN);
        given(clubMemberProfileRepository.findByIdAndClubId(profileId, clubId)).willReturn(Optional.of(profile));
        given(customSecurityService.getCurrentUserId()).willReturn(currentUserId);

        // when & then
        assertThatThrownBy(() -> clubMemberService.deleteMember(clubId, profileId))
                .isInstanceOf(CannotDeleteSelfException.class);
    }

    private ClubMemberProfile createProfile(Long profileId, Long userId, String name, String studentId, Role role) {
        User user = User.builder().name(name).studentId(studentId).build();
        ReflectionTestUtils.setField(user, "id", userId);

        Club club = Club.builder().name("동아리움").build();
        
        ClubMember clubMember = ClubMember.builder()
                .user(user)
                .club(club)
                .activeStatus(ActiveStatus.ACTIVE)
                .role(role)
                .build();

        ClubMemberProfile profile = ClubMemberProfile.builder()
                .clubMember(clubMember)
                .name(name)
                .studentId(studentId)
                .phoneNumber("010-1234-5678")
                .college("공과대학")
                .department("컴퓨터공학과")
                .academicStatus(AcademicStatus.ENROLLED)
                .joinDate(LocalDate.of(2024, 3, 1))
                .role(role)
                .build();
        ReflectionTestUtils.setField(profile, "id", profileId);
        
        clubMember.setProfile(profile); // 양방향 연결
        return profile;
    }

    private ClubMemberSaveRequestDto createRequestDto(String name, String studentId, Role role) {
        return new ClubMemberSaveRequestDto(
                name, studentId, "010-1111-2222", "공과대학", "컴퓨터공학과",
                AcademicStatus.ENROLLED, role, "2024-03"
        );
    }

    private MockMultipartFile createExcelFile(String fileName, List<List<String>> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Members");
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> rowData = data.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    row.createCell(j).setCellValue(rowData.get(j));
                }
            }
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return new MockMultipartFile("file", fileName, 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());
        }
    }
}
