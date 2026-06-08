package com.jeimandei.imanuelbytes.media.service;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.media.dto.CreateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.media.entity.GalleryItem;
import com.jeimandei.imanuelbytes.media.mapper.GalleryItemMapper;
import com.jeimandei.imanuelbytes.media.repository.GalleryItemRepository;
import com.jeimandei.imanuelbytes.media.service.impl.GalleryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GalleryServiceImplTest {

    @Mock
    private GalleryItemRepository galleryItemRepository;

    @Mock
    private GalleryItemMapper galleryItemMapper;

    @InjectMocks
    private GalleryServiceImpl galleryService;

    // -------------------------------------------------------------------------
    // createGalleryItem tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("createGalleryItem")
    class CreateGalleryItem {

        @Test
        @DisplayName("maps request to entity, saves it, and returns DTO")
        void createGalleryItem_success_returnsDto() {
            // Arrange
            CreateGalleryItemRequest request = buildCreateRequest("Baptism 2024", true);

            GalleryItem entity = new GalleryItem();
            entity.setTitle(request.getTitle());
            entity.setImageUrl(request.getImageUrl());
            entity.setAlbumName(request.getAlbumName());

            GalleryItem saved = new GalleryItem();
            saved.setId(1L);
            saved.setTitle(request.getTitle());
            saved.setImageUrl(request.getImageUrl());
            saved.setAlbumName(request.getAlbumName());
            saved.setActive(true);

            GalleryItemDto expectedDto = buildGalleryItemDto(1L, "Baptism 2024", true);

            when(galleryItemMapper.toEntity(request)).thenReturn(entity);
            when(galleryItemRepository.save(entity)).thenReturn(saved);
            when(galleryItemMapper.toDto(saved)).thenReturn(expectedDto);

            // Act
            GalleryItemDto result = galleryService.createGalleryItem(request);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Baptism 2024", result.getAlbumName());
            assertTrue(result.isActive());
            verify(galleryItemRepository).save(entity);
        }

        @Test
        @DisplayName("creates gallery item with active=false when request specifies inactive")
        void createGalleryItem_inactiveItem_savedCorrectly() {
            // Arrange
            CreateGalleryItemRequest request = buildCreateRequest("Draft Album", false);

            GalleryItem entity = new GalleryItem();
            entity.setActive(false);

            GalleryItem saved = new GalleryItem();
            saved.setId(2L);
            saved.setActive(false);

            GalleryItemDto expectedDto = buildGalleryItemDto(2L, "Draft Album", false);

            when(galleryItemMapper.toEntity(request)).thenReturn(entity);
            when(galleryItemRepository.save(entity)).thenReturn(saved);
            when(galleryItemMapper.toDto(saved)).thenReturn(expectedDto);

            // Act
            GalleryItemDto result = galleryService.createGalleryItem(request);

            // Assert
            assertFalse(result.isActive());
        }
    }

    // -------------------------------------------------------------------------
    // toggleActive tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("toggleActive")
    class ToggleActive {

        @Test
        @DisplayName("flips active from true to false")
        void toggleActive_activeItem_becomesInactive() {
            // Arrange
            GalleryItem item = new GalleryItem();
            item.setId(5L);
            item.setActive(true);   // currently active

            GalleryItem saved = new GalleryItem();
            saved.setId(5L);
            saved.setActive(false); // after toggling

            GalleryItemDto expectedDto = buildGalleryItemDto(5L, "Wedding 2024", false);

            when(galleryItemRepository.findById(5L)).thenReturn(Optional.of(item));
            when(galleryItemRepository.save(item)).thenReturn(saved);
            when(galleryItemMapper.toDto(saved)).thenReturn(expectedDto);

            // Act
            GalleryItemDto result = galleryService.toggleActive(5L);

            // Assert – the entity's flag must have been flipped before saving
            assertFalse(item.isActive(), "entity.active should be false after toggle");
            assertFalse(result.isActive());
            verify(galleryItemRepository).save(item);
        }

        @Test
        @DisplayName("flips active from false to true")
        void toggleActive_inactiveItem_becomesActive() {
            // Arrange
            GalleryItem item = new GalleryItem();
            item.setId(6L);
            item.setActive(false);  // currently inactive

            GalleryItem saved = new GalleryItem();
            saved.setId(6L);
            saved.setActive(true);  // after toggling

            GalleryItemDto expectedDto = buildGalleryItemDto(6L, "Easter 2024", true);

            when(galleryItemRepository.findById(6L)).thenReturn(Optional.of(item));
            when(galleryItemRepository.save(item)).thenReturn(saved);
            when(galleryItemMapper.toDto(saved)).thenReturn(expectedDto);

            // Act
            GalleryItemDto result = galleryService.toggleActive(6L);

            // Assert
            assertTrue(item.isActive(), "entity.active should be true after toggle");
            assertTrue(result.isActive());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when item does not exist")
        void toggleActive_notFound_throwsException() {
            // Arrange
            when(galleryItemRepository.findById(77L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> galleryService.toggleActive(77L));
            verify(galleryItemRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // deleteGalleryItem tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteGalleryItem")
    class DeleteGalleryItem {

        @Test
        @DisplayName("calls deleteById when gallery item exists")
        void deleteGalleryItem_success_deletesById() {
            // Arrange
            when(galleryItemRepository.existsById(3L)).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> galleryService.deleteGalleryItem(3L));

            // Assert
            verify(galleryItemRepository).deleteById(3L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when gallery item does not exist")
        void deleteGalleryItem_notFound_throwsException() {
            // Arrange
            when(galleryItemRepository.existsById(88L)).thenReturn(false);

            // Act & Assert
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> galleryService.deleteGalleryItem(88L)
            );
            assertTrue(ex.getMessage().contains("88"));
            verify(galleryItemRepository, never()).deleteById(any());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateGalleryItemRequest buildCreateRequest(String albumName, boolean active) {
        CreateGalleryItemRequest request = new CreateGalleryItemRequest();
        request.setTitle("Photo Title");
        request.setImageUrl("https://cdn.example.com/photo.jpg");
        request.setAlbumName(albumName);
        request.setDisplayOrder(1);
        request.setActive(active);
        return request;
    }

    private GalleryItemDto buildGalleryItemDto(Long id, String albumName, boolean active) {
        GalleryItemDto dto = new GalleryItemDto();
        dto.setId(id);
        dto.setTitle("Photo Title");
        dto.setImageUrl("https://cdn.example.com/photo.jpg");
        dto.setAlbumName(albumName);
        dto.setActive(active);
        return dto;
    }
}
