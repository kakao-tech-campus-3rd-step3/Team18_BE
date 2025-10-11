package com.kakaotech.team18.backend_server.domain.club.repository;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findById(Long id);

    @EntityGraph(attributePaths = {"cautions","cautions.items","introduction.images"})
    Optional<Club> findClubDetailById(Long id);

    Optional<Club> findByName(String name);

    List<Club> findByCategory(Category category);

    List<ClubSummary> findAllProjectedBy();

    Optional<ClubSummary> findSummaryByName(String name);

    List<ClubSummary> findSummariesByCategory(Category category);

    List<ClubSummary> findSummariesByNameContaining(String name);

}
