package com.kakaotech.team18.backend_server.domain.comment.repository;

import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
            SELECT c
            FROM Comment c
            JOIN FETCH c.user
            WHERE c.application.id = :applicationId
            ORDER BY c.createdAt ASC""")
    List<Comment> findByApplicationIdWithUser(Long applicationId);

    @Query("""
             SELECT AVG(c.rating)
             FROM Comment c
             WHERE c.application.id = :applicationId""")
    Optional<Double> findAverageRatingByApplicationId(@Param("applicationId") Long applicationId);
}
