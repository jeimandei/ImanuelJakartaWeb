package com.jeimandei.imanuelbytes.event.service;

import com.jeimandei.imanuelbytes.event.dto.CreateEventRequest;
import com.jeimandei.imanuelbytes.event.dto.EventDto;
import com.jeimandei.imanuelbytes.event.dto.FeaturedEventDto;
import com.jeimandei.imanuelbytes.event.dto.UpdateEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Business-logic contract for church event management.
 */
public interface EventService {

    /**
     * Returns all events regardless of status (admin view).
     *
     * @param pageable pagination and sorting parameters
     * @return page of all events
     */
    Page<EventDto> getAllEvents(Pageable pageable);

    /**
     * Returns only PUBLISHED events (public view).
     *
     * @param pageable pagination and sorting parameters
     * @return page of published events
     */
    Page<EventDto> getPublishedEvents(Pageable pageable);

    /**
     * Returns published events whose start time is in the future.
     *
     * @param pageable pagination and sorting parameters
     * @return page of upcoming events
     */
    Page<EventDto> getUpcomingEvents(Pageable pageable);

    /**
     * Returns published events whose start time is in the past.
     *
     * @param pageable pagination and sorting parameters
     * @return page of past events
     */
    Page<EventDto> getPastEvents(Pageable pageable);

    /**
     * Returns all events that are featured, published, and upcoming.
     *
     * @return list of featured event projections
     */
    List<FeaturedEventDto> getFeaturedEvents();

    /**
     * Returns a single event by its surrogate key.
     *
     * @param id the event identifier
     * @return the event DTO
     * @throws com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException if not found
     */
    EventDto getEventById(Long id);

    /**
     * Returns a single event by its URL slug.
     *
     * @param slug the URL-friendly identifier
     * @return the event DTO
     * @throws com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException if not found
     */
    EventDto getEventBySlug(String slug);

    /**
     * Persists a new event.  If no slug is supplied in the request one is
     * auto-generated from the title.
     *
     * @param request the creation request
     * @return the saved event DTO
     */
    EventDto createEvent(CreateEventRequest request);

    /**
     * Applies partial updates to an existing event.
     *
     * @param id      the event to update
     * @param request fields to change (null values are ignored)
     * @return the updated event DTO
     */
    EventDto updateEvent(Long id, UpdateEventRequest request);

    /**
     * Soft-deletes an event by setting its status to CANCELLED.
     *
     * @param id the event to cancel
     */
    void deleteEvent(Long id);

    /**
     * Toggles the featured flag on an event.
     *
     * @param id the event to update
     * @return the updated event DTO
     */
    EventDto toggleFeatured(Long id);

    /**
     * Transitions an event to PUBLISHED status.
     *
     * @param id the event to publish
     * @return the updated event DTO
     */
    EventDto publishEvent(Long id);

    /**
     * Transitions an event to CANCELLED status.
     *
     * @param id the event to cancel
     * @return the updated event DTO
     */
    EventDto cancelEvent(Long id);
}
