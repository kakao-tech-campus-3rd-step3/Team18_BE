package com.kakaotech.team18.backend_server.domain.comment.controller;

import com.kakaotech.team18.backend_server.domain.comment.dto.CommentRequestDto;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;
import com.kakaotech.team18.backend_server.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{applicationId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable("applicationId") Long applicationId,
            @Valid @RequestBody CommentRequestDto commentRequestDto
    ) {
        // TODO: Spring Security 적용 후 실제 사용자 ID를 가져와야 함
        Long currentUserId = 1L; // 임시 사용자 ID

        CommentResponseDto newComment = commentService.createComment(applicationId, commentRequestDto, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @PatchMapping("/{applicationId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable("applicationId") Long applicationId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentRequestDto commentRequestDto
    ) {
        // TODO: Spring Security 적용 후 실제 사용자 ID를 가져와야 함
        Long currentUserId = 1L; // 임시 사용자 ID

        CommentResponseDto updatedComment = commentService.updateComment(commentId, commentRequestDto, currentUserId);

        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{applicationId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("applicationId") Long applicationId,
            @PathVariable("commentId") Long commentId
    ) {
        // TODO: Spring Security 적용 후 실제 사용자 ID를 가져와야 함
        Long currentUserId = 1L; // 임시 사용자 ID

        commentService.deleteComment(commentId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
