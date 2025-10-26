package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeBriefResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.entity.Notice;
import com.kakaotech.team18.backend_server.domain.notices.repository.NoticeRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.NoticeNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class NoticeServiceImpl implements NoticeService {

    NoticeRepository noticeRepository;
    ClubMemberRepository clubMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NoticeBriefResponseDto> getAllNotices(Long page, Long size) {
        long p = (page == null || page < 1) ? 1L : page;
        int s = (size == null || size < 1 || size > 100) ? 10 : size.intValue();

        Pageable pageable = PageRequest.of((int) (p - 1), s);

        return noticeRepository.findByIsAliveTrueOrderByCreatedAtDesc(pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponseDto getNoticeById(Long noticeId) {
        Notice n = noticeRepository.findAliveById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항을 찾을 수 없습니다. ID:" + noticeId) );

        String authorName = clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN)
                .map(cm -> cm.getUser().getName())
                .orElse("관리자");

        String authorEmail = clubMemberRepository.findFirstByRole(Role.SYSTEM_ADMIN)
                .map(cm -> cm.getUser().getEmail())
                .orElse("jnupole004@gmail.com");

        return new NoticeResponseDto(
                n.getId(),
                n.getTitle(),
                n.getContent(),
                n.getCreatedAt(),
                authorName,
                authorEmail
        );
    }
}
