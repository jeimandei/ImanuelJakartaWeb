package com.jeimandei.imanuelbytes.event.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.common.util.SlugUtils;
import com.jeimandei.imanuelbytes.event.audit.AuditClientService;
import com.jeimandei.imanuelbytes.event.dto.CreateEventRequest;
import com.jeimandei.imanuelbytes.event.dto.EventDto;
import com.jeimandei.imanuelbytes.event.dto.FeaturedEventDto;
import com.jeimandei.imanuelbytes.event.dto.UpdateEventRequest;
import com.jeimandei.imanuelbytes.event.entity.Event;
import com.jeimandei.imanuelbytes.event.entity.EventStatus;
import com.jeimandei.imanuelbytes.event.mapper.EventMapper;
import com.jeimandei.imanuelbytes.event.repository.EventRepository;
import com.jeimandei.imanuelbytes.event.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final AuditClientService auditClient;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper,
                            AuditClientService auditClient) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.auditClient = auditClient;
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
        log.debug("Fetching event by slug='{}'", slug);
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.warn("Event not found for slug='{}'", slug);
                    return new ResourceNotFoundException("Event", "slug", slug);
                });
        return eventMapper.toDtoWithUpcoming(event);
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    @Override
    public EventDto createEvent(CreateEventRequest request) {
        // Resolve slug: use supplied value or generate from title
        String slug = resolveSlug(request.getSlug(), request.getTitle());
        log.debug("Creating event with title='{}', slug='{}'", request.getTitle(), slug);

        if (eventRepository.existsBySlug(slug)) {
            log.warn("Event slug conflict: slug='{}' already exists", slug);
            throw new ValidationException("slug", "An event with slug '" + slug + "' already exists", true);
        }

        Event event = eventMapper.toEntity(request);
        event.setSlug(slug);

        if (event.getStatus() == null) {
            event.setStatus(EventStatus.DRAFT);
        }

        Event saved = eventRepository.save(event);
        log.info("Created event id={}, slug='{}'", saved.getId(), saved.getSlug());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_EVENT", "Event",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for CREATE_EVENT {}: {}", saved.getId(), e.getMessage());
        }
        return eventMapper.toDtoWithUpcoming(saved);
    }

    @Override
    public EventDto updateEvent(Long id, UpdateEventRequest request) {
        log.debug("Updating event id={}", id);
        Event event = findEventOrThrow(id);

        // If slug is being changed, validate uniqueness
        if (request.getSlug() != null
                && !request.getSlug().equals(event.getSlug())
                && eventRepository.existsBySlug(request.getSlug())) {
            log.warn("Event slug conflict on update: slug='{}' already exists", request.getSlug());
            throw new ValidationException("slug", "An event with slug '" + request.getSlug() + "' already exists", true);
        }

        eventMapper.updateEntity(event, request);
        Event saved = eventRepository.save(event);
        log.info("Updated event id={}", saved.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_EVENT", "Event",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_EVENT {}: {}", id, e.getMessage());
        }
        return eventMapper.toDtoWithUpcoming(saved);
    }

    @Override
    public void deleteEvent(Long id) {
        log.debug("Deleting event id={}", id);
        Event event = findEventOrThrow(id);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        log.info("Deleted (cancelled) event id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "DELETE_EVENT", "Event",
                    String.valueOf(id), event.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for DELETE_EVENT {}: {}", id, e.getMessage());
        }
    }

    @Override
    public EventDto toggleFeatured(Long id) {
        log.debug("Toggling featured state for event id={}", id);
        Event event = findEventOrThrow(id);
        event.setFeatured(!event.isFeatured());
        Event saved = eventRepository.save(event);
        log.info("Event id={} featured set to {}", saved.getId(), saved.isFeatured());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "TOGGLE_FEATURED", "Event",
                    String.valueOf(saved.getId()), saved.getTitle() + " featured=" + saved.isFeatured());
        } catch (Exception e) {
            log.warn("Audit log failed for TOGGLE_FEATURED {}: {}", id, e.getMessage());
        }
        return eventMapper.toDtoWithUpcoming(saved);
    }

    @Override
    public EventDto publishEvent(Long id) {
        log.debug("Publishing event id={}", id);
        Event event = findEventOrThrow(id);
        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);
        log.info("Published event id={}, slug='{}'", saved.getId(), saved.getSlug());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "PUBLISH_EVENT", "Event",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for PUBLISH_EVENT {}: {}", id, e.getMessage());
        }
        return eventMapper.toDtoWithUpcoming(saved);
    }

    @Override
    public EventDto cancelEvent(Long id) {
        log.debug("Cancelling event id={}", id);
        Event event = findEventOrThrow(id);
        event.setStatus(EventStatus.CANCELLED);
        Event saved = eventRepository.save(event);
        log.info("Cancelled event id={}", saved.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CANCEL_EVENT", "Event",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for CANCEL_EVENT {}: {}", id, e.getMessage());
        }
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

    private String getCurrentActor() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String getCurrentActorRole() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("UNKNOWN");
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }
}
