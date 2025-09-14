package com.kakaotech.team18.backend_server.domain.comment.service;

import com.kakaotech.team18.backend_server.domain.comment.dto.CommentRequestDto;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;

import java.util.List;

public interface CommentService {

    List<CommentResponseDto> getComments(Long applicationId);

    /**
     * 특정 지원서에 새로운 댓글과 별점을 생성합니다.
     *
     * @param applicationId 댓글을 달 지원서 ID
     * @param commentRequestDto 댓글 내용과 별점 정보
     * @param userId 현재 로그인한 사용자의 ID (임시)
     * @return 생성된 댓글 정보 DTO
     */
    CommentResponseDto createComment(Long applicationId, CommentRequestDto commentRequestDto, Long userId);
}
