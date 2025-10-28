package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeBriefResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticePageResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;

import java.util.List;

public interface NoticeService {

    NoticePageResponseDto getAllNotices(Integer page, Integer size);

    NoticeResponseDto getNoticeById(Long noticeId);

}
