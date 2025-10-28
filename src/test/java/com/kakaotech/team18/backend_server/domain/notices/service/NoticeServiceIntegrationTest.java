package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticePageResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.entity.Notice;
import com.kakaotech.team18.backend_server.domain.notices.repository.NoticeRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.exception.exceptions.NoticeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class NoticeServiceIntegrationTest {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @MockitoBean
    private ClubMemberRepository clubMemberRepository;

    @Mock
    private ClubMember mockAdmin;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAll();

        noticeRepository.save(Notice.builder()
                .title("A-1")
                .content("content-1")
                .isAlive(true)
                .build());

        noticeRepository.save(Notice.builder()
                .title("A-2")
                .content("content-2")
                .isAlive(true)
                .build());

        noticeRepository.save(Notice.builder()
                .title("A-3")
                .content("content-3")
                .isAlive(true)
                .build());

        noticeRepository.save(Notice.builder()
                .title("DEAD")
                .content("content-dead")
                .isAlive(false)
                .build());
    }

    @Nested
    @DisplayName("getAllNotices(page, size)")
    class GetAllNoticesTest {

        @Test
        @DisplayName("페이지 1(size=2): isAlive=true만 ASC(id) 순으로 2건 반환")
        void firstPage_size2_returnsTwoAliveAsc() {
            // when
            NoticePageResponseDto page1 = noticeService.getAllNotices(1, 2);

            // then
            assertThat(page1.content()).hasSize(2);
            assertThat(page1.content().get(0).title()).isEqualTo("A-3");
            assertThat(page1.content().get(0).author()).isEqualTo("관리자");
            assertThat(page1.content().get(1).title()).isEqualTo("A-2");
            assertThat(page1.pageInfo().pageSize()).isEqualTo(2);
            assertThat(page1.pageInfo().currentPage()).isEqualTo(1);
            assertThat(page1.pageInfo().totalPages()).isEqualTo(2);
            assertThat(page1.pageInfo().totalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("페이지 2(size=2): 나머지 alive 1건 반환")
        void secondPage_size2_returnsLastAlive() {
            // when
            NoticePageResponseDto page2 = noticeService.getAllNotices(2, 2);

            // then
            assertThat(page2.content()).hasSize(1);
            assertThat(page2.content().get(0).title()).isEqualTo("A-1");
            assertThat(page2.pageInfo().pageSize()).isEqualTo(2);
            assertThat(page2.pageInfo().currentPage()).isEqualTo(2);
            assertThat(page2.pageInfo().totalPages()).isEqualTo(2);
            assertThat(page2.pageInfo().totalElements()).isEqualTo(3);

        }

        @Test
        @DisplayName("페이지 범위를 넘기면 빈 배열 반환")
        void outOfRange_returnsEmpty() {
            // when
            NoticePageResponseDto page3 = noticeService.getAllNotices(3, 2);

            // then
            assertThat(page3.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getNoticeById(noticeId)")
    class GetNoticeByIdTest {

        @Test
        @DisplayName("관리자 존재 시: 작성자/이메일은 관리자(User) 기반으로 세팅")
        void adminExists_returnsAuthorFromAdmin() {
            // given
            Long targetId = noticeRepository.findAll()
                    .stream()
                    .filter(Notice::isAlive)
                    .map(Notice::getId)
                    .sorted()
                    .findFirst()
                    .orElseThrow();

            when(clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN))
                    .thenReturn(Optional.of(mockAdmin));
            when(mockAdmin.getUser()).thenReturn(mockUser);
            when(mockUser.getName()).thenReturn("System Admin");
            when(mockUser.getEmail()).thenReturn("admin@club.com");

            // when
            NoticeResponseDto dto = noticeService.getNoticeById(targetId);

            // then
            assertThat(dto.id()).isEqualTo(targetId);
            assertThat(dto.title()).isEqualTo("A-1");
            assertThat(dto.author()).isEqualTo("System Admin");
            assertThat(dto.email()).isEqualTo("admin@club.com");
            assertThat(dto.file()).isEqualTo("temp.txt");
        }

        @Test
        @DisplayName("관리자 미존재 시: 기본 작성자/이메일로 세팅(관리자 / jnupole004@gmail.com)")
        void adminNotExists_returnsAuthorFallback() {
            // given
            Long targetId = noticeRepository.findAll()
                    .stream()
                    .filter(Notice::isAlive)
                    .map(Notice::getId)
                    .sorted()
                    .findFirst()
                    .orElseThrow();

            when(clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN))
                    .thenReturn(Optional.empty());

            // when
            NoticeResponseDto dto = noticeService.getNoticeById(targetId);

            // then
            assertThat(dto.author()).isEqualTo("관리자");
            assertThat(dto.email()).isEqualTo("jnupole004@gmail.com");
        }

        @Test
        @DisplayName("존재하지 않거나 isAlive=false인 공지: NoticeNotFoundException")
        void notFound_throwsException() {
            // given
            long nonExistingId = 9999L;

            // then
            assertThatThrownBy(() -> noticeService.getNoticeById(nonExistingId))
                    .isInstanceOf(NoticeNotFoundException.class);
        }
    }
}
