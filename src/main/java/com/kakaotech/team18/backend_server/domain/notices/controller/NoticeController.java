package com.kakaotech.team18.backend_server.domain.notices.controller;

import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeBriefResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.dto.NoticeResponseDto;
import com.kakaotech.team18.backend_server.domain.notices.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "공지사항 API", description = "공지사항 list 및 상세내용 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation
    @ApiResponse
    @GetMapping
    public ResponseEntity<List<NoticeBriefResponseDto>> getAllNotices(
            @Parameter @RequestParam("page") Long page,
            @Parameter @RequestParam("size") Long size
    ) {
        List<NoticeBriefResponseDto> response =  noticeService.getAllNotices(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation
    @ApiResponse
    @GetMapping("/{noticesId}")
    public ResponseEntity<NoticeResponseDto> getNoticeById(
            @Parameter @RequestParam("noticeId") Long noticeId
    ){
        NoticeResponseDto response = noticeService.getNoticeById(noticeId);
        return ResponseEntity.ok(response);
    }

}
