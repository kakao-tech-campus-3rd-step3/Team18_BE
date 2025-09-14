package com.kakaotech.team18.backend_server.domain.comment.service;

import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;

import java.util.List;

/**
 * 댓글 관련 비즈니스 로직의 명세를 정의하는 인터페이스입니다.
 */
public interface CommentService {

    /**
     * 특정 지원서에 달린 모든 댓글을 조회합니다.
     *
     * @param applicationId 지원서 ID
     * @return 댓글 정보 DTO 리스트
     */
    List<CommentResponseDto> getComments(Long applicationId);
}
