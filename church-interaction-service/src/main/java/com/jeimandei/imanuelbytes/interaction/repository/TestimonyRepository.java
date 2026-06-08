package com.jeimandei.imanuelbytes.interaction.repository;

import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.entity.TestimonySubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestimonyRepository extends JpaRepository<TestimonySubmission, Long> {

    Page<TestimonySubmission> findByApprovedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<TestimonySubmission> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    Page<TestimonySubmission> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
