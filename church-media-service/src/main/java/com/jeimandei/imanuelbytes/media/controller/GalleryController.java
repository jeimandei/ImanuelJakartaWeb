package com.jeimandei.imanuelbytes.media.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.media.dto.CreateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.service.GalleryService;
import jakarta.validation.Valid;
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
        GalleryItemDto created = galleryService.createGalleryItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gallery item created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GalleryItemDto>> updateGalleryItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGalleryItemRequest request) {
        GalleryItemDto updated = galleryService.updateGalleryItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Gallery item updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGalleryItem(@PathVariable Long id) {
        galleryService.deleteGalleryItem(id);
        return ResponseEntity.ok(ApiResponse.success("Gallery item deleted successfully"));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GalleryItemDto>> toggleActive(@PathVariable Long id) {
        GalleryItemDto toggled = galleryService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Gallery item visibility toggled", toggled));
    }
}
