package com.jeimandei.imanuelbytes.media.repository;

import com.jeimandei.imanuelbytes.media.entity.Sermon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SermonRepository extends JpaRepository<Sermon, Long> {

    Page<Sermon> findBySpeakerContainingIgnoreCase(String speaker, Pageable pageable);

    Page<Sermon> findBySeriesName(String seriesName, Pageable pageable);

    Page<Sermon> findAllByOrderBySermonDateDesc(Pageable pageable);

    @Query("SELECT s FROM Sermon s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.speaker) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.seriesName) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Sermon> searchSermons(@Param("q") String q, Pageable pageable);
}
