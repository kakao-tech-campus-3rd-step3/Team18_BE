package com.kakaotech.team18.backend_server.domain.clubPopularity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.persistence.EntityManager;

@DataJpaTest
class ClubViewBatchRepositoryDataJpaTest {

    @Autowired ClubRepository clubRepository;
    @Autowired ClubViewRepository clubViewRepository;
    @Autowired @Qualifier("clubViewRepository") ClubViewBatchRepository clubViewBatchRepository;
    @Autowired EntityManager entityManager;

    @Test
    void h2UpsertKeepsNewestTimestampAndOneRowPerViewer() {
        Club club = clubRepository.saveAndFlush(Club.builder()
                .name("batch-test")
                .category(Category.STUDY)
                .location("online")
                .isInterviewRequired(false)
                .isRegistered(false)
                .build());

        clubViewBatchRepository.upsertUser(club.getId(), 15L, Instant.ofEpochSecond(20));
        clubViewBatchRepository.upsertUser(club.getId(), 15L, Instant.ofEpochSecond(10));
        entityManager.clear();
        assertThat(clubViewRepository.findAll().get(0).getLastViewedAt())
                .isEqualTo(Instant.ofEpochSecond(20));
        clubViewBatchRepository.upsertUser(club.getId(), 15L, Instant.ofEpochSecond(30));
        entityManager.clear();

        assertThat(clubViewRepository.findAll()).hasSize(1);
        assertThat(clubViewRepository.findAll().get(0).getLastViewedAt())
                .isEqualTo(Instant.ofEpochSecond(30));
    }
}
