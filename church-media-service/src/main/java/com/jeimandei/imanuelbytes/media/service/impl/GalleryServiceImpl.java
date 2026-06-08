package com.jeimandei.imanuelbytes.media.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.media.audit.AuditClientService;
import com.jeimandei.imanuelbytes.media.dto.CreateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.entity.GalleryItem;
import com.jeimandei.imanuelbytes.media.mapper.GalleryItemMapper;
import com.jeimandei.imanuelbytes.media.repository.GalleryItemRepository;
import com.jeimandei.imanuelbytes.media.service.GalleryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GalleryServiceImpl implements GalleryService {

    private static final Logger log = LoggerFactory.getLogger(GalleryServiceImpl.class);

    private final GalleryItemRepository galleryItemRepository;
    private final GalleryItemMapper galleryItemMapper;
    private final AuditClientService auditClient;

    public GalleryServiceImpl(GalleryItemRepository galleryItemRepository,
                              GalleryItemMapper galleryItemMapper,
                              AuditClientService auditClient) {
        this.galleryItemRepository = galleryItemRepository;
        this.galleryItemMapper = galleryItemMapper;
        this.auditClient = auditClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GalleryItemDto> getAllGalleryItems(Pageable pageable) {
        return galleryItemRepository.findByActiveTrueOrderByAlbumNameAscDisplayOrderAsc(pageable)
                .map(galleryItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GalleryItemDto> getGalleryByAlbum(String albumName, Pageable pageable) {
        return galleryItemRepository.findByAlbumNameAndActiveTrueOrderByDisplayOrderAsc(albumName, pageable)
                .map(galleryItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAlbumNames() {
        return galleryItemRepository.findDistinctAlbumNames();
    }

    @Override
    public GalleryItemDto createGalleryItem(CreateGalleryItemRequest request) {
        log.debug("Creating gallery item for album='{}'", request.getAlbumName());
        GalleryItem item = galleryItemMapper.toEntity(request);
        GalleryItem saved = galleryItemRepository.save(item);
        log.info("Gallery item created successfully: id={}, album='{}'", saved.getId(), saved.getAlbumName());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_GALLERY_ITEM", "GalleryItem",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for CREATE_GALLERY_ITEM {}: {}", saved.getId(), e.getMessage());
        }
        return galleryItemMapper.toDto(saved);
    }

    @Override
    public GalleryItemDto updateGalleryItem(Long id, UpdateGalleryItemRequest request) {
        log.debug("Updating gallery item id={}", id);
        GalleryItem item = galleryItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Gallery item not found for update: id={}", id);
                    return new ResourceNotFoundException("GalleryItem", id);
                });
        galleryItemMapper.updateEntity(item, request);
        GalleryItem saved = galleryItemRepository.save(item);
        log.info("Gallery item updated successfully: id={}", saved.getId());
        return galleryItemMapper.toDto(saved);
    }

    @Override
    public void deleteGalleryItem(Long id) {
        log.debug("Deleting gallery item id={}", id);
        GalleryItem item = galleryItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Gallery item not found for deletion: id={}", id);
                    return new ResourceNotFoundException("GalleryItem", id);
                });
        galleryItemRepository.deleteById(id);
        log.info("Gallery item deleted successfully: id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "DELETE_GALLERY_ITEM", "GalleryItem",
                    String.valueOf(id), item.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for DELETE_GALLERY_ITEM {}: {}", id, e.getMessage());
        }
    }

    @Override
    public GalleryItemDto toggleActive(Long id) {
        log.debug("Toggling active state for gallery item id={}", id);
        GalleryItem item = galleryItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Gallery item not found for toggle: id={}", id);
                    return new ResourceNotFoundException("GalleryItem", id);
                });
        item.setActive(!item.isActive());
        GalleryItem saved = galleryItemRepository.save(item);
        log.info("Gallery item visibility toggled: id={}, active={}", saved.getId(), saved.isActive());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "TOGGLE_GALLERY_ITEM", "GalleryItem",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for TOGGLE_GALLERY_ITEM {}: {}", id, e.getMessage());
        }
        return galleryItemMapper.toDto(saved);
    }

    private String getCurrentActor() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String getCurrentActorRole() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("UNKNOWN");
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }
}
