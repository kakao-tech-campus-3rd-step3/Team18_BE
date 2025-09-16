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
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidRatingUnitException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
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
    void createComment_success(CapturedOutput output) {
        // given
        final Long applicationId = 1L;
        final Long userId = 1L;
        final CommentRequestDto requestDto = new CommentRequestDto("새로운 댓글입니다.", 4.5);

        User mockUser = mock(User.class);
        Application mockApplication = mock(Application.class);

        when(mockApplication.getId()).thenReturn(applicationId);
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(mockApplication));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(commentRepository.findByApplicationIdWithUser(applicationId)).thenReturn(Collections.emptyList());

        // when
        CommentResponseDto responseDto = commentService.createComment(applicationId, requestDto, userId);

        // then
        assertThat(responseDto.content()).isEqualTo("새로운 댓글입니다.");
        assertThat(responseDto.rating()).isEqualTo(4.5);

        assertThat(output).contains("댓글 생성 시도")
                .contains("applicationId: " + applicationId)
                .contains("댓글 생성 성공");

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
    void updateComment_success(CapturedOutput output) {
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
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(mockApplication));
        when(commentRepository.findByApplicationIdWithUser(applicationId)).thenReturn(Collections.singletonList(mockComment));

        // when
        commentService.updateComment(commentId, requestDto, userId);

        // then
        assertThat(output).contains("댓글 수정 시도")
                .contains("commentId: " + commentId)
                .contains("댓글 수정 성공");

        verify(mockComment, times(1)).update(requestDto.content(), requestDto.rating());
        verify(mockApplication, times(1)).updateAverageRating(anyDouble());
    }

    @Test
    @DisplayName("댓글 수정 - 실패 (권한 없음)")
    void updateComment_fail_accessDenied(CapturedOutput output) {
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
        assertThat(output).contains("댓글 수정 시도")
                .contains("댓글 접근 권한 없음")
                .contains("commentId: " + commentId)
                .contains("ownerId: " + ownerId)
                .contains("requesterId: " + requesterId);

        verify(mockComment, never()).update(anyString(), anyDouble());
        verify(applicationRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_success(CapturedOutput output) {
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
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(mockApplication));
        when(commentRepository.findByApplicationIdWithUser(applicationId)).thenReturn(Collections.emptyList());

        // when
        commentService.deleteComment(commentId, userId);

        // then
        assertThat(output).contains("댓글 삭제 시도")
                .contains("commentId: " + commentId)
                .contains("댓글 삭제 성공");

        verify(commentRepository, times(1)).delete(mockComment);
        verify(mockApplication, times(1)).updateAverageRating(anyDouble());
    }

    @Test
    @DisplayName("댓글 삭제 - 실패 (권한 없음)")
    void deleteComment_fail_accessDenied(CapturedOutput output) {
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
        assertThrows(CommentAccessDeniedException.class, () -> {
            commentService.deleteComment(commentId, requesterId);
        });

        // then
        assertThat(output).contains("댓글 삭제 시도")
                .contains("댓글 접근 권한 없음")
                .contains("commentId: " + commentId)
                .contains("ownerId: " + ownerId)
                .contains("requesterId: " + requesterId);

        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
