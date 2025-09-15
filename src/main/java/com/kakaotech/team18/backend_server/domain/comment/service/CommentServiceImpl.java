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
import com.kakaotech.team18.backend_server.global.exception.exceptions.CommentAccessDeniedException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CommentNotFoundException;
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
        this.validateRating(commentRequestDto.rating());

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("해당 지원서를 찾을 수 없습니다. ID: " + applicationId));

        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        Comment newComment = Comment.builder()
                .content(commentRequestDto.content())
                .rating(commentRequestDto.rating())
                .application(application)
                .user(user)
                .build();

        commentRepository.save(newComment);

        this.updateApplicationAverageRating(application.getId());

        return CommentResponseDto.from(newComment);
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto, Long userId) {
        this.validateRating(commentRequestDto.rating());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다. ID: " + commentId));

        this.validateCommentOwner(comment, userId);

        comment.update(commentRequestDto.content(), commentRequestDto.rating());

        this.updateApplicationAverageRating(comment.getApplication().getId());

        return CommentResponseDto.from(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다. ID: " + commentId));

        this.validateCommentOwner(comment, userId);

        Long applicationId = comment.getApplication().getId();

        commentRepository.delete(comment);

        this.updateApplicationAverageRating(applicationId);
    }

    private void validateRating(Double rating) {
        if (rating % 0.5 != 0) {
            throw new InvalidRatingUnitException();
        }
    }

    private void validateCommentOwner(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new CommentAccessDeniedException("해당 댓글을 수정/삭제할 권한이 없습니다. 사용자 ID: " + userId);
        }
    }

    private void updateApplicationAverageRating(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("해당 지원서를 찾을 수 없습니다. ID: " + applicationId));

        List<Comment> comments = commentRepository.findByApplicationIdWithUser(applicationId);
        double average = comments.stream()
                .mapToDouble(Comment::getRating)
                .average()
                .orElse(0.0);

        double roundedAverage = Math.round(average * 10.0) / 10.0;

        application.updateAverageRating(roundedAverage);
    }
}
