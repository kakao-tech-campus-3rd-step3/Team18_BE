package com.kakaotech.team18.backend_server.domain.user.service;

import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.clubReview.repository.ClubReviewRepository;
import com.kakaotech.team18.backend_server.domain.comment.repository.CommentRepository;
import com.kakaotech.team18.backend_server.domain.user.dto.MyProfileResponseDto;
import com.kakaotech.team18.backend_server.domain.user.dto.UserActivityDto;
import com.kakaotech.team18.backend_server.domain.user.dto.UserActivityDto.UserApplicationDto;
import com.kakaotech.team18.backend_server.domain.user.dto.UserActivityDto.UserClubReviewDto;
import com.kakaotech.team18.backend_server.domain.user.dto.UserActivityDto.UserCommentDto;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UserNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClubReviewRepository clubReviewRepository;
    private final CommentRepository commentRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    public MyProfileResponseDto getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return MyProfileResponseDto.from(user);
    }

    @Override
    public UserActivityDto getMyActivities(Long userId) {
        List<UserClubReviewDto> reviews = clubReviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(review -> new UserClubReviewDto(
                        review.getId(),
                        review.getClub().getId(),
                        review.getClub().getName(),
                        review.getContent(),
                        review.getCreatedAt()))
                .toList();

        List<UserCommentDto> comments = commentRepository.findAllByUserId(userId)
                .stream()
                .map(comment -> new UserCommentDto(
                        comment.getId(),
                        comment.getApplication().getId(),
                        comment.getApplication().getClubApplyForm().getClub().getId(),
                        comment.getApplication().getClubApplyForm().getClub().getName(),
                        comment.getContent(),
                        comment.getRating(),
                        comment.getCreatedAt()))
                .toList();

        List<UserApplicationDto> applications = applicationRepository.findAllByUserId(userId)
                .stream()
                .map(app -> new UserApplicationDto(
                        app.getId(),
                        app.getClubApplyForm().getClub().getId(),
                        app.getClubApplyForm().getClub().getName(),
                        app.getStatus().name(),
                        app.getCreatedAt()))
                .toList();

        return new UserActivityDto(reviews, comments, applications);
    }
}
