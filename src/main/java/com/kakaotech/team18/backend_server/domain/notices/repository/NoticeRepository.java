package com.kakaotech.team18.backend_server.domain.notices.repository;

import com.kakaotech.team18.backend_server.domain.notices.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
           select n
           from Notice n
           where n.isAlive = true
           order by n.id desc
           """)
    Page<Notice> findAlive(Pageable pageable);

    @Query("""
           select n
           from Notice n
           where n.id = :noticeId and n.isAlive = true
           """)
    Optional<Notice> findAliveById(Long noticeId);
}
