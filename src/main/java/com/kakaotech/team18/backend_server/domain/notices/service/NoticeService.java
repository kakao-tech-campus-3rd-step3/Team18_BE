package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeBriefResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;

import java.util.List;

public interface NoticeService {

    List<NoticeBriefResponseDto> getAllNotices(Long page, Long size);

    NoticeResponseDto getNoticeById(Long noticeId);

}
