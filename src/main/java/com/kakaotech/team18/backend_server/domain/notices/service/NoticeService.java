package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeCreateRequestDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticePageResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoticeService {

    NoticePageResponseDto getAllNotices(Integer page, Integer size);

    NoticeResponseDto getNoticeById(Long noticeId);

    NoticeResponseDto createNotice(
        NoticeCreateRequestDto requestDto,
        List<MultipartFile> files,
        Authentication authentication
    );

}
