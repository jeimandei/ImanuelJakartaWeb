package com.jeimandei.imanuelbytes.cms.repository;

import com.jeimandei.imanuelbytes.cms.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("SELECT a FROM Announcement a WHERE a.active = true " +
           "AND a.startDate <= :today AND a.endDate >= :today " +
           "ORDER BY a.priority DESC")
    List<Announcement> findActiveAnnouncements(@org.springframework.data.repository.query.Param("today") LocalDate today);

    Page<Announcement> findByActiveOrderByPriorityDesc(boolean active, Pageable pageable);
}
