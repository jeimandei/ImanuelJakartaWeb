package com.jeimandei.imanuelbytes.event.repository;

import com.jeimandei.imanuelbytes.event.entity.Event;
import com.jeimandei.imanuelbytes.event.entity.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Event} entities.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Finds an event by its unique URL slug.
     *
     * @param slug the URL-friendly identifier
     * @return the matching event wrapped in an Optional
     */
    Optional<Event> findBySlug(String slug);

    /**
     * Returns a page of events with the given status.
     *
     * @param status   the lifecycle state to filter by
     * @param pageable pagination and sorting parameters
     * @return page of matching events
     */
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    /**
     * Returns all published events whose start time is in the future,
     * ordered by start time ascending (soonest first).
     *
     * @param now      the current instant used as the lower bound
     * @param pageable pagination parameters
     * @return page of upcoming published events
     */
    @Query("SELECT e FROM Event e WHERE e.eventStart >= :now AND e.status = 'PUBLISHED' ORDER BY e.eventStart ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Returns all featured, published, upcoming events ordered by start time.
     *
     * @param now the current instant used as the lower bound
     * @return list of featured events
     */
    @Query("SELECT e FROM Event e WHERE e.featured = true AND e.status = 'PUBLISHED' AND e.eventStart >= :now ORDER BY e.eventStart ASC")
    List<Event> findFeaturedEvents(@Param("now") LocalDateTime now);

    /**
     * Returns published events whose start time is strictly before {@code now},
     * representing events that have already occurred.
     *
     * @param now      the current instant used as the upper bound
     * @param pageable pagination parameters
     * @return page of past published events
     */
    @Query("SELECT e FROM Event e WHERE e.eventStart < :now AND e.status = 'PUBLISHED' ORDER BY e.eventStart DESC")
    Page<Event> findPastEvents(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Checks whether an event with the given slug already exists.
     *
     * @param slug the slug to test
     * @return {@code true} if at least one event uses this slug
     */
    boolean existsBySlug(String slug);
}
