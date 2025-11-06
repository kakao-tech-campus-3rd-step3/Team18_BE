package com.kakaotech.team18.backend_server.domain.comment.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentRequestDto;
import com.kakaotech.team18.backend_server.domain.comment.dto.CommentResponseDto;
import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import com.kakaotech.team18.backend_server.domain.comment.repository.CommentRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CommentAccessDeniedException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CommentNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidRatingUnitException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("댓글 생성 - 성공")
    void createComment_success() {
        // given
        final Long applicationId = 1L;
        final Long userId = 1L;
        final CommentRequestDto requestDto = new CommentRequestDto("새로운 댓글입니다.", 4.5);

        User mockUser = mock(User.class);
        Application mockApplication = mock(Application.class);

        when(mockApplication.getId()).thenReturn(applicationId);
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(mockApplication));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(applicationRepository.findByIdWithPessimisticLock(applicationId)).thenReturn(Optional.of(mockApplication));
        when(commentRepository.findAverageRatingByApplicationId(applicationId)).thenReturn(Optional.of(4.5));

        // when
        CommentResponseDto responseDto = commentService.createComment(applicationId, requestDto, userId);

        // then
        assertThat(responseDto.content()).isEqualTo("새로운 댓글입니다.");
        assertThat(responseDto.rating()).isEqualTo(4.5);

        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(mockApplication, times(1)).updateAverageRating(anyDouble());
    }

    @Test
    @DisplayName("댓글 생성 - 실패 (잘못된 별점 단위)")
    void createComment_fail_invalidRatingUnit() {
        // given
        final Long applicationId = 1L;
        final Long userId = 1L;
        final CommentRequestDto requestDto = new CommentRequestDto("잘못된 별점 댓글", 4.7);

        // when & then
        assertThrows(InvalidRatingUnitException.class, () -> {
            commentService.createComment(applicationId, requestDto, userId);
        });

        verify(applicationRepository, never()).findById(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void updateComment_success() {
        // given
        final Long applicationId = 1L;
        final Long userId = 1L;
        final Long commentId = 1L;
        final CommentRequestDto requestDto = new CommentRequestDto("수정된 댓글입니다.", 3.0);

        User mockUser = mock(User.class);
        Application mockApplication = mock(Application.class);
        Comment mockComment = mock(Comment.class);

        when(mockUser.getId()).thenReturn(userId);
        when(mockApplication.getId()).thenReturn(applicationId);
        when(mockComment.getUser()).thenReturn(mockUser);
        when(mockComment.getApplication()).thenReturn(mockApplication);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(applicationRepository.findByIdWithPessimisticLock(applicationId)).thenReturn(Optional.of(mockApplication));
        when(commentRepository.findAverageRatingByApplicationId(applicationId)).thenReturn(Optional.of(3.0));

        // when
        commentService.updateComment(commentId, requestDto, userId);

        // then
        verify(mockComment, times(1)).update(requestDto.content(), requestDto.rating());
        verify(mockApplication, times(1)).updateAverageRating(anyDouble());
    }

    @Test
    @DisplayName("댓글 수정 - 실패 (권한 없음)")
    void updateComment_fail_accessDenied() {
        // given
        final Long commentId = 1L;
        final Long requesterId = 1L;
        final Long ownerId = 2L;
        final CommentRequestDto requestDto = new CommentRequestDto("수정 시도", 3.0);

        User mockOwner = mock(User.class);
        Comment mockComment = mock(Comment.class);

        when(mockComment.getId()).thenReturn(commentId);
        when(mockOwner.getId()).thenReturn(ownerId);
        when(mockComment.getUser()).thenReturn(mockOwner);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        // when & then
        assertThrows(CommentAccessDeniedException.class, () -> {
            commentService.updateComment(commentId, requestDto, requesterId);
        });

        // then
        verify(mockComment, never()).update(anyString(), anyDouble());
        verify(applicationRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("댓글 수정 - 실패 (잘못된 별점 단위)")
    void updateComment_fail_invalidRatingUnit() {
        // given
        final Long commentId = 1L;
        final Long userId = 1L;
        final CommentRequestDto requestDto = new CommentRequestDto("잘못된 별점 수정", 3.3);

        // when & then
        assertThrows(InvalidRatingUnitException.class, () -> {
            commentService.updateComment(commentId, requestDto, userId);
        });

        verify(commentRepository, never()).findById(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 - 실패 (존재하지 않는 댓글)")
    void updateComment_fail_commentNotFound() {
        // given
        final Long nonExistentCommentId = 999L;
        final Long userId = 1L;
        final CommentRequestDto requestDto = new CommentRequestDto("수정 시도", 3.0);

        // commentRepository.findById가 Optional.empty()를 반환하도록 설정
        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CommentNotFoundException.class, () -> {
            commentService.updateComment(nonExistentCommentId, requestDto, userId);
        });

        // then
        // 예외가 발생했으므로, 후속 로직인 평균 별점 계산 등이 호출되지 않았는지 검증
        verify(applicationRepository, never()).findByIdWithPessimisticLock(anyLong());
    }


    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_success() {
        // given
        final Long applicationId = 1L;
        final Long userId = 1L;
        final Long commentId = 1L;

        User mockUser = mock(User.class);
        Application mockApplication = mock(Application.class);
        Comment mockComment = mock(Comment.class);

        when(mockUser.getId()).thenReturn(userId);
        when(mockApplication.getId()).thenReturn(applicationId);
        when(mockComment.getUser()).thenReturn(mockUser);
        when(mockComment.getApplication()).thenReturn(mockApplication);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(applicationRepository.findByIdWithPessimisticLock(applicationId)).thenReturn(Optional.of(mockApplication));
        when(commentRepository.findAverageRatingByApplicationId(applicationId)).thenReturn(Optional.of(0.0));

        // when
        commentService.deleteComment(commentId, userId);

        // then
        verify(commentRepository, times(1)).delete(mockComment);
        verify(mockApplication, times(1)).updateAverageRating(anyDouble());
    }

    @Test
    @DisplayName("댓글 삭제 - 실패 (권한 없음)")
    void deleteComment_fail_accessDenied() {
        // given
        final Long commentId = 1L;
        final Long requesterId = 1L;
        final Long ownerId = 2L;

        User mockOwner = mock(User.class);
        Comment mockComment = mock(Comment.class);

        when(mockComment.getId()).thenReturn(commentId);
        when(mockOwner.getId()).thenReturn(ownerId);
        when(mockComment.getUser()).thenReturn(mockOwner);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        // when & then
        // 예외가 발생하는지 먼저 검증
        assertThrows(CommentAccessDeniedException.class, () -> {
            commentService.deleteComment(commentId, requesterId);
        });

        // then
        // 예외 발생 후, 그 전까지의 로그가 올바르게 출력되었는지 검증
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 삭제 - 실패 (존재하지 않는 댓글)")
    void deleteComment_fail_commentNotFound() {
        // given
        final Long nonExistentCommentId = 999L;
        final Long userId = 1L;

        // commentRepository.findById가 Optional.empty()를 반환하도록 설정
        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CommentNotFoundException.class, () -> {
            commentService.deleteComment(nonExistentCommentId, userId);
        });

        // then
        // 예외가 발생했으므로, 후속 로직인 delete나 평균 별점 계산 등이 호출되지 않았는지 검증
        verify(commentRepository, never()).delete(any(Comment.class));
        verify(applicationRepository, never()).findByIdWithPessimisticLock(anyLong());
    }
}
