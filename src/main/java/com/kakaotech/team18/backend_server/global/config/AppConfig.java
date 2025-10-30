package com.kakaotech.team18.backend_server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Value("${api.rest-client.connect-timeout}")
    private int connectTimeout;

    @Value("${api.rest-client.read-timeout}")
    private int readTimeout;

    /**
     * 외부 서버(카카오 등)와의 HTTP 통신을 위해 RestClient를 Spring 컨테이너에 Bean으로 등록합니다.
     * SimpleClientHttpRequestFactory를 사용하여 연결 및 읽기 타임아웃을 설정하여,
     * 외부 서비스의 응답 지연이 전체 애플리케이션에 영향을 미치는 것을 방지합니다.
     *
     * @return 타임아웃이 설정된 RestClient 객체
     */
    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
