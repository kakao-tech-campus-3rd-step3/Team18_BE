package com.kakaotech.team18.backend_server.domain.comment.repository;

import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
            SELECT c
            FROM Comment c
            JOIN FETCH c.user
            WHERE c.application.id = :applicationId
            ORDER BY c.createdAt ASC""")
    List<Comment> findByApplicationIdWithUser(Long applicationId);
}
