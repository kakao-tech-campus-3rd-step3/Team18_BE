package com.kakaotech.team18.backend_server.domain.notices.controller;

import com.kakaotech.team18.backend_server.domain.notices.dto.NoticePageResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "공지사항 API", description = "공지사항 목록 및 상세 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(
            summary = "공지사항 목록 조회",
            description = """
                    전체 공지사항 목록을 페이지 단위로 조회합니다.
                    - `page`: 1부터 시작하는 페이지 번호
                    - `size`: 페이지당 항목 수
                    - 반환은 `id` 기준 오름차순 정렬됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping
    public ResponseEntity<NoticePageResponseDto> getAllNotices(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(value = "page", defaultValue = "1")@Min(1) Integer page,

            @Parameter(description = "페이지당 항목 수 ", example = "10")
            @RequestParam(value = "size", defaultValue = "10")@Min(1) @Max(100) Integer size
    ) {
        NoticePageResponseDto response = noticeService.getAllNotices(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항 ID를 이용해 단일 공지의 상세 내용을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeResponseDto> getNoticeById(
            @Parameter(description = "조회할 공지사항의 ID", example = "1")
            @PathVariable("noticeId") Long noticeId
    ) {
        NoticeResponseDto response = noticeService.getNoticeById(noticeId);
        return ResponseEntity.ok(response);
    }
}