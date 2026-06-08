package com.jeimandei.imanuelbytes.cms.controller;

import com.jeimandei.imanuelbytes.cms.dto.CmsPageDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateCmsPageRequest;
import com.jeimandei.imanuelbytes.cms.dto.UpdateCmsPageRequest;
import com.jeimandei.imanuelbytes.cms.service.CmsPageService;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cms/pages")
public class CmsPageController {

    private static final Logger log = LoggerFactory.getLogger(CmsPageController.class);

    private final CmsPageService cmsPageService;

    public CmsPageController(CmsPageService cmsPageService) {
        this.cmsPageService = cmsPageService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<PageResponse<CmsPageDto>>> getAllPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(cmsPageService.searchPages(q, pageable))));
        }
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(cmsPageService.getAllPages(pageable))));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<PageResponse<CmsPageDto>>> getPublishedPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(cmsPageService.getPublishedPages(pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CmsPageDto>> getPageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(cmsPageService.getPageById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CmsPageDto>> getPageBySlug(@PathVariable String slug) {
        log.debug("Fetching CMS page by slug='{}'", slug);
        CmsPageDto page = cmsPageService.getPageBySlug(slug);
        log.info("Retrieved CMS page id={} for slug='{}'", page.getId(), slug);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/slug/{slug}/published")
    public ResponseEntity<ApiResponse<CmsPageDto>> getPublishedPageBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(cmsPageService.getPublishedPageBySlug(slug)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<CmsPageDto>> createPage(
            @Valid @RequestBody CreateCmsPageRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        log.debug("Creating CMS page with slug='{}', author='{}'", request.getSlug(), username);
        CmsPageDto created = cmsPageService.createPage(request, username);
        log.info("Created CMS page id={}, slug='{}'", created.getId(), created.getSlug());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Page created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<CmsPageDto>> updatePage(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCmsPageRequest request) {
        log.debug("Updating CMS page id={}", id);
        CmsPageDto updated = cmsPageService.updatePage(id, request);
        log.info("Updated CMS page id={}", updated.getId());
        return ResponseEntity.ok(ApiResponse.success("Page updated", updated));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<CmsPageDto>> publishPage(@PathVariable Long id) {
        log.debug("Publishing CMS page id={}", id);
        CmsPageDto published = cmsPageService.publishPage(id);
        log.info("Published CMS page id={}, slug='{}'", published.getId(), published.getSlug());
        return ResponseEntity.ok(ApiResponse.success("Page published", published));
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<CmsPageDto>> unpublishPage(@PathVariable Long id) {
        log.debug("Unpublishing CMS page id={}", id);
        CmsPageDto unpublished = cmsPageService.unpublishPage(id);
        log.info("Unpublished CMS page id={}", unpublished.getId());
        return ResponseEntity.ok(ApiResponse.success("Page unpublished", unpublished));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePage(@PathVariable Long id) {
        log.debug("Deleting CMS page id={}", id);
        cmsPageService.deletePage(id);
        log.info("Deleted CMS page id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Page deleted", null));
    }
}
