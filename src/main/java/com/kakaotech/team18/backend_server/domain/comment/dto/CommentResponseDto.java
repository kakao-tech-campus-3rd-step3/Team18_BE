package com.kakaotech.team18.backend_server.domain.comment.dto;

import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import com.kakaotech.team18.backend_server.domain.user.entity.User;

import java.time.LocalDateTime;

public record CommentResponseDto(
    Long commentId,
    String content,
    Double rating,
    AuthorDto author,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
            comment.getId(),
            comment.getContent(),
            comment.getRating(),
            AuthorDto.from(comment.getUser()),
            comment.getCreatedAt(),
            comment.getLastModifiedAt()
        );
    }

    public record AuthorDto(
        Long id,
        String name
    ) {
        public static AuthorDto from(User user) {
            return new AuthorDto(user.getId(), user.getName());
        }
    }
}
