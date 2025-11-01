package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.files.entity.File;
import com.kakaotech.team18.backend_server.domain.files.repository.FileDataRepository;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticePageResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.entity.Notice;
import com.kakaotech.team18.backend_server.domain.notices.repository.NoticeRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.FileNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.NoticeNotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final FileDataRepository  fileDataRepository;
    private final String bucketName;
    private final S3Presigner presigner;

    public NoticeServiceImpl(
            NoticeRepository noticeRepository,
            ClubMemberRepository clubMemberRepository,
            FileDataRepository fileDataRepository,
            S3Presigner presigner,
            @Value("${cloud.aws.s3.bucket}") String bucketName
            ){
        this.noticeRepository = noticeRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.fileDataRepository = fileDataRepository;
        this.presigner = presigner;
        this.bucketName = bucketName;
    }

    @Override
    @Transactional(readOnly = true)
    public NoticePageResponseDto getAllNotices(Integer page, Integer size) {

        int p = page - 1;
        int s = size;

        Pageable pageable = PageRequest.of(p, s);

        //TODO 실제 작성자 호출할 방법 필요
        final String author = clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN)
                .map(cm -> cm.getUser().getName())
                .orElse("관리자");

        List<NoticePageResponseDto.NoticeBriefResponseDto> brief = noticeRepository.findAlive(pageable)
                .map(n -> new NoticePageResponseDto.NoticeBriefResponseDto(
                        n.getId(),
                        n.getTitle(),
                        n.getCreatedAt(),
                        author
                ))
                .getContent();

        Integer totalElements =  noticeRepository.countByIsAliveTrue();
        Integer totalPages = (totalElements + size -1) / size;

        NoticePageResponseDto.PageInfo pageInfo = new NoticePageResponseDto.PageInfo(
                page,
                size,
                totalPages,
                totalElements
        );

        return new NoticePageResponseDto(
                brief,
                pageInfo
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponseDto getNoticeById(Long noticeId) {
        Notice n = noticeRepository.findAliveById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항을 찾을 수 없습니다. ID:" + noticeId) );

        //TODO 실제 작성자 호출할 방법 필요
        Optional<ClubMember> clubMember = clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN);
        String authorName = clubMember
                .map(cm -> cm.getUser().getName())
                .orElse("관리자");

        String authorEmail = clubMember
                .map(cm -> cm.getUser().getEmail())
                .orElse("jnupole004@gmail.com");

        List<File> fileList = fileDataRepository.findAllByNoticeId(noticeId);
        List<NoticeResponseDto.FileDetail> fileDetailList = new ArrayList<>();

        for(File file:fileList) {
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getName())
                    .responseContentDisposition(contentDispositionAttachment(file.getName()))
                    .responseContentType(file.getType())
                    .build();

            GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getReq)
                    .build();

            String presignedUrl = presigner.presignGetObject(presignReq).url().toString();

            fileDetailList.add(new NoticeResponseDto.FileDetail(
                    file.getId(),
                    file.getName(),
                    presignedUrl,
                    file.getObjectUri()
            ));
        }

        return new NoticeResponseDto(
                n.getId(),
                n.getTitle(),
                n.getContent(),
                n.getCreatedAt(),
                authorName,
                authorEmail,
                fileDetailList
        );
    }
    private static String contentDispositionAttachment(String filename) {
        // 1) 브라우저 호환용 ASCII 대체 이름(간단히 공백/한글 제거 또는 기본값)
        String asciiFallback = "download";

        // 2) RFC 5987 방식 UTF-8 인코딩 (공백은 %20로)
        String encoded = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        // 3) Content-Disposition 조합
        return "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encoded;
    }

}
