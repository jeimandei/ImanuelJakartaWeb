package com.jeimandei.imanuelbytes.cms.controller;

import com.jeimandei.imanuelbytes.cms.dto.CreateServiceTimeRequest;
import com.jeimandei.imanuelbytes.cms.dto.ServiceTimeDto;
import com.jeimandei.imanuelbytes.cms.service.ServiceTimeService;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/service-times")
public class ServiceTimeController {

    private static final Logger log = LoggerFactory.getLogger(ServiceTimeController.class);

    private final ServiceTimeService serviceTimeService;

    public ServiceTimeController(ServiceTimeService serviceTimeService) {
        this.serviceTimeService = serviceTimeService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ServiceTimeDto>>> getActiveServiceTimes() {
        return ResponseEntity.ok(ApiResponse.success(serviceTimeService.getActiveServiceTimes()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<List<ServiceTimeDto>>> getAllServiceTimes() {
        return ResponseEntity.ok(ApiResponse.success(serviceTimeService.getAllServiceTimes()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<ServiceTimeDto>> getServiceTimeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(serviceTimeService.getServiceTimeById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<ServiceTimeDto>> createServiceTime(
            @Valid @RequestBody CreateServiceTimeRequest request) {
        log.debug("Creating service time '{}'", request.getName());
        ServiceTimeDto created = serviceTimeService.createServiceTime(request);
        log.info("Created service time id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service time created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<ServiceTimeDto>> updateServiceTime(
            @PathVariable Long id, @Valid @RequestBody CreateServiceTimeRequest request) {
        log.debug("Updating service time id={}", id);
        ServiceTimeDto updated = serviceTimeService.updateServiceTime(id, request);
        log.info("Updated service time id={}", updated.getId());
        return ResponseEntity.ok(ApiResponse.success("Service time updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteServiceTime(@PathVariable Long id) {
        log.debug("Deleting service time id={}", id);
        serviceTimeService.deleteServiceTime(id);
        log.info("Deleted service time id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Service time deleted", null));
    }
}
