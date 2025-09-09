package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.Users;
import com.kakaotech.team18.backend_server.domain.user.repository.UsersRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UsersRepository usersRepository;


    public ClubDetailResponseDto getClubDetail(Long clubId) {
        Club findClub = clubRepository.findById(clubId).orElseThrow(NoSuchElementException::new);
        Users findUser = usersRepository.findById(findClub.getPresident().getId()).orElseThrow(NoSuchElementException::new);
        return ClubDetailResponseDto.from(findClub, findUser);
    }
}
