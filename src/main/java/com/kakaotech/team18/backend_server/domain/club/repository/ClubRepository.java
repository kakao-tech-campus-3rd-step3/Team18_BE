package com.kakaotech.team18.backend_server.domain.club.repository;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

}
