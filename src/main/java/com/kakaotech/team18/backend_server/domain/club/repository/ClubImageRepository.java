package com.kakaotech.team18.backend_server.domain.club.repository;

import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClubImageRepository extends JpaRepository<ClubImage, Long> {

    @Query("SELECT ci.imageUrl FROM ClubImage ci")
    List<String> findAllImageUrls();
}
