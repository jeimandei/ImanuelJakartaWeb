package com.jeimandei.imanuelbytes.media.service;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.media.dto.CreateSermonRequest;
import com.jeimandei.imanuelbytes.media.dto.SermonDto;
import com.jeimandei.imanuelbytes.media.entity.Sermon;
import com.jeimandei.imanuelbytes.media.mapper.SermonMapper;
import com.jeimandei.imanuelbytes.media.repository.SermonRepository;
import com.jeimandei.imanuelbytes.media.service.impl.SermonServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SermonServiceImplTest {

    @Mock
    private SermonRepository sermonRepository;

    @Mock
    private SermonMapper sermonMapper;

    @InjectMocks
    private SermonServiceImpl sermonService;

    // -------------------------------------------------------------------------
    // createSermon
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createSermon: watch?v= URL is normalised to embed format")
    void createSermon_normalizesWatchUrl_toEmbedUrl() {
        // Arrange
        CreateSermonRequest request = buildCreateRequest("https://www.youtube.com/watch?v=abc123");
        Sermon entity = new Sermon();
        SermonDto expectedDto = buildSermonDto(1L, "https://www.youtube.com/embed/abc123");

        when(sermonMapper.toEntity(request)).thenReturn(entity);
        when(sermonRepository.save(entity)).thenReturn(entity);
        when(sermonMapper.toDto(entity)).thenReturn(expectedDto);

        // Act
        SermonDto result = sermonService.createSermon(request);

        // Assert
        assertEquals("https://www.youtube.com/embed/abc123", result.getYoutubeUrl());
        // Verify the entity had the normalised URL set before being saved
        ArgumentCaptor<Sermon> savedCaptor = ArgumentCaptor.forClass(Sermon.class);
        verify(sermonRepository).save(savedCaptor.capture());
        assertEquals("https://www.youtube.com/embed/abc123", savedCaptor.getValue().getYoutubeUrl());
    }

    @Test
    @DisplayName("createSermon: watch?v= URL with extra query params strips them correctly")
    void createSermon_normalizesWatchUrlWithQueryParams() {
        // Arrange
        CreateSermonRequest request = buildCreateRequest(
                "https://www.youtube.com/watch?v=xyz789&list=PLabc&index=2");
        Sermon entity = new Sermon();
        SermonDto expectedDto = buildSermonDto(2L, "https://www.youtube.com/embed/xyz789");

        when(sermonMapper.toEntity(request)).thenReturn(entity);
        when(sermonRepository.save(entity)).thenReturn(entity);
        when(sermonMapper.toDto(entity)).thenReturn(expectedDto);

        // Act
        SermonDto result = sermonService.createSermon(request);

        // Assert
        ArgumentCaptor<Sermon> savedCaptor = ArgumentCaptor.forClass(Sermon.class);
        verify(sermonRepository).save(savedCaptor.capture());
        assertEquals("https://www.youtube.com/embed/xyz789", savedCaptor.getValue().getYoutubeUrl());
    }

    @Test
    @DisplayName("createSermon: already-embed URL is stored unchanged")
    void createSermon_embedUrl_remainsUnchanged() {
        // Arrange
        String embedUrl = "https://www.youtube.com/embed/def456";
        CreateSermonRequest request = buildCreateRequest(embedUrl);
        Sermon entity = new Sermon();
        SermonDto expectedDto = buildSermonDto(3L, embedUrl);

        when(sermonMapper.toEntity(request)).thenReturn(entity);
        when(sermonRepository.save(entity)).thenReturn(entity);
        when(sermonMapper.toDto(entity)).thenReturn(expectedDto);

        // Act
        sermonService.createSermon(request);

        // Assert
        ArgumentCaptor<Sermon> savedCaptor = ArgumentCaptor.forClass(Sermon.class);
        verify(sermonRepository).save(savedCaptor.capture());
        assertEquals(embedUrl, savedCaptor.getValue().getYoutubeUrl());
    }

    // -------------------------------------------------------------------------
    // getSermonById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getSermonById: returns DTO when sermon exists")
    void getSermonById_found_returnsDto() {
        // Arrange
        Sermon sermon = new Sermon();
        sermon.setId(10L);
        SermonDto dto = buildSermonDto(10L, null);

        when(sermonRepository.findById(10L)).thenReturn(Optional.of(sermon));
        when(sermonMapper.toDto(sermon)).thenReturn(dto);

        // Act
        SermonDto result = sermonService.getSermonById(10L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(sermonRepository).findById(10L);
    }

    @Test
    @DisplayName("getSermonById: throws ResourceNotFoundException when not found")
    void getSermonById_notFound_throwsException() {
        // Arrange
        when(sermonRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> sermonService.getSermonById(99L)
        );
        assertTrue(ex.getMessage().contains("99"));
        verify(sermonRepository).findById(99L);
    }

    // -------------------------------------------------------------------------
    // deleteSermon
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteSermon: calls deleteById when sermon exists")
    void deleteSermon_success_deletesById() {
        // Arrange
        when(sermonRepository.existsById(5L)).thenReturn(true);

        // Act
        sermonService.deleteSermon(5L);

        // Assert
        verify(sermonRepository).existsById(5L);
        verify(sermonRepository).deleteById(5L);
    }

    @Test
    @DisplayName("deleteSermon: throws ResourceNotFoundException when sermon not found")
    void deleteSermon_notFound_throwsException() {
        // Arrange
        when(sermonRepository.existsById(55L)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> sermonService.deleteSermon(55L)
        );
        assertTrue(ex.getMessage().contains("55"));
        verify(sermonRepository, never()).deleteById(any());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateSermonRequest buildCreateRequest(String youtubeUrl) {
        CreateSermonRequest req = new CreateSermonRequest();
        req.setTitle("Test Sermon");
        req.setSpeaker("Pastor John");
        req.setSermonDate(LocalDate.of(2025, 6, 1));
        req.setYoutubeUrl(youtubeUrl);
        return req;
    }

    private SermonDto buildSermonDto(Long id, String youtubeUrl) {
        SermonDto dto = new SermonDto();
        dto.setId(id);
        dto.setTitle("Test Sermon");
        dto.setSpeaker("Pastor John");
        dto.setYoutubeUrl(youtubeUrl);
        return dto;
    }
}
