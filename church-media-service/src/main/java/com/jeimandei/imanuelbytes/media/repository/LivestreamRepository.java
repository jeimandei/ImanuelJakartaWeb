package com.jeimandei.imanuelbytes.media.repository;

import com.jeimandei.imanuelbytes.media.entity.Livestream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivestreamRepository extends JpaRepository<Livestream, Long> {

    Optional<Livestream> findFirstByActiveTrueOrderByScheduledStartDesc();

    List<Livestream> findByActiveTrueOrderByScheduledStartDesc();

    Page<Livestream> findAllByOrderByScheduledStartDesc(Pageable pageable);
}
