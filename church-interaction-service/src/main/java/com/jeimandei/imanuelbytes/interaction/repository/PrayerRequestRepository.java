package com.jeimandei.imanuelbytes.interaction.repository;

import com.jeimandei.imanuelbytes.interaction.entity.PrayerRequest;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrayerRequestRepository extends JpaRepository<PrayerRequest, Long> {

    Page<PrayerRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    Page<PrayerRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(RequestStatus status);
}
