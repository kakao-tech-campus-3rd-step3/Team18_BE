package com.kakaotech.team18.backend_server.domain.comment.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentRequestDto;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;
import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import com.kakaotech.team18.backend_server.domain.comment.repository.CommentRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UsersRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidRatingUnitException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UserNotFoundException;
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
    private final ApplicationRepository applicationRepository;
    private final UsersRepository usersRepository;

    @Override
    public List<CommentResponseDto> getComments(Long applicationId) {
        List<Comment> comments = commentRepository.findByApplicationIdWithUser(applicationId);
        return comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDto createComment(Long applicationId, CommentRequestDto commentRequestDto, Long userId) {
        // 1. 유효성 검사 및 엔티티 조회
        if (commentRequestDto.rating() % 0.5 != 0) {
            throw new InvalidRatingUnitException();
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("해당 지원서를 찾을 수 없습니다. ID: " + applicationId));

        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        // 2. 댓글 엔티티 생성 및 저장
        Comment newComment = Comment.builder()
                .content(commentRequestDto.content())
                .rating(commentRequestDto.rating())
                .application(application)
                .user(user)
                .build();

        commentRepository.save(newComment);

        // 3. 평균 별점 계산 및 업데이트
        List<Comment> comments = commentRepository.findByApplicationIdWithUser(applicationId);
        double average = comments.stream()
                .mapToDouble(Comment::getRating)
                .average()
                .orElse(0.0);

        double roundedAverage = Math.round(average * 10.0) / 10.0;

        application.updateAverageRating(roundedAverage);

        // 4. 응답 DTO 생성 및 반환
        return CommentResponseDto.from(newComment);
    }
}
