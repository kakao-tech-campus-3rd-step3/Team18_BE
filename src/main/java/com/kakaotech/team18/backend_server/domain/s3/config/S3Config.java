package com.kakaotech.team18.backend_server.domain.s3.config;

import com.kakaotech.team18.backend_server.domain.s3.properties.S3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties props) {
        AwsBasicCredentials creds = AwsBasicCredentials.create(
                props.credentials().accessKey(),
                props.credentials().secretKey()
        );
        return S3Client.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties props) {
        AwsBasicCredentials creds = AwsBasicCredentials.create(
                props.credentials().accessKey(),
                props.credentials().secretKey()
        );
        return S3Presigner.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build();
    }
}