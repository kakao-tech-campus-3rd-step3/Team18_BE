package com.kakaotech.team18.backend_server.domain.comment.service;

import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;
import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import com.kakaotech.team18.backend_server.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    // 특정 지원서에 달린 모든 댓글을 조회하는 메소드
    @Override
    public List<CommentResponseDto> getComments(Long applicationId) {
        List<Comment> comments = commentRepository.findByApplicationIdWithUser(applicationId);
        
        return comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }
}
