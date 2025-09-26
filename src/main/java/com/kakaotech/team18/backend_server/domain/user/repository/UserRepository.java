package com.kakaotech.team18.backend_server.domain.user.repository;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
