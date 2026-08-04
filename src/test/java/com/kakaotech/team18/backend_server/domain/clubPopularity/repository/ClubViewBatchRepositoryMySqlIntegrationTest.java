package com.kakaotech.team18.backend_server.domain.clubPopularity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClubViewBatchRepositoryMySqlIntegrationTest {

    @Container
    private static final GenericContainer<?> MYSQL = new GenericContainer<>("mysql:8.0")
            .withEnv("MYSQL_DATABASE", "team18")
            .withEnv("MYSQL_USER", "team18")
            .withEnv("MYSQL_PASSWORD", "team18")
            .withEnv("MYSQL_ROOT_PASSWORD", "root")
            .withExposedPorts(3306)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                "jdbc:mysql://%s:%d/team18?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                        .formatted(MYSQL.getHost(), MYSQL.getMappedPort(3306)));
        registry.add("spring.datasource.username", () -> "team18");
        registry.add("spring.datasource.password", () -> "team18");
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired ClubRepository clubRepository;
    @Autowired ClubViewRepository clubViewRepository;
    @Autowired @Qualifier("clubViewRepository") ClubViewBatchRepository clubViewBatchRepository;

    @Test
    void mysqlUpsertKeepsNewestTimestampAndAnonymousIdentity() {
        Club club = clubRepository.saveAndFlush(Club.builder()
                .name("mysql-batch-test")
                .category(Category.STUDY)
                .location("online")
                .isInterviewRequired(false)
                .isRegistered(false)
                .build());

        clubViewBatchRepository.upsertUser(club.getId(), 15L, Instant.ofEpochSecond(20));
        clubViewBatchRepository.upsertUser(club.getId(), 15L, Instant.ofEpochSecond(10));
        clubViewBatchRepository.upsertAnonymous(club.getId(), "anonymous-key".getBytes(), Instant.ofEpochSecond(30));

        assertThat(clubViewRepository.findAll()).hasSize(2);
        assertThat(clubViewRepository.findAll()).filteredOn(view -> view.getUserId() != null)
                .singleElement().satisfies(view -> {
                    assertThat(view.getUserId()).isEqualTo(15L);
                    assertThat(view.getLastViewedAt()).isEqualTo(Instant.ofEpochSecond(20));
                });
        assertThat(clubViewRepository.findAll()).filteredOn(view -> view.getUserId() == null)
                .singleElement().satisfies(view -> {
                    assertThat(view.getAnonymousIdentity()).isEqualTo("anonymous-key".getBytes());
                    assertThat(view.getLastViewedAt()).isEqualTo(Instant.ofEpochSecond(30));
                });
    }
}
