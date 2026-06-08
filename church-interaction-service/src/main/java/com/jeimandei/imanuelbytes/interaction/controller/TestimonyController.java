package com.jeimandei.imanuelbytes.interaction.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.interaction.dto.SubmitTestimonyRequest;
import com.jeimandei.imanuelbytes.interaction.dto.TestimonyDto;
import com.jeimandei.imanuelbytes.interaction.service.TestimonyService;
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

@RestController
@RequestMapping("/api/testimonies")
public class TestimonyController {

    private static final Logger log = LoggerFactory.getLogger(TestimonyController.class);

    private final TestimonyService testimonyService;

    public TestimonyController(TestimonyService testimonyService) {
        this.testimonyService = testimonyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TestimonyDto>> submitTestimony(
            @Valid @RequestBody SubmitTestimonyRequest request) {
        log.debug("Submitting testimony from name='{}'", request.getName());
        TestimonyDto created = testimonyService.submitTestimony(request);
        log.info("Testimony submitted successfully: id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Testimony submitted successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TestimonyDto>>> getApprovedTestimonies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TestimonyDto> result = testimonyService.getApprovedTestimonies(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<TestimonyDto>>> getAllTestimonies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TestimonyDto> result = testimonyService.getAllTestimonies(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TestimonyDto>> approveTestimony(@PathVariable Long id) {
        log.debug("Approving testimony id={}", id);
        TestimonyDto approved = testimonyService.approveTestimony(id);
        log.info("Testimony approved: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Testimony approved", approved));
    }
}
