package com.kakaotech.team18.backend_server.global.service;

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
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@RequiredArgsConstructor
@Service
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String region;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg",
        "image/png"
    );

    public List<String> listAllFiles() {
        try {
            List<String> urls = new ArrayList<>();
            String continuationToken = null;
            do {
                ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .continuationToken(continuationToken)
                    .build();
                ListObjectsV2Response result = s3Client.listObjectsV2(request);

                result.contents().stream()
                        .map(S3Object::key)
                        .map(key -> String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key)).forEach(urls::add);
                continuationToken =
                    result.isTruncated() ? result.nextContinuationToken() : null;
            } while (continuationToken != null);

            if (urls.isEmpty()) {
                log.info("S3 bucket [{}] has no objects.", bucket);
            }
            return urls;
        } catch (S3Exception e) {
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

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("빈 파일은 업로드할 수 없습니다.");
        }


        String contentType = file.getContentType();
        if (contentType == null) {
            throw new InvalidFileException("파일의 Content-Type을 확인할 수 없습니다.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException("JPG 또는 PNG 파일만 업로드 가능합니다.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new InvalidFileException("파일명이 없습니다.");
        }

        // 경로 구분자 제거 및 base name만 추출
        originalName = originalName.replaceAll("[\\\\/]", "");
        if (originalName.isBlank()) {
            throw new InvalidFileException("유효하지 않은 파일명입니다.");
        }
        originalName = originalName.toLowerCase(java.util.Locale.ROOT);

        if (!(originalName.endsWith(".png") || originalName.endsWith(".jpg") || originalName.endsWith(".jpeg"))) {
            throw new InvalidFileException("확장자가 JPG 또는 PNG가 아닙니다.");
        }

        String dir = "club_detail_image/";
        String fileName = dir + UUID.randomUUID() + "-" + originalName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

        try {
            s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new InputStreamException("S3 파일 업로드 과정에서 에러가 발생했습니다.");
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
    }

    public void deleteFile(String fileUrl) {
        String path = URI.create(fileUrl).getPath();
        String key = path.startsWith("/") ? path.substring(1) : path;
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            // AWS 응답 오류 (권한, 버킷, region 문제 등)
            log.warn("AWS S3 S3Exception error: [{}], key: [{}]", e.getMessage(), key);
            throw new S3Exception("S3 객체 삭제 실패 (AWS 오류) key=" + key);
        } catch (SdkClientException e) {
            // 네트워크 / 연결 오류
            log.warn("AWS S3 SdkClientException error: [{}], key: [{}]", e.getMessage(), key);
            throw new S3Exception("S3 객체 삭제 실패 (네트워크 오류) key=" + key);
        }
    }
}
