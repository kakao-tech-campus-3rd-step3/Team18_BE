package com.kakaotech.team18.backend_server.domain.files.repository;

import com.kakaotech.team18.backend_server.domain.files.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileDataRepository extends JpaRepository<File, Long> {

    Optional<File> findById(Long fieldId);

    List<File> findAllByNoticeId(Long noticeId);
}
