package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.files.entity.File;
import com.kakaotech.team18.backend_server.domain.files.repository.FileDataRepository;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeCreateRequestDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.entity.Notice;
import com.kakaotech.team18.backend_server.domain.notices.repository.NoticeRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.exception.exceptions.AwsS3Exception;
import com.kakaotech.team18.backend_server.global.security.PrincipalDetails;
import com.kakaotech.team18.backend_server.global.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase
class NoticeCreateServiceTest {

        @Autowired
        private NoticeService noticeService;

        @Autowired
        private NoticeRepository noticeRepository;

        @Autowired
        private FileDataRepository fileDataRepository;

        @MockitoBean
        private S3Service s3Service;

        @MockitoBean
        private S3Presigner s3Presigner;

        @MockitoBean
        private ClubMemberRepository clubMemberRepository;

        @Test
        @DisplayName("공지사항 생성 - 파일 없음")
        @Transactional
        void createNotice_withoutFiles_success() throws Exception {
                // given
                NoticeCreateRequestDto request = new NoticeCreateRequestDto(
                                "Test Notice Title",
                                "Test Notice Content");

                User mockUser = mock(User.class);
                when(mockUser.getName()).thenReturn("테스트관리자");
                when(mockUser.getEmail()).thenReturn("admin@test.com");

                ClubMember mockClubMember = mock(ClubMember.class);
                when(mockClubMember.getUser()).thenReturn(mockUser);
                when(clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN))
                                .thenReturn(Optional.of(mockClubMember));

                Authentication auth = createMockAuthentication(1L);

                // when
                NoticeResponseDto response = noticeService.createNotice(request, null, auth);

                // then
                assertThat(response.id()).isNotNull();
                assertThat(response.title()).isEqualTo("Test Notice Title");
                assertThat(response.content()).isEqualTo("Test Notice Content");
                assertThat(response.file()).isEmpty();
                assertThat(response.author()).isEqualTo("테스트관리자");
                assertThat(response.email()).isEqualTo("admin@test.com");

                // DB 확인
                Optional<Notice> savedNotice = noticeRepository.findById(response.id());
                assertThat(savedNotice).isPresent();
                assertThat(savedNotice.get().getTitle()).isEqualTo("Test Notice Title");
                assertThat(savedNotice.get().isAlive()).isTrue();

