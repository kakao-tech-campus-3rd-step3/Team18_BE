package com.kakaotech.team18.backend_server.domain.club.repository;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import java.util.List;
import java.util.Optional;

import com.kakaotech.team18.backend_server.domain.club.repository.dto.ClubSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findById(long id);

    Optional<Club> findByName(String name);

    List<Club> findByCategory(Category category);

    @Query("""
    select c.id as id,
           c.name as name,
           c.shortIntroduction as shortIntroduction,
           c.category as category,
           c.recruitStart as recruitStart,
           c.recruitEnd as recruitEnd
    from Club c
    """)
    List<ClubSummary> findAllSummaries();

    @Query(value = """ 
    select c.id as id,
           c.name as name,
           c.category as category, 
           c.short_introduction as shortIntroduction, 
           c.recruit_start as recruitStart, 
           c.recruit_end as recruitEnd
    from club c
    where c.category = :name
    """, nativeQuery = true)
    Optional<ClubSummary> findClubSummaryByName(@Param("name") String name);

    @Query(value = """ 
    select c.id as id,
           c.name as name,
           c.category as category, 
           c.short_introduction as shortIntroduction, 
           c.recruit_start as recruitStart, 
           c.recruit_end as recruitEnd
    from club c
    where c.category = :category
    """, nativeQuery = true)
    List<ClubSummary> findSummariesByCategory(@Param("category") Category category);


    @Query(value = """
    select c.id as id,
           c.club_name as name,
           c.category as category,
           c.short_introduction as shortIntroduction,
           c.recruit_start as recruitStart,
           c.recruit_end as recruitEnd
    from club c
    where c.club_name like concat('%', :name, '%')
    """, nativeQuery = true)
    List<ClubSummary> findSummariesByNameContaining(@Param("name") String name);

