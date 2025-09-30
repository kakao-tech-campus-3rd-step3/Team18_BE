package com.kakaotech.team18.backend_server.global.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * 외부 서버(카카오 등)와의 HTTP 통신을 위해 RestTemplate을 Spring 컨테이너에 Bean으로 등록합니다.
     * RestTemplateBuilder를 사용하여 연결 및 읽기 타임아웃을 설정하여,
     * 외부 서비스의 응답 지연이 전체 애플리케이션에 영향을 미치는 것을 방지합니다.
     *
     * @param builder Spring Boot가 자동으로 구성해주는 RestTemplateBuilder
     * @return 타임아웃이 설정된 RestTemplate 객체
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(3)) // 연결 타임아웃 3초
                .readTimeout(Duration.ofSeconds(3))    // 읽기 타임아웃 3초
                .build();
    }
}
