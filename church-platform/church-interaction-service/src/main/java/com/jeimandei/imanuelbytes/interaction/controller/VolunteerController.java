package com.jeimandei.imanuelbytes.interaction.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.interaction.dto.CreateVolunteerApplicationRequest;
import com.jeimandei.imanuelbytes.interaction.dto.VolunteerApplicationDto;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.service.VolunteerService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VolunteerApplicationDto>> submitApplication(
            @Valid @RequestBody CreateVolunteerApplicationRequest request) {
        VolunteerApplicationDto created = volunteerService.submitApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Volunteer application submitted successfully", created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<VolunteerApplicationDto>>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<VolunteerApplicationDto> result = volunteerService.getAllApplications(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/ministry/{ministry}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<VolunteerApplicationDto>>> getByMinistry(
            @PathVariable String ministry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<VolunteerApplicationDto> result = volunteerService.getByMinistry(ministry, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VolunteerApplicationDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam RequestStatus status) {
        VolunteerApplicationDto updated = volunteerService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Application status updated", updated));
    }
}
