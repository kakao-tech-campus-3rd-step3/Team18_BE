package com.kakaotech.team18.backend_server.domain.clubMember.repository;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMemberProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubMemberProfileRepository extends JpaRepository<ClubMemberProfile, Long> {

    @Query("SELECT p FROM ClubMemberProfile p " +
           "JOIN p.clubMember cm " +
           "WHERE cm.club.id = :clubId")
    List<ClubMemberProfile> findAllByClubId(@Param("clubId") Long clubId);

    @Query("SELECT p FROM ClubMemberProfile p " +
           "JOIN p.clubMember cm " +
           "WHERE p.id = :profileId AND cm.club.id = :clubId")
    Optional<ClubMemberProfile> findByIdAndClubId(@Param("profileId") Long profileId, @Param("clubId") Long clubId);
    
    boolean existsByClubMember_Club_IdAndStudentId(Long clubId, String studentId);
}
