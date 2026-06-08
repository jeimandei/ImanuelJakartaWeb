package com.jeimandei.imanuelbytes.media.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.media.dto.CreateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.dto.LivestreamDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.service.LivestreamService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/livestreams")
public class LivestreamController {

    private static final Logger log = LoggerFactory.getLogger(LivestreamController.class);

    private final LivestreamService livestreamService;

    public LivestreamController(LivestreamService livestreamService) {
        this.livestreamService = livestreamService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<LivestreamDto>> getActiveLivestream() {
        log.debug("Fetching active livestream");
        Optional<LivestreamDto> active = livestreamService.getActiveLivestream();
        if (active.isEmpty()) {
            log.debug("No active livestream found");
        }
        return ResponseEntity.ok(ApiResponse.success(active.orElse(null)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<LivestreamDto>>> getAllLivestreams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Listing all livestreams: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<LivestreamDto> resultPage = livestreamService.getAllLivestreams(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(resultPage)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LivestreamDto>> getLivestreamById(@PathVariable Long id) {
        log.debug("Fetching livestream by id={}", id);
        LivestreamDto livestream = livestreamService.getLivestreamById(id);
        return ResponseEntity.ok(ApiResponse.success(livestream));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LivestreamDto>> createLivestream(
            @Valid @RequestBody CreateLivestreamRequest request) {
        log.debug("Creating livestream with title='{}'", request.getTitle());
        LivestreamDto created = livestreamService.createLivestream(request);
        log.info("Livestream created successfully: id={}, title='{}'", created.getId(), created.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Livestream created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LivestreamDto>> updateLivestream(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLivestreamRequest request) {
        log.debug("Updating livestream id={}", id);
        LivestreamDto updated = livestreamService.updateLivestream(id, request);
        log.info("Livestream updated successfully: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Livestream updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLivestream(@PathVariable Long id) {
        log.debug("Deleting livestream id={}", id);
        livestreamService.deleteLivestream(id);
        log.info("Livestream deleted successfully: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Livestream deleted successfully"));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LivestreamDto>> activateLivestream(@PathVariable Long id) {
        log.debug("Activating livestream id={}", id);
        LivestreamDto activated = livestreamService.activateLivestream(id);
        log.info("Livestream activated: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Livestream activated", activated));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LivestreamDto>> deactivateLivestream(@PathVariable Long id) {
        log.debug("Deactivating livestream id={}", id);
        LivestreamDto deactivated = livestreamService.deactivateLivestream(id);
        log.info("Livestream deactivated: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Livestream deactivated", deactivated));
    }
}
