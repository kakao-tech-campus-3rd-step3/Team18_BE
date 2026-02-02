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

        // Mocking registerMember behavior (내부 호출)
        // 주의: 같은 클래스 내의 메서드 호출은 @Spy를 쓰지 않는 한 Mocking이 안 됨.
        // 여기서는 registerMember가 실제로 실행되도록 두고, 내부 의존성들을 Mocking해야 함.
        
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

        ClubMemberProfile existingProfile = createProfile("이지훈", "20231234", Role.CLUB_MEMBER);
        
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
