package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.user.entity.Users;
import com.kakaotech.team18.backend_server.domain.user.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final UsersRepository usersRepository;

    public ClubServiceImpl(
            ClubRepository clubRepository,
            UsersRepository usersRepository
    ) {
        this.clubRepository = clubRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    public List<ClubListResponseDto> getClubByCategory(Category category) {
        if (category == null) {
            return mapToResponse(clubRepository.findAllSummaries());
        }
        return mapToResponse(clubRepository.findSummariesByCategory(category));
    }

    @Override
    public List<ClubListResponseDto> getClubByName(String name) {
        if (name == null || name.isBlank()) {
            return mapToResponse(clubRepository.findAllSummaries());
        }
        return mapToResponse(clubRepository.findSummariesByNameContaining(name));
    }

    @Override
    public List<ClubListResponseDto> getAllClubs() {
        return mapToResponse(clubRepository.findAllSummaries());
    }

    @Override
    public ClubDetailResponseDto getClubDetail(Long clubId) {
        Club findClub = clubRepository.findById(clubId).orElseThrow(NoSuchElementException::new);
        Users findUser = usersRepository.findById(findClub.getPresident().getId()).orElseThrow(NoSuchElementException::new);
        return ClubDetailResponseDto.from(findClub, findUser);
    }

    // ---- private helpers ----
    private List<ClubListResponseDto> mapToResponse(List<ClubSummary> summaries) {
        return summaries.stream()
                .map(s -> ClubListResponseDto.from(
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
