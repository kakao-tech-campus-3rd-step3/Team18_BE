package com.kakaotech.team18.backend_server.domain.club.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubIntroduction;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ClubRestClientTest {

    @Autowired
    private ClubRepository clubRepository;


    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        Club club = Club.builder()
                .name("Test Club")
                .category(com.kakaotech.team18.backend_server.domain.club.entity.Category.STUDY)
                .shortIntroduction("Test Short Introduction")
                .location("Test Location")
                .introduction(ClubIntroduction.builder()
                        .overview("Test Overview")
                        .activities("Test Activities")
                        .ideal("Test Ideal")
                        .build())
                .build();
        clubRepository.save(club);
    }

    @AfterEach
    void tearDown() {
        clubRepository.deleteAll();
    }

    @DisplayName("동아리 이미지 업로드 시, 5MB 이하의 파일은 성공한다.")
    @Test
    void uploadWithinLimit_200OK() {

        // RestClient 인스턴스
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // given: 5MB
        byte[] smallFile = new byte[5 * 1024 * 1024];

        ByteArrayResource resource = new ByteArrayResource(smallFile) {
            @Override
            public String getFilename() {
                return "valid.jpg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("images", resource);

        // when
        var response = restClient.put()
                .uri("/api/clubs/1/images")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @DisplayName("동아리 이미지 업로드 시, 5MB 초과 파일은 실패한다.")
    @Test
    void uploadTooLargeFile_exceed1MB() {

        // RestClient 인스턴스
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // given: 6MB 파일
        byte[] bigFile = new byte[6 * 1024 * 1024];

        ByteArrayResource resource = new ByteArrayResource(bigFile) {
            @Override
            public String getFilename() {
                return "big.jpg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("images", resource);

        // when & then: Tomcat에서 연결을 끊기 때문에 ResourceAccessException 발생
        assertThatThrownBy(() ->
                restClient.put()
                        .uri("/api/clubs/1/images")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity()
        ).isInstanceOfAny(
                ResourceAccessException.class,
                HttpClientErrorException.BadRequest.class
        );
    }

    @DisplayName("동아리 이미지 업로드 시, 총 50MB 초과 파일은 실패한다.")
    @Test
    void uploadTotalSizeExceed_throwsException() {

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // 5MB 파일 11개 → 총 55MB
        byte[] fileData = new byte[5 * 1024 * 1024];

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (int i = 0; i < 11; i++) {
            ByteArrayResource resource = new ByteArrayResource(fileData) {
                @Override
                public String getFilename() {
                    return "image_" + System.nanoTime() + ".jpg";
                }
            };
            body.add("images", resource);
        }

        assertThatThrownBy(() ->
                restClient.put()
                        .uri("/api/clubs/1/images")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity()
        ).isInstanceOfAny(
                ResourceAccessException.class,
                HttpClientErrorException.BadRequest.class
        );
    }
}