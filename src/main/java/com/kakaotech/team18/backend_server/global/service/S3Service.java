package com.kakaotech.team18.backend_server.global.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InputStreamException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidFileException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.S3DeleteException;
import java.io.IOException;
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

    public String upload(MultipartFile file)  {
        if (file.isEmpty()) {
            throw new InvalidFileException("빈 파일은 업로드할 수 없습니다.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("JPG 또는 PNG 파일만 업로드 가능합니다.");
        }

        String originalName = file.getOriginalFilename().toLowerCase();

        if (!(originalName.endsWith(".png") || originalName.endsWith(".jpg") || originalName.endsWith(".jpeg"))) {
            throw new InvalidFileException("확장자가 JPG 또는 PNG가 아닙니다.");
        }

        String dir = "club_detail_image/";
        String fileName = dir + UUID.randomUUID() + "-" + file.getOriginalFilename();

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
        String key = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
        try {
            amazonS3.deleteObject(bucket, key);
        } catch (AmazonServiceException e) {
            // AWS 응답 오류 (권한, 버킷, region 문제 등)
            log.warn("AWS S3 AmazonServiceException error: [{}], key: [{}]", e.getMessage(), key);
            throw new S3DeleteException("S3 객체 삭제 실패 (AWS 오류) key=" + key);
        } catch (SdkClientException e) {
            // 네트워크 / 연결 오류
            log.warn("AWS S3 SdkClientException error: [{}], key: [{}]", e.getMessage(), key);
            throw new S3DeleteException("S3 객체 삭제 실패 (네트워크 오류) key=" + key);
        }
    }
}
