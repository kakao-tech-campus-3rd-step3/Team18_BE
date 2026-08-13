package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.dto.ResultNotificationRequestSummariesResponse;
import com.kakaotech.team18.backend_server.domain.notification.repository.ResultNotificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResultNotificationQueryService {

    private final ResultNotificationRequestRepository repository;

    @Transactional(readOnly = true)
    public ResultNotificationRequestSummariesResponse findRequestSummaries(Long clubId, int limit) {
        return new ResultNotificationRequestSummariesResponse(
                repository.findSummariesByClubId(clubId, PageRequest.of(0, limit))
        );
    }
}
