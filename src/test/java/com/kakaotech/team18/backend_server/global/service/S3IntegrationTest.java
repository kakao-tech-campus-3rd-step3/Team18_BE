package com.kakaotech.team18.backend_server.global.service;

import com.amazonaws.services.s3.AmazonS3;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class S3IntegrationTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3)
            .withExposedPorts(4566);

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private S3Service s3Service;

    @Test
    @Disabled("LocalStack 환경 필요 - CI에서는 무시")
    void uploadAndDeleteFile() throws Exception {
        // given
        String bucketName = "test-bucket213";
        amazonS3.createBucket(bucketName);
        ReflectionTestUtils.setField(s3Service, "bucket", bucketName);

        MultipartFile file = new MockMultipartFile(
                "image", "sample.png", "image/png", "data".getBytes()
        );

        // when
        String url = s3Service.upload(file);

        // then
        assertThat(amazonS3.doesObjectExist(bucketName, extractKey(url))).isTrue();

        s3Service.deleteFile(url);
        assertThat(amazonS3.doesObjectExist(bucketName, extractKey(url))).isFalse();
    }

    private String extractKey(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}