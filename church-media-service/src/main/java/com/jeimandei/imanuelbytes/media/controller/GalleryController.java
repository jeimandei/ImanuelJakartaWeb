package com.jeimandei.imanuelbytes.media.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.media.dto.CreateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.service.GalleryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private static final Logger log = LoggerFactory.getLogger(GalleryController.class);

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GalleryItemDto>>> getAllGalleryItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GalleryItemDto> resultPage = galleryService.getAllGalleryItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(resultPage)));
    }

    @GetMapping("/albums")
    public ResponseEntity<ApiResponse<List<String>>> getAlbumNames() {
        List<String> albums = galleryService.getAlbumNames();
        return ResponseEntity.ok(ApiResponse.success(albums));
    }

    @GetMapping("/album/{albumName}")
    public ResponseEntity<ApiResponse<PageResponse<GalleryItemDto>>> getGalleryByAlbum(
            @PathVariable String albumName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GalleryItemDto> resultPage = galleryService.getGalleryByAlbum(albumName, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(resultPage)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GalleryItemDto>> createGalleryItem(
            @Valid @RequestBody CreateGalleryItemRequest request) {
        log.debug("Creating gallery item for album='{}'", request.getAlbumName());
        GalleryItemDto created = galleryService.createGalleryItem(request);
        log.info("Gallery item created successfully: id={}, album='{}'", created.getId(), created.getAlbumName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gallery item created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GalleryItemDto>> updateGalleryItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGalleryItemRequest request) {
        log.debug("Updating gallery item id={}", id);
        GalleryItemDto updated = galleryService.updateGalleryItem(id, request);
        log.info("Gallery item updated successfully: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Gallery item updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGalleryItem(@PathVariable Long id) {
        log.debug("Deleting gallery item id={}", id);
        galleryService.deleteGalleryItem(id);
        log.info("Gallery item deleted successfully: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Gallery item deleted successfully"));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GalleryItemDto>> toggleActive(@PathVariable Long id) {
        log.debug("Toggling active state for gallery item id={}", id);
        GalleryItemDto toggled = galleryService.toggleActive(id);
        log.info("Gallery item visibility toggled: id={}, active={}", toggled.getId(), toggled.isActive());
        return ResponseEntity.ok(ApiResponse.success("Gallery item visibility toggled", toggled));
    }
}
