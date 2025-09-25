package com.kakaotech.team18.backend_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * 외부 서버와의 HTTP 통신을 위해 RestTemplate을 Spring 컨테이너에 Bean으로 등록합니다.
     *
     * @return 기본 설정의 RestTemplate 객체
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
