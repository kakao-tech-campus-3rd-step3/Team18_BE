package com.kakaotech.team18.backend_server.domain.club.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClubControllerRestClientTest {

    @LocalServerPort
    int port;

    @Test
    void uploadWithinLimit_200OK() {

        // RestClient 인스턴스
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // given: 500KB 정상 파일 (1MB 이하)
        byte[] smallFile = new byte[500 * 1024];

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

    @Test
    void uploadTooLargeFile_exceed1MB() {

        // RestClient 인스턴스
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // given: 2MB 파일
        byte[] bigFile = new byte[2 * 1024 * 1024];

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
}