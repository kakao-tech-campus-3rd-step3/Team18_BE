package com.kakaotech.team18.backend_server.global.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InputStreamException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file)  {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

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

    public void deleteFile(ClubImage clubImage) {
        String fileUrl = clubImage.getImageUrl();
        String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        amazonS3.deleteObject(bucket, key);
    }
}
