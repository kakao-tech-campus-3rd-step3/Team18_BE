package com.kakaotech.team18.backend_server.domain.s3.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws.s3")
public record S3Properties(String region, Credentials credentials) {
    public record Credentials(String accessKey, String secretKey) {}
}