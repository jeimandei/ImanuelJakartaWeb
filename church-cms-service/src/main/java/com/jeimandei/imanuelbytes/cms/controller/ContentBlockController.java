package com.jeimandei.imanuelbytes.cms.controller;

import com.jeimandei.imanuelbytes.cms.dto.CmsContentBlockDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateContentBlockRequest;
import com.jeimandei.imanuelbytes.cms.service.ContentBlockService;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cms/blocks")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
public class ContentBlockController {

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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Block created", contentBlockService.createBlock(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CmsContentBlockDto>> updateBlock(
            @PathVariable Long id, @Valid @RequestBody CreateContentBlockRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Block updated", contentBlockService.updateBlock(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(@PathVariable Long id) {
        contentBlockService.deleteBlock(id);
        return ResponseEntity.ok(ApiResponse.success("Block deleted", null));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<CmsContentBlockDto>> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contentBlockService.toggleActive(id)));
    }
}
