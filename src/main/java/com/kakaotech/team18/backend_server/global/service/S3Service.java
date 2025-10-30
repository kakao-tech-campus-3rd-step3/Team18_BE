package com.kakaotech.team18.backend_server.global.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InputStreamException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidFileException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.S3Exception;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png"
    );

    public List<String> listAllFiles() {
        try {
            List<String> urls = new ArrayList<>();
            String continuationToken = null;
            do {
                ListObjectsV2Request request = new ListObjectsV2Request()
                        .withBucketName(bucket)
                        .withContinuationToken(continuationToken);
                ListObjectsV2Result result = amazonS3.listObjectsV2(request);

                result.getObjectSummaries().stream()
                        .map(S3ObjectSummary::getKey)
                        .map(key -> amazonS3.getUrl(bucket, key).toString())
                        .forEach(urls::add);

                continuationToken = result.isTruncated() ? result.getNextContinuationToken() : null;
            } while (continuationToken != null);

            if (urls.isEmpty()) {
                log.info("S3 bucket [{}] has no objects.", bucket);
            }
            return urls;
        } catch (AmazonServiceException e) {
            log.warn("AWS returned an error while listing objects in bucket [{}]: {}", bucket, e.getMessage());
            throw new S3Exception("S3 객체 목록 조회 실패 (AWS 오류) bucket=" + bucket);

        } catch (SdkClientException e) {
            log.warn("SDK client error while connecting to S3: {}", e.getMessage());
            throw new S3Exception("S3 객체 목록 조회 실패 (네트워크 오류) bucket=" + bucket);

        } catch (Exception e) {
            log.error("Unexpected error while listing S3 objects: {}", e.getMessage(), e);
            throw new S3Exception("S3 객체 목록 조회 실패 (예상치 못한 오류) bucket=" + bucket);
        }
    }

    public String upload(MultipartFile file)  {
        if (file.isEmpty()) {
            throw new InvalidFileException("빈 파일은 업로드할 수 없습니다.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("JPG 또는 PNG 파일만 업로드 가능합니다.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new InvalidFileException("파일명이 없습니다.");
        }

        originalName = originalName.toLowerCase();

        if (!(originalName.endsWith(".png") || originalName.endsWith(".jpg") || originalName.endsWith(".jpeg"))) {
            throw new InvalidFileException("확장자가 JPG 또는 PNG가 아닙니다.");
        }

        String dir = "club_detail_image/";
        String fileName = dir + UUID.randomUUID() + "-" + originalName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new InputStreamException("S3 파일 업로드 과정에서 에러가 발생했습니다.");
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public void deleteFile(String fileUrl) {
        String path = URI.create(fileUrl).getPath(); // ex) "/club_detail_image/..."
        String key = path.startsWith("/") ? path.substring(1) : path;
        try {
            amazonS3.deleteObject(bucket, key);
        } catch (AmazonServiceException e) {
            // AWS 응답 오류 (권한, 버킷, region 문제 등)
            log.warn("AWS S3 AmazonServiceException error: [{}], key: [{}]", e.getMessage(), key);
            throw new S3Exception("S3 객체 삭제 실패 (AWS 오류) key=" + key);
        } catch (SdkClientException e) {
            // 네트워크 / 연결 오류
            log.warn("AWS S3 SdkClientException error: [{}], key: [{}]", e.getMessage(), key);
            throw new S3Exception("S3 객체 삭제 실패 (네트워크 오류) key=" + key);
        }
    }
}
