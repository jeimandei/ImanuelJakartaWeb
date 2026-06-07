package com.jeimandei.imanuelbytes.cms.controller;

import com.jeimandei.imanuelbytes.cms.dto.AnnouncementDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateAnnouncementRequest;
import com.jeimandei.imanuelbytes.cms.service.AnnouncementService;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementController.class);

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AnnouncementDto>>> getActiveAnnouncements() {
        log.debug("Fetching active announcements");
        List<AnnouncementDto> active = announcementService.getActiveAnnouncements();
        log.info("Retrieved {} active announcements", active.size());
        return ResponseEntity.ok(ApiResponse.success(active));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<PageResponse<AnnouncementDto>>> getAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(announcementService.getAllAnnouncements(
                        PageRequest.of(page, size, Sort.by("priority").descending())))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> getAnnouncementById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(announcementService.getAnnouncementById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request) {
        log.debug("Creating announcement with title='{}'", request.getTitle());
        AnnouncementDto created = announcementService.createAnnouncement(request);
        log.info("Created announcement id={}, title='{}'", created.getId(), created.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> updateAnnouncement(
            @PathVariable Long id, @Valid @RequestBody CreateAnnouncementRequest request) {
        log.debug("Updating announcement id={}", id);
        AnnouncementDto updated = announcementService.updateAnnouncement(id, request);
        log.info("Updated announcement id={}", updated.getId());
        return ResponseEntity.ok(ApiResponse.success("Announcement updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        log.debug("Deleting announcement id={}", id);
        announcementService.deleteAnnouncement(id);
        log.info("Deleted announcement id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Announcement deleted", null));
    }
}
