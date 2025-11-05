package com.kakaotech.team18.backend_server.domain.notices.service;

import com.kakaotech.team18.backend_server.domain.notices.dto.NoticePageResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;

public interface NoticeService {

    NoticePageResponseDto getAllNotices(Integer page, Integer size);

    NoticeResponseDto getNoticeById(Long noticeId);

}
