package com.kakaotech.team18.backend_server.global.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.global.exception.exceptions.InputStreamException;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @DisplayName("S3 파일 업로드 시 올바른 URL을 반환한다.")
    @Test
    void upload_shouldReturnFileUrl(){
        // given
        MultipartFile file = new MockMultipartFile(
                "image", "test.png", "image/png", "test data".getBytes()
        );

        String bucket = "test-bucket";
        ReflectionTestUtils.setField(s3Service, "bucket", bucket);

        // when
        String result = s3Service.upload(file);

        // then
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        Assertions.assertThat(result).contains("https://").contains(bucket);
    }

    @Test
    @DisplayName("S3 파일 삭제 시 올바른 객체가 삭제된다")
    void deleteFile_shouldDeleteObjectFromS3() {
        // given
        String bucket = "test-bucket";
        String url = "https://test-bucket.s3.amazonaws.com/club_detail_image/test.png";
        ReflectionTestUtils.setField(s3Service, "bucket", bucket);

        // when
        s3Service.deleteFile(url);

        // then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("S3 파일 업로드 시 InputStream 예외가 발생하면 InputStreamException을 던진다.")
    void upload_shouldThrowInputStreamException_whenIOExceptionOccurs() throws IOException {
        // given
        MultipartFile file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn("test.png");
        given(file.getContentType()).willReturn("image/png");
        given(file.getSize()).willReturn(100L);
        given(file.getInputStream()).willThrow(new IOException("Test IOException"));

        // when & then
        Assertions.assertThatThrownBy(() -> s3Service.upload(file))
                .isInstanceOf(InputStreamException.class)
                .hasMessageContaining("파일 입출력 실패.");
    }
}