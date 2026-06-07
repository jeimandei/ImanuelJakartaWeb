package com.jeimandei.imanuelbytes.media.service;

import com.jeimandei.imanuelbytes.media.dto.CreateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateGalleryItemRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GalleryService {

    Page<GalleryItemDto> getAllGalleryItems(Pageable pageable);

    Page<GalleryItemDto> getGalleryByAlbum(String albumName, Pageable pageable);

    List<String> getAlbumNames();

    GalleryItemDto createGalleryItem(CreateGalleryItemRequest request);

    GalleryItemDto updateGalleryItem(Long id, UpdateGalleryItemRequest request);

    void deleteGalleryItem(Long id);

    GalleryItemDto toggleActive(Long id);
}
