package com.kakaotech.team18.backend_server.domain.comment.dto;

import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "댓글 조회 시 응답 데이터")
public record CommentResponseDto(
    @Schema(description = "댓글 고유 ID", example = "20") Long commentId,
    @Schema(description = "댓글 내용", example = "지원자의 경험이 우리 동아리와 잘 맞는 것 같습니다.") String content,
    @Schema(description = "별점", example = "4.5") Double rating,
    @Schema(description = "작성자 정보") AuthorDto author,
    @Schema(description = "생성일") LocalDateTime createdAt,
    @Schema(description = "최종 수정일") LocalDateTime updatedAt
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

    @Schema(description = "댓글 작성자 정보")
    public record AuthorDto(
        @Schema(description = "작성자 고유 ID (User ID)", example = "5") Long id,
        @Schema(description = "작성자 이름", example = "김운영") String name
    ) {
        public static AuthorDto from(User user) {
            return new AuthorDto(user.getId(), user.getName());
        }
    }
}
