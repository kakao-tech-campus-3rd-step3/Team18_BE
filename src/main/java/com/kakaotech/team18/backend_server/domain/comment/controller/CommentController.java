package com.kakaotech.team18.backend_server.domain.comment.controller;

import com.kakaotech.team18.backend_server.domain.comment.dto.CommentRequestDto;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;
import com.kakaotech.team18.backend_server.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글/리뷰 API", description = "지원서에 대한 댓글 및 별점 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "특정 지원서의 모든 댓글 조회", description = "특정 지원서 ID를 받아, 해당 지원서에 달린 모든 댓글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{applicationId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @Parameter(description = "지원서의 고유 ID", required = true, example = "100") @PathVariable("applicationId") Long applicationId
    ) {
        List<CommentResponseDto> comments = commentService.getComments(applicationId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 작성", description = "특정 지원서에 새로운 댓글과 별점을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "404", description = "댓글을 작성할 지원서를 찾을 수 없음")
    })
    @PostMapping("/{applicationId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @Parameter(description = "지원서의 고유 ID", required = true, example = "100") @PathVariable("applicationId") Long applicationId,
            @Valid @RequestBody CommentRequestDto commentRequestDto
    ) {
        // TODO: Spring Security 적용 후 실제 사용자 ID를 가져와야 함
        Long currentUserId = 1L; // 임시 사용자 ID

        CommentResponseDto newComment = commentService.createComment(applicationId, commentRequestDto, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @Operation(summary = "댓글 수정", description = "자신이 작성한 댓글의 내용과 별점을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "댓글을 수정할 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "수정할 댓글을 찾을 수 없음")
    })
    @PatchMapping("/{applicationId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @Parameter(description = "관련 지원서의 고유 ID (URL 일관성을 위해 포함)", example = "100") @PathVariable("applicationId") Long applicationId,
            @Parameter(description = "수정할 댓글의 고유 ID", required = true, example = "20") @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentRequestDto commentRequestDto
    ) {
        // TODO: Spring Security 적용 후 실제 사용자 ID를 가져와야 함
        Long currentUserId = 1L; // 임시 사용자 ID

        CommentResponseDto updatedComment = commentService.updateComment(commentId, commentRequestDto, currentUserId);

        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "댓글 삭제", description = "자신이 작성한 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "댓글을 삭제할 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "삭제할 댓글을 찾을 수 없음")
    })
    @DeleteMapping("/{applicationId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "관련 지원서의 고유 ID (URL 일관성을 위해 포함)", example = "100") @PathVariable("applicationId") Long applicationId,
            @Parameter(description = "삭제할 댓글의 고유 ID", required = true, example = "20") @PathVariable("commentId") Long commentId
    ) {
        // TODO: Spring Security 적용 후 실제 사용자 ID를 가져와야 함
        Long currentUserId = 1L; // 임시 사용자 ID

        commentService.deleteComment(commentId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
