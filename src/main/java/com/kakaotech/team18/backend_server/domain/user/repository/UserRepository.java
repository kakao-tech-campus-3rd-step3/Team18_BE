package com.kakaotech.team18.backend_server.domain.user.repository;

import com.kakaotech.team18.backend_server.domain.user.entity.Faculty;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String s);

    /**
     * gender가 아직 없는 User에 한해 원자적으로 채워 넣습니다.
     * <p>
     * WHERE 절의 {@code gender IS NULL}이 DB 레벨에서 조건을 재확인하므로, 같은 학번으로 두 요청이 동시에
     * 들어와도 먼저 커밋되는 한 요청만 값을 채우고 나머지는 0건 갱신으로 끝납니다. 애플리케이션 레벨에서 읽은
     * User 엔티티의 gender 값만으로 판단하면 이 경합을 막을 수 없습니다.
     *
     * @return 실제로 갱신된 행 수 (0 또는 1)
     */
    @Modifying
    @Query("UPDATE User u SET u.gender = :gender WHERE u.id = :id AND u.gender IS NULL")
    int updateGenderIfAbsent(@Param("id") Long id, @Param("gender") Gender gender);

    /**
     * faculty가 아직 없는 User에 한해 원자적으로 채워 넣습니다.
     * <p>
     * {@link #updateGenderIfAbsent}와 동일하게, WHERE 절의 {@code faculty IS NULL}이 DB 레벨에서 조건을
     * 재확인하므로 같은 학번 동시 제출 시에도 먼저 커밋되는 한 요청만 값을 채웁니다.
     *
     * @return 실제로 갱신된 행 수 (0 또는 1)
     */
    @Modifying
    @Query("UPDATE User u SET u.faculty = :faculty WHERE u.id = :id AND u.faculty IS NULL")
    int updateFacultyIfAbsent(@Param("id") Long id, @Param("faculty") Faculty faculty);
}
