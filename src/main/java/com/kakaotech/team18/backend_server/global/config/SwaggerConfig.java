package com.kakaotech.team18.backend_server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("동아리움 API 명세서")
                        .version("v1.0.0")
                        .description("동아리 통합 플랫폼 - 동아리움의 모든 API에 대한 명세와 테스트를 제공합니다."));
    }
}
