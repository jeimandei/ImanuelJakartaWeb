package com.jeimandei.imanuelbytes.interaction.repository;

import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.entity.VolunteerApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerApplicationRepository extends JpaRepository<VolunteerApplication, Long> {

    Page<VolunteerApplication> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    Page<VolunteerApplication> findByMinistryOrderByCreatedAtDesc(String ministry, Pageable pageable);

    Page<VolunteerApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
