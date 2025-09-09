package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubResponse;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.kakaotech.team18.backend_server.domain.club.repository.dto.ClubSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;

    public ClubServiceImpl(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    @Override
    public List<ClubResponse> getClubByCategory(Category category) {
        if (category == null) {
            return mapToResponse(clubRepository.findAllSummaries());
        }
        return mapToResponse(clubRepository.findSummariesByCategory(category));
    }

    @Override
    public List<ClubResponse> getClubByName(String name) {
        if (name == null || name.isBlank()) {
            return mapToResponse(clubRepository.findAllSummaries());
        }
        return mapToResponse(clubRepository.findSummariesByNameContaining(name));
    }

    @Override
    public List<ClubResponse> getAllClubs() {
        return mapToResponse(clubRepository.findAllSummaries());
    }

    // ---- private helpers ----
    private List<ClubResponse> mapToResponse(List<ClubSummary> summaries) {
        return summaries.stream()
                .map(s -> ClubResponse.from(
                        s,
                        calculateRecruitStatus(s.recruitStart(), s.recruitEnd())
                ))
                .toList();
    }

    private String calculateRecruitStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime today = LocalDateTime.now();

        if (start == null || end == null) {
            return "모집 일정 미정";
        }
        if (today.isBefore(start)) {
            return "모집 준비중";
        }
        if (!today.isBefore(end)) {
            return "모집 종료";
        }
        return "모집중";
    }
}
