package com.kakaotech.team18.backend_server.domain.club.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.domain.club.repository.ClubImageRepository;
import com.kakaotech.team18.backend_server.global.service.S3Service;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrphanImageCleanupServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private ClubImageRepository clubImageRepository;

    @InjectMocks
    private OrphanImageCleanupService cleanupService;

    @Test
    @DisplayName("DB에는 없고 S3에만 존재하는 이미지가 삭제된다")
    void cleanupOrphanImagesLogic_deletesOnlyOrphanFiles() {
        // given
        List<String> allS3Urls = List.of(
                "https://bucket.s3.amazonaws.com/a.jpg",
                "https://bucket.s3.amazonaws.com/b.jpg",
                "https://bucket.s3.amazonaws.com/c.jpg"
        );
        List<String> validUrls = List.of(
                "https://bucket.s3.amazonaws.com/a.jpg"
        );

        given(s3Service.listAllFiles()).willReturn(allS3Urls);
        given(clubImageRepository.findAllImageUrls()).willReturn(validUrls);

        // when
        int deletedCount = cleanupService.cleanupOrphanImagesLogic();

        // then
        assertThat(deletedCount).isEqualTo(2); // b, c 삭제
        verify(s3Service, times(1)).deleteFile("https://bucket.s3.amazonaws.com/b.jpg");
        verify(s3Service, times(1)).deleteFile("https://bucket.s3.amazonaws.com/c.jpg");
        verify(s3Service, never()).deleteFile("https://bucket.s3.amazonaws.com/a.jpg");
    }
}