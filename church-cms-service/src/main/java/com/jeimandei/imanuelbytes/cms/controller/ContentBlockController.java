package com.jeimandei.imanuelbytes.cms.controller;

import com.jeimandei.imanuelbytes.cms.dto.CmsContentBlockDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateContentBlockRequest;
import com.jeimandei.imanuelbytes.cms.service.ContentBlockService;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cms/blocks")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class ContentBlockController {

    private static final Logger log = LoggerFactory.getLogger(ContentBlockController.class);

    private final ContentBlockService contentBlockService;

    public ContentBlockController(ContentBlockService contentBlockService) {
        this.contentBlockService = contentBlockService;
    }

    @GetMapping("/page/{pageId}")
    public ResponseEntity<ApiResponse<List<CmsContentBlockDto>>> getBlocksByPage(@PathVariable Long pageId) {
        return ResponseEntity.ok(ApiResponse.success(contentBlockService.getBlocksByPage(pageId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CmsContentBlockDto>> createBlock(@Valid @RequestBody CreateContentBlockRequest request) {
        log.debug("Creating content block for pageId={}, blockType='{}'", request.getPageId(), request.getBlockType());
        CmsContentBlockDto created = contentBlockService.createBlock(request);
        log.info("Created content block id={} for pageId={}", created.getId(), created.getPageId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Block created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CmsContentBlockDto>> updateBlock(
            @PathVariable Long id, @Valid @RequestBody CreateContentBlockRequest request) {
        log.debug("Updating content block id={}", id);
        CmsContentBlockDto updated = contentBlockService.updateBlock(id, request);
        log.info("Updated content block id={}", updated.getId());
        return ResponseEntity.ok(ApiResponse.success("Block updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(@PathVariable Long id) {
        log.debug("Deleting content block id={}", id);
        contentBlockService.deleteBlock(id);
        log.info("Deleted content block id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Block deleted", null));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<CmsContentBlockDto>> toggleActive(@PathVariable Long id) {
        log.debug("Toggling active state of content block id={}", id);
        CmsContentBlockDto toggled = contentBlockService.toggleActive(id);
        log.info("Toggled content block id={} active={}", toggled.getId(), toggled.isActive());
        return ResponseEntity.ok(ApiResponse.success(toggled));
    }
}
