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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

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
        EventDto created = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Event created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<EventDto>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", eventService.updateEvent(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @PutMapping("/{id}/featured")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<EventDto>> toggleFeatured(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.toggleFeatured(id)));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<EventDto>> publishEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Event published", eventService.publishEvent(id)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EventDto>> cancelEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Event cancelled", eventService.cancelEvent(id)));
    }
}
