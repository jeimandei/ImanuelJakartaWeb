package com.jeimandei.imanuelbytes.interaction.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.interaction.dto.CreatePrayerRequestRequest;
import com.jeimandei.imanuelbytes.interaction.dto.PrayerRequestDto;
import com.jeimandei.imanuelbytes.interaction.dto.UpdatePrayerRequestStatusRequest;
import com.jeimandei.imanuelbytes.interaction.service.PrayerRequestService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/prayer-requests")
public class PrayerRequestController {

    private static final Logger log = LoggerFactory.getLogger(PrayerRequestController.class);

    private final PrayerRequestService prayerRequestService;

    public PrayerRequestController(PrayerRequestService prayerRequestService) {
        this.prayerRequestService = prayerRequestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PrayerRequestDto>> submitPrayerRequest(
            @Valid @RequestBody CreatePrayerRequestRequest request) {
        log.debug("Submitting prayer request from name='{}'", request.getName());
        PrayerRequestDto created = prayerRequestService.submitPrayerRequest(request);
        log.info("Prayer request submitted successfully: id={}, confidential={}", created.getId(), created.isConfidential());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Prayer request submitted successfully", created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<PageResponse<PrayerRequestDto>>> getAllPrayerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Listing prayer requests: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PrayerRequestDto> result = prayerRequestService.getAllPrayerRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<PrayerRequestDto>> getPrayerRequestById(@PathVariable Long id) {
        log.debug("Fetching prayer request by id={}", id);
        return ResponseEntity.ok(ApiResponse.success(prayerRequestService.getPrayerRequestById(id)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<PrayerRequestDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrayerRequestStatusRequest request) {
        log.debug("Updating prayer request status: id={}, status={}", id, request.getStatus());
        PrayerRequestDto updated = prayerRequestService.updateStatus(id, request);
        log.info("Prayer request status updated: id={}, status={}", id, updated.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Prayer request status updated", updated));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(prayerRequestService.getStats()));
    }
}
