package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticePageResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.entity.Notice;
import com.kakaotech.team18.backend_server.domain.notices.repository.NoticeRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.NoticeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final RestClient.Builder builder;

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

        //TODO 실제 첨부파일 로직으로 대체 필요
        String attachFile = "temp.txt";

        return new NoticeResponseDto(
                n.getId(),
                n.getTitle(),
                n.getContent(),
                n.getCreatedAt(),
                authorName,
                authorEmail,
                attachFile
        );
    }
}
