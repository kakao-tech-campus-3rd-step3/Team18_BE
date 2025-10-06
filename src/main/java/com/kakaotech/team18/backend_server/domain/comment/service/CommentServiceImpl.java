package com.kakaotech.team18.backend_server.domain.comment.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentRequestDto;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;
import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import com.kakaotech.team18.backend_server.domain.comment.repository.CommentRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CommentAccessDeniedException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CommentNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidRatingUnitException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

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
        log.info("댓글 생성 시도 - applicationId: {}, userId: {}", applicationId, userId);
        this.validateRating(commentRequestDto.rating());

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("해당 지원서를 찾을 수 없습니다. ID: " + applicationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        Comment newComment = Comment.builder()
                .content(commentRequestDto.content())
                .rating(commentRequestDto.rating())
                .application(application)
                .user(user)
                .build();

        commentRepository.save(newComment);

        this.updateApplicationAverageRating(application.getId());

        log.info("댓글 생성 성공 - commentId: {}", newComment.getId());
        return CommentResponseDto.from(newComment);
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto, Long userId) {
        log.info("댓글 수정 시도 - commentId: {}, userId: {}", commentId, userId);
        this.validateRating(commentRequestDto.rating());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다. ID: " + commentId));

        this.validateCommentOwner(comment, userId);

        comment.update(commentRequestDto.content(), commentRequestDto.rating());

        this.updateApplicationAverageRating(comment.getApplication().getId());

        log.info("댓글 수정 성공 - commentId: {}", commentId);
        return CommentResponseDto.from(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.info("댓글 삭제 시도 - commentId: {}, userId: {}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다. ID: " + commentId));

        this.validateCommentOwner(comment, userId);

        Long applicationId = comment.getApplication().getId();

        commentRepository.delete(comment);

        this.updateApplicationAverageRating(applicationId);
        log.info("댓글 삭제 성공 - commentId: {}", commentId);
    }

    private void validateRating(Double rating) {
        if (rating % 0.5 != 0) {
            throw new InvalidRatingUnitException();
        }
    }

    private void validateCommentOwner(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            log.warn("댓글 접근 권한 없음 - commentId: {}, ownerId: {}, requesterId: {}",
                    comment.getId(), comment.getUser().getId(), userId);
            throw new CommentAccessDeniedException("해당 댓글을 수정/삭제할 권한이 없습니다. 사용자 ID: " + userId);
        }
    }

    private void updateApplicationAverageRating(Long applicationId) {
        // 1. 비관적 락을 사용하여 Application 엔티티를 안전하게 조회합니다.
        Application application = applicationRepository.findByIdWithPessimisticLock(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("해당 지원서를 찾을 수 없습니다. ID: " + applicationId));

        // 2. DB에서 직접 계산한 평균 평점을 가져옵니다. 댓글이 없으면 0.0을 사용합니다.
        double average = commentRepository.findAverageRatingByApplicationId(applicationId)
                .orElse(0.0);

        // 3. 소수점 첫째 자리까지 반올림합니다.
        double roundedAverage = Math.round(average * 10.0) / 10.0;

        // 4. 조회한 Application 엔티티의 평균 평점을 업데이트합니다.
        application.updateAverageRating(roundedAverage);
    }
}
