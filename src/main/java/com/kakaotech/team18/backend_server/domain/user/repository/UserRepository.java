package com.kakaotech.team18.backend_server.domain.user.repository;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    /**
     * 학번을 기준으로 사용자를 조회합니다.
     * 학번은 unique 제약조건이 걸려있으므로, 결과는 최대 1개 또는 0개입니다.
     *
     * @param studentId 사용자의 학번
     * @return Optional<User> 사용자 정보 또는 빈 Optional
     */
    Optional<User> findByStudentId(String studentId);

    boolean existsByName(@NotBlank(message = "이름은 필수입니다.") String name);

    boolean existsByEmail(@NotBlank(message = "이메일은 필수입니다.") @Email(message = "유효한 이메일 형식이 아닙니다.") String email);

    boolean existsByPhoneNumber(@Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-xxxx-xxxx 형식이어야 합니다.") String s);
}
