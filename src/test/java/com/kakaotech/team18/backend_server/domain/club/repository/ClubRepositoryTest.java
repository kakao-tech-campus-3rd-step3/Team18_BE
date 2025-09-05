package com.kakaotech.team18.backend_server.domain.club.repository;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
class ClubRepositoryTest extends DataJpaTestSupport{

    @Autowired
    private ClubRepository clubRepository;

    private Club buildClub(String name, Category category, String shortIntro,
                           LocalDateTime start, LocalDateTime end) {
        Club c = new Club();
        ReflectionTestUtils.setField(c, "name", name);
        ReflectionTestUtils.setField(c, "category", category);
        ReflectionTestUtils.setField(c, "shortIntroduction", shortIntro);
        ReflectionTestUtils.setField(c, "recruitStart", start);
        ReflectionTestUtils.setField(c, "recruitEnd", end);
        return c;
    }
}
