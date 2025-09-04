package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubResponse;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.Users;
import com.kakaotech.team18.backend_server.domain.user.repository.UsersRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UsersRepository usersRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public List<ClubListResponseDto> getClubByCategory(String category) {
        List<Club> clubs;

    public ClubDetailResponseDto getClubDetail(Long clubId) {
        Club findClub = clubRepository.findById(clubId).orElseThrow(NoSuchElementException::new);
        Users findUser = usersRepository.findById(findClub.getPresident().getId()).orElseThrow(NoSuchElementException::new);
        return ClubDetailResponseDto.from(findClub, findUser);
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
                .map(ClubDetailResponseDto::from)
                .toList();
    }
}