                verify(s3Service, never()).uploadAttachment(any());
        }

        @Test
        @DisplayName("공지사항 생성 - 파일 포함")
        @Transactional
        void createNotice_withFiles_success() throws Exception {
                // given
                NoticeCreateRequestDto request = new NoticeCreateRequestDto(
                                "Test Notice with Files",
                                "Test Content with Attachments");

                MockMultipartFile file1 = new MockMultipartFile(
                                "files",
                                "test.pdf",
                                "application/pdf",
                                "test pdf content".getBytes());

                MockMultipartFile file2 = new MockMultipartFile(
                                "files",
                                "document.docx",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "test docx content".getBytes());

                when(s3Service.uploadAttachment(file1))
                                .thenReturn("https://bucket-attachments.s3.region.amazonaws.com/attachments/uuid-test.pdf");
                when(s3Service.uploadAttachment(file2))
                                .thenReturn("https://bucket-attachments.s3.region.amazonaws.com/attachments/uuid-document.docx");

                PresignedGetObjectRequest mockPresigned = mock(PresignedGetObjectRequest.class);
                when(mockPresigned.url()).thenReturn(URI.create("https://presigned-url.com/file").toURL());
                when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresigned);

                User mockUser = mock(User.class);
                when(mockUser.getName()).thenReturn("관리자");
                when(mockUser.getEmail()).thenReturn("admin@test.com");

                ClubMember mockClubMember = mock(ClubMember.class);
                when(mockClubMember.getUser()).thenReturn(mockUser);
                when(clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN))
                                .thenReturn(Optional.of(mockClubMember));

                Authentication auth = createMockAuthentication(1L);

                // when
                NoticeResponseDto response = noticeService.createNotice(
                                request,
                                List.of(file1, file2),
                                auth);

                // then
                assertThat(response.file()).hasSize(2);
                assertThat(response.file().get(0).name()).isEqualTo("test.pdf");
                assertThat(response.file().get(1).name()).isEqualTo("document.docx");

                verify(s3Service, times(1)).uploadAttachment(file1);
                verify(s3Service, times(1)).uploadAttachment(file2);

                // DB에 파일 정보 저장 확인
                List<File> savedFiles = fileDataRepository.findAllByNoticeId(response.id());
                assertThat(savedFiles).hasSize(2);
        }

        @Test
        @DisplayName("공지사항 생성 - 빈 파일 제외")
        @Transactional
        void createNotice_withEmptyFile_skipEmptyFile() throws Exception {
                // given
                NoticeCreateRequestDto request = new NoticeCreateRequestDto(
                                "Test Notice",
                                "Test Content");

                MockMultipartFile emptyFile = new MockMultipartFile(
                                "files",
                                "empty.txt",
                                "text/plain",
                                new byte[0] // 빈 파일
                );

                MockMultipartFile validFile = new MockMultipartFile(
                                "files",
                                "valid.pdf",
                                "application/pdf",
                                "content".getBytes());

                when(s3Service.uploadAttachment(validFile))
                                .thenReturn("https://bucket-attachments.s3.region.amazonaws.com/attachments/uuid-valid.pdf");

                PresignedGetObjectRequest mockPresigned = mock(PresignedGetObjectRequest.class);
                when(mockPresigned.url()).thenReturn(URI.create("https://presigned-url.com/file").toURL());
                when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresigned);

                User mockUser = mock(User.class);
                when(mockUser.getName()).thenReturn("관리자");
                when(mockUser.getEmail()).thenReturn("admin@test.com");

                ClubMember mockClubMember = mock(ClubMember.class);
                when(mockClubMember.getUser()).thenReturn(mockUser);
                when(clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN))
                                .thenReturn(Optional.of(mockClubMember));

                Authentication auth = createMockAuthentication(1L);

                // when
                NoticeResponseDto response = noticeService.createNotice(
                                request,
                                List.of(emptyFile, validFile),
                                auth);

                // then
                assertThat(response.file()).hasSize(1); // 빈 파일은 제외
                assertThat(response.file().get(0).name()).isEqualTo("valid.pdf");

                verify(s3Service, never()).uploadAttachment(emptyFile);
                verify(s3Service, times(1)).uploadAttachment(validFile);
        }

        @Test
        @DisplayName("공지사항 생성 - S3 업로드 실패 시 트랜잭션 롤백")
        // @Transactional NOT used here to verify rollback in service
        void createNotice_s3UploadFails_rollback() {
                // given
                NoticeCreateRequestDto request = new NoticeCreateRequestDto(
                                "Test Notice",
                                "Test Content");

                MockMultipartFile file = new MockMultipartFile(
                                "files",
                                "test.pdf",
                                "application/pdf",
                                "test".getBytes());

                when(s3Service.uploadAttachment(any()))
                                .thenThrow(new AwsS3Exception("S3 upload failed"));

                Authentication auth = createMockAuthentication(1L);

                long beforeCount = noticeRepository.count();

                // when & then
                assertThatThrownBy(() -> noticeService.createNotice(request, List.of(file), auth))
                                .isInstanceOf(AwsS3Exception.class)
                                .hasMessageContaining("AWS 에러 발생");

                // Verify notice was not saved (transaction rolled back)
                long afterCount = noticeRepository.count();
                assertThat(afterCount).isEqualTo(beforeCount);
        }

        @Test
        @DisplayName("공지사항 생성 - SYSTEM_ADMIN이 없어도 기본값 사용")
        @Transactional
        void createNotice_noSystemAdmin_useDefaultAuthor() throws Exception {
                // given
                NoticeCreateRequestDto request = new NoticeCreateRequestDto(
                                "Test Notice",
                                "Test Content");

                when(clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN))
                                .thenReturn(Optional.empty());

                Authentication auth = createMockAuthentication(1L);

                // when
                NoticeResponseDto response = noticeService.createNotice(request, null, auth);

                // then
                assertThat(response.author()).isEqualTo("관리자");
                assertThat(response.email()).isEqualTo("jnupole004@gmail.com");
        }

        private Authentication createMockAuthentication(Long userId) {
                PrincipalDetails principal = new PrincipalDetails(userId, Map.of());
                return new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities());
        }
}
