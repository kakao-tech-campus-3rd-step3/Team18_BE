package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubResponse;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;

import java.time.Clock;
import java.util.List;

import com.kakaotech.team18.backend_server.domain.club.repository.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.service.model.RecruitStatus;
import com.kakaotech.team18.backend_server.domain.club.service.model.RecruitStatusCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final Clock clock;

    public ClubServiceImpl(ClubRepository clubRepository, Clock clock) {
        this.clubRepository = clubRepository;
        this.clock = clock;
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
                .map(s -> {
                    RecruitStatus status =
                            RecruitStatusCalculator.of(s.getRecruitStart(), s.getRecruitEnd(), clock);
                    return ClubResponse.from(s, status);
                })
                .toList();
    }
}
