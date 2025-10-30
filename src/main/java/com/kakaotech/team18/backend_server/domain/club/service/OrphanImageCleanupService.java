package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.club.repository.ClubImageRepository;
import com.kakaotech.team18.backend_server.global.service.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrphanImageCleanupService {

    private final S3Service s3Service;
    private final ClubImageRepository clubImageRepository;

    public int cleanupOrphanImagesLogic() {
        List<String> allS3Urls = s3Service.listAllFiles();
        List<String> validUrls = clubImageRepository.findAllImageUrls();

        List<String> orphanUrls = allS3Urls.stream()
                .filter(url -> !validUrls.contains(url))
                .toList();

        orphanUrls.forEach(s3Service::deleteFile);
        return orphanUrls.size();  // 삭제된 개수를 반환 (테스트 검증용)
    }
}