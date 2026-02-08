package com.kakaotech.team18.backend_server.domain.clubMember.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberDeleteResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.service.ClubMemberService;
import com.kakaotech.team18.backend_server.global.security.CustomSecurityService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClubMemberController.class)
class ClubMemberControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ClubMemberService clubMemberService;

        @MockBean
        private CustomSecurityService customSecurityService;

        @Test
        @WithMockUser
        @DisplayName("동아리원 목록 조회 - 성공")
        void getClubMembers_Success() throws Exception {
                // given
                Long clubId = 1L;
                ClubMemberResponseDto responseDto = new ClubMemberResponseDto(
                                101L, "TestUser", null, null, null, Role.CLUB_MEMBER, null);

                given(customSecurityService.isClubAdminOrExecutive(clubId)).willReturn(true);
                given(clubMemberService.getClubMembers(clubId)).willReturn(List.of(responseDto));

                // when & then
                mockMvc.perform(get("/api/clubs/{clubId}/members", clubId)
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("TestUser"));
        }

        @Test
        @WithMockUser
        @DisplayName("동아리원 수동 등록 - 성공")
        void registerMember_Success() throws Exception {
                // given
                Long clubId = 1L;
                ClubMemberSaveRequestDto requestDto = new ClubMemberSaveRequestDto(
                                "NewUser", "20240001", "010-1234-5678", "Engineering", "CS",
                                AcademicStatus.ENROLLED, Role.CLUB_MEMBER, "2024-03");

                ClubMemberResponseDto responseDto = new ClubMemberResponseDto(
                                null, "NewUser", null, "20240001", null, Role.CLUB_MEMBER, null);

                given(customSecurityService.isClubAdminOrExecutive(clubId)).willReturn(true);
                given(clubMemberService.registerMember(eq(clubId), any(ClubMemberSaveRequestDto.class)))
                                .willReturn(responseDto);

                // when & then
                mockMvc.perform(post("/api/clubs/{clubId}/members", clubId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("NewUser"))
                                .andExpect(jsonPath("$.studentId").value("20240001"));
        }

        @Test
        @WithMockUser
        @DisplayName("동아리원 일괄 등록 - 성공")
        void uploadMembers_Success() throws Exception {
                // given
                Long clubId = 1L;
                MockMultipartFile file = new MockMultipartFile(
                                "file", "members.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "test data".getBytes());

                given(customSecurityService.isClubAdminOrExecutive(clubId)).willReturn(true);

                // when & then
                mockMvc.perform(multipart("/api/clubs/{clubId}/members/upload", clubId)
                                .file(file)
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("동아리원 정보 수정 - 성공")
        void updateMember_Success() throws Exception {
                // given
                Long clubId = 1L;
                Long profileId = 101L;
                ClubMemberUpdateRequestDto requestDto = new ClubMemberUpdateRequestDto(
                                "UpdatedUser", "20240001", "010-9876-5432", "Engineering", "CS",
                                AcademicStatus.LEAVE_OF_ABSENCE, "2024-02");

                ClubMemberResponseDto responseDto = new ClubMemberResponseDto(
                                profileId, "UpdatedUser", null, null, null, null, null);

                given(customSecurityService.isClubAdminOrExecutive(clubId)).willReturn(true);
                given(clubMemberService.updateMember(eq(clubId), eq(profileId), any(ClubMemberUpdateRequestDto.class)))
                                .willReturn(responseDto);

                // when & then
                mockMvc.perform(patch("/api/clubs/{clubId}/members/{profileId}", clubId, profileId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("UpdatedUser"));
        }

        @Test
        @WithMockUser
        @DisplayName("동아리원 직책 변경 - 성공")
        void updateMemberRole_Success() throws Exception {
                // given
                Long clubId = 1L;
                Long profileId = 101L;
                ClubMemberRoleUpdateRequestDto requestDto = new ClubMemberRoleUpdateRequestDto(Role.CLUB_EXECUTIVE);

                ClubMemberRoleUpdateResponseDto responseDto = ClubMemberRoleUpdateResponseDto.builder()
                                .clubId(clubId)
                                .clubMemberProfileId(profileId)
                                .newRole(Role.CLUB_EXECUTIVE)
                                .build();

                given(customSecurityService.isClubAdminOrExecutive(clubId)).willReturn(true);
                given(clubMemberService.updateMemberRole(eq(clubId), eq(profileId),
                                any(ClubMemberRoleUpdateRequestDto.class)))
                                .willReturn(responseDto);

                // when & then
                mockMvc.perform(patch("/api/clubs/{clubId}/members/{profileId}/role", clubId, profileId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.newRole").value("CLUB_EXECUTIVE"));
        }

        @Test
        @WithMockUser
        @DisplayName("동아리원 삭제 - 성공")
        void deleteMember_Success() throws Exception {
                // given
                Long clubId = 1L;
                Long profileId = 101L;
                ClubMemberDeleteResponseDto responseDto = new ClubMemberDeleteResponseDto("Deleted successfully",
                                profileId);

                given(customSecurityService.isClubAdminOrExecutive(clubId)).willReturn(true);
                given(clubMemberService.deleteMember(clubId, profileId)).willReturn(responseDto);

                // when & then
                mockMvc.perform(delete("/api/clubs/{clubId}/members/{profileId}", clubId, profileId)
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedClubMemberProfileId").value(profileId));
        }
}
