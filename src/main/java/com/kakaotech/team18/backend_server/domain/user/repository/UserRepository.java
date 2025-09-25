package com.kakaotech.team18.backend_server.domain.user.repository;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 카카오 ID를 기준으로 사용자를 조회합니다.
     * 카카오 ID는 unique 제약조건이 걸려있으므로, 결과는 최대 1개 또는 0개입니다.
     *
     * @param kakaoId 카카오에서 발급한 사용자의 고유 ID
     * @return Optional<User> 사용자 정보 또는 빈 Optional
     */
    Optional<User> findByKakaoId(Long kakaoId);
    Optional<User> findByStudentId(String s);
}
