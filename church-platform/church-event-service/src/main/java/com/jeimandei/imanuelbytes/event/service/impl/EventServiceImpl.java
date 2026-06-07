package com.jeimandei.imanuelbytes.event.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.common.util.SlugUtils;
import com.jeimandei.imanuelbytes.event.dto.CreateEventRequest;
import com.jeimandei.imanuelbytes.event.dto.EventDto;
import com.jeimandei.imanuelbytes.event.dto.FeaturedEventDto;
import com.jeimandei.imanuelbytes.event.dto.UpdateEventRequest;
import com.jeimandei.imanuelbytes.event.entity.Event;
import com.jeimandei.imanuelbytes.event.entity.EventStatus;
import com.jeimandei.imanuelbytes.event.mapper.EventMapper;
import com.jeimandei.imanuelbytes.event.repository.EventRepository;
import com.jeimandei.imanuelbytes.event.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Default implementation of {@link EventService}.
 *
 * <p>All write operations are wrapped in transactions. Read-only operations use
 * {@code readOnly = true} to allow the JPA provider to optimise dirty-checking.</p>
 */
@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<EventDto> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(eventMapper::toDtoWithUpcoming);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDto> getPublishedEvents(Pageable pageable) {
        return eventRepository.findByStatus(EventStatus.PUBLISHED, pageable)
                .map(eventMapper::toDtoWithUpcoming);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDto> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable)
                .map(eventMapper::toDtoWithUpcoming);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDto> getPastEvents(Pageable pageable) {
        return eventRepository.findPastEvents(LocalDateTime.now(), pageable)
                .map(eventMapper::toDtoWithUpcoming);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeaturedEventDto> getFeaturedEvents() {
        return eventRepository.findFeaturedEvents(LocalDateTime.now())
                .stream()
                .map(eventMapper::toFeaturedDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getEventById(Long id) {
        Event event = findEventOrThrow(id);
        return eventMapper.toDtoWithUpcoming(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getEventBySlug(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "slug", slug));
        return eventMapper.toDtoWithUpcoming(event);
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    @Override
    public EventDto createEvent(CreateEventRequest request) {
        // Resolve slug: use supplied value or generate from title
        String slug = resolveSlug(request.getSlug(), request.getTitle());

        if (eventRepository.existsBySlug(slug)) {
            throw new ValidationException("slug", "An event with slug '" + slug + "' already exists", true);
        }

        Event event = eventMapper.toEntity(request);
        event.setSlug(slug);

        if (event.getStatus() == null) {
            event.setStatus(EventStatus.DRAFT);
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toDtoWithUpcoming(saved);
    }

    @Override
    public EventDto updateEvent(Long id, UpdateEventRequest request) {
        Event event = findEventOrThrow(id);

        // If slug is being changed, validate uniqueness
        if (request.getSlug() != null
                && !request.getSlug().equals(event.getSlug())
                && eventRepository.existsBySlug(request.getSlug())) {
            throw new ValidationException("slug", "An event with slug '" + request.getSlug() + "' already exists", true);
        }

        eventMapper.updateEntity(event, request);
        Event saved = eventRepository.save(event);
        return eventMapper.toDtoWithUpcoming(saved);
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = findEventOrThrow(id);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    @Override
    public EventDto toggleFeatured(Long id) {
        Event event = findEventOrThrow(id);
        event.setFeatured(!event.isFeatured());
        Event saved = eventRepository.save(event);
        return eventMapper.toDtoWithUpcoming(saved);
    }

    @Override
    public EventDto publishEvent(Long id) {
        Event event = findEventOrThrow(id);
        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);
        return eventMapper.toDtoWithUpcoming(saved);
    }

    @Override
    public EventDto cancelEvent(Long id) {
        Event event = findEventOrThrow(id);
        event.setStatus(EventStatus.CANCELLED);
        Event saved = eventRepository.save(event);
        return eventMapper.toDtoWithUpcoming(saved);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    /**
     * Returns {@code supplied} when non-blank, otherwise generates a slug from
     * {@code title}.
     */
    private String resolveSlug(String supplied, String title) {
        if (supplied != null && !supplied.isBlank()) {
            return SlugUtils.toSlug(supplied);
        }
        return SlugUtils.toSlug(title);
    }
}
