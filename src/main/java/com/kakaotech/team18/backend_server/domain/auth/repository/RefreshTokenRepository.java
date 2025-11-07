package com.kakaotech.team18.backend_server.domain.auth.repository;

import com.kakaotech.team18.backend_server.domain.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
}
