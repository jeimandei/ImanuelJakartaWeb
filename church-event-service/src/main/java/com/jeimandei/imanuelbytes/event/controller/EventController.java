package com.jeimandei.imanuelbytes.event.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.event.dto.CreateEventRequest;
import com.jeimandei.imanuelbytes.event.dto.EventDto;
import com.jeimandei.imanuelbytes.event.dto.FeaturedEventDto;
import com.jeimandei.imanuelbytes.event.dto.UpdateEventRequest;
import com.jeimandei.imanuelbytes.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getPublishedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventStart").ascending());
        Page<EventDto> result = eventService.getPublishedEvents(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<EventDto> result = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventStart").ascending());
        Page<EventDto> result = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/past")
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getPastEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventStart").descending());
        Page<EventDto> result = eventService.getPastEvents(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<FeaturedEventDto>>> getFeaturedEvents() {
        List<FeaturedEventDto> result = eventService.getFeaturedEvents();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDto>> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEventById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<EventDto>> getEventBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEventBySlug(slug)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<EventDto>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        log.debug("Creating event with title='{}'", request.getTitle());
        EventDto created = eventService.createEvent(request);
        log.info("Created event id={}, slug='{}'", created.getId(), created.getSlug());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Event created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<EventDto>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {
        log.debug("Updating event id={}", id);
        EventDto updated = eventService.updateEvent(id, request);
        log.info("Updated event id={}", updated.getId());
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        log.debug("Deleting event id={}", id);
        eventService.deleteEvent(id);
        log.info("Deleted event id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @PutMapping("/{id}/featured")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<EventDto>> toggleFeatured(@PathVariable Long id) {
        log.debug("Toggling featured state for event id={}", id);
        EventDto toggled = eventService.toggleFeatured(id);
        log.info("Event id={} featured set to {}", toggled.getId(), toggled.isFeatured());
        return ResponseEntity.ok(ApiResponse.success(toggled));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<EventDto>> publishEvent(@PathVariable Long id) {
        log.debug("Publishing event id={}", id);
        EventDto published = eventService.publishEvent(id);
        log.info("Published event id={}, slug='{}'", published.getId(), published.getSlug());
        return ResponseEntity.ok(ApiResponse.success("Event published", published));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EventDto>> cancelEvent(@PathVariable Long id) {
        log.debug("Cancelling event id={}", id);
        EventDto cancelled = eventService.cancelEvent(id);
        log.info("Cancelled event id={}", cancelled.getId());
        return ResponseEntity.ok(ApiResponse.success("Event cancelled", cancelled));
    }
}
