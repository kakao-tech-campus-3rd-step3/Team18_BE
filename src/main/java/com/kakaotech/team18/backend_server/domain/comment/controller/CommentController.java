package com.kakaotech.team18.backend_server.domain.comment.controller;

import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;
import com.kakaotech.team18.backend_server.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{applicationId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable("applicationId") Long applicationId
    ) {
        List<CommentResponseDto> comments = commentService.getComments(applicationId);
        return ResponseEntity.ok(comments);
    }
}
