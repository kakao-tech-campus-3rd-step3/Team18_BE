package com.kakaotech.team18.backend_server.global.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@Disabled
@Testcontainers
class S3IntegrationTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3)
            .withExposedPorts(4566);

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Service s3Service;

    @Test
    @Disabled("LocalStack 환경 필요 - CI에서는 무시")
    void uploadAndDeleteFile() {
        String bucketName = "test-bucket213";

        s3Client.createBucket(b -> b.bucket(bucketName));
        ReflectionTestUtils.setField(s3Service, "bucket", bucketName);

        MultipartFile file = new MockMultipartFile(
                "image", "sample.png", "image/png", "data".getBytes()
        );

        String url = s3Service.upload(file);

        String key = extractKey(url);
        assertThat(objectExists(bucketName, key)).isTrue();

        s3Service.deleteFile(url);
        assertThat(objectExists(bucketName, key)).isFalse();
    }

    private boolean objectExists(String bucket, String key) {
        try {
            s3Client.headObject(b -> b.bucket(bucket).key(key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private String extractKey(String url) {
        // URL 형식: https://{bucket}.s3.{region}.amazonaws.com/{key}
        // 세 번째 "/" 이후가 key
        int thirdSlash = url.indexOf("/", url.indexOf("//") + 2);
        return url.substring(thirdSlash + 1);    }
}