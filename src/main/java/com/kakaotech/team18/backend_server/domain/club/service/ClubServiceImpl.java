package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubResponse;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ClubServiceImpl {

    private final ClubRepository clubRepository;

    public ClubServiceImpl(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public List<ClubResponse> getClubByCategory(String category) {
        List<Club> clubs;

        if (category == null || category.isBlank()) {
            clubs = clubRepository.findAll();
        } else {
            final Category matchedCategory;
            try {
                matchedCategory = Category.valueOf(category);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(category);
            }
            clubs = clubRepository.findByCategory(matchedCategory);
        }

        return clubs.stream()
                .map(ClubResponse::from)
                .toList();
    }
}
