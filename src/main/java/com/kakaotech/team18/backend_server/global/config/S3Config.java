package com.kakaotech.team18.backend_server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {

        // 로컬 개발 환경(키 존재)
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
        }

        // EC2 배포 환경(IAM Role 자동 사용)
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {

        // 로컬 개발 환경(키 존재)
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            return S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
        }

        // EC2 배포 환경(IAM Role 자동 사용)
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }
}
