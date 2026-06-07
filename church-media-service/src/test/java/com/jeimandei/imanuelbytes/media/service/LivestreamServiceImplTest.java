package com.jeimandei.imanuelbytes.media.service;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.media.dto.CreateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.dto.LivestreamDto;
import com.jeimandei.imanuelbytes.media.entity.Livestream;
import com.jeimandei.imanuelbytes.media.mapper.LivestreamMapper;
import com.jeimandei.imanuelbytes.media.repository.LivestreamRepository;
import com.jeimandei.imanuelbytes.media.service.impl.LivestreamServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivestreamServiceImplTest {

    @Mock
    private LivestreamRepository livestreamRepository;

    @Mock
    private LivestreamMapper livestreamMapper;

    @InjectMocks
    private LivestreamServiceImpl livestreamService;

    // -------------------------------------------------------------------------
    // createLivestream tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("createLivestream")
    class CreateLivestream {

        @Test
        @DisplayName("saves and returns DTO when embed URL is valid")
        void createLivestream_validEmbedUrl_savesAndReturnsDto() {
            // Arrange
            CreateLivestreamRequest request = buildCreateRequest("https://www.youtube.com/embed/liveABC");

            Livestream entity = new Livestream();
            entity.setTitle("Sunday Service Live");

            Livestream saved = new Livestream();
            saved.setId(1L);
            saved.setTitle("Sunday Service Live");
            saved.setYoutubeEmbedUrl("https://www.youtube.com/embed/liveABC");

            LivestreamDto expectedDto = buildLivestreamDto(1L, "https://www.youtube.com/embed/liveABC", false);

            when(livestreamMapper.toEntity(request)).thenReturn(entity);
            when(livestreamRepository.save(entity)).thenReturn(saved);
            when(livestreamMapper.toDto(saved)).thenReturn(expectedDto);

            // Act
            LivestreamDto result = livestreamService.createLivestream(request);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("https://www.youtube.com/embed/liveABC", result.getYoutubeEmbedUrl());
            verify(livestreamRepository).save(entity);
        }

        @Test
        @DisplayName("throws ValidationException when URL does not match embed pattern")
        void createLivestream_invalidEmbedUrl_throwsValidationException() {
            // Arrange – a plain watch URL is not a valid embed URL
            CreateLivestreamRequest request = buildCreateRequest("https://www.youtube.com/watch?v=liveABC");

            // Act & Assert
            assertThrows(ValidationException.class, () -> livestreamService.createLivestream(request));
            verify(livestreamRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ValidationException when URL is a random non-YouTube string")
        void createLivestream_randomUrl_throwsValidationException() {
            // Arrange
            CreateLivestreamRequest request = buildCreateRequest("https://vimeo.com/12345678");

            // Act & Assert
            assertThrows(ValidationException.class, () -> livestreamService.createLivestream(request));
            verify(livestreamRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // activateLivestream tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("activateLivestream")
    class ActivateLivestream {

        @Test
        @DisplayName("deactivates all other active livestreams then activates the target")
        void activateLivestream_deactivatesOthersAndActivatesTarget() {
            // Arrange
            Long targetId = 10L;

            Livestream target = new Livestream();
            target.setId(targetId);
            target.setActive(false);

            Livestream otherActive1 = new Livestream();
            otherActive1.setId(20L);
            otherActive1.setActive(true);

            Livestream otherActive2 = new Livestream();
            otherActive2.setId(30L);
            otherActive2.setActive(true);

            LivestreamDto expectedDto = buildLivestreamDto(targetId, "https://www.youtube.com/embed/abc", true);

            when(livestreamRepository.findById(targetId)).thenReturn(Optional.of(target));
            when(livestreamRepository.findByActiveTrueOrderByScheduledStartDesc())
                    .thenReturn(List.of(otherActive1, otherActive2));
            when(livestreamRepository.save(any(Livestream.class))).thenReturn(target);
            when(livestreamMapper.toDto(target)).thenReturn(expectedDto);

            // Act
            LivestreamDto result = livestreamService.activateLivestream(targetId);

            // Assert – the two previously-active items should have been deactivated
            assertFalse(otherActive1.isActive(), "otherActive1 should have been deactivated");
            assertFalse(otherActive2.isActive(), "otherActive2 should have been deactivated");
            verify(livestreamRepository).save(otherActive1);
            verify(livestreamRepository).save(otherActive2);

            // The target should be activated and saved
            assertTrue(target.isActive(), "target should be activated");
            verify(livestreamRepository).save(target);
            assertTrue(result.isActive());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when target livestream does not exist")
        void activateLivestream_notFound_throwsException() {
            // Arrange
            when(livestreamRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> livestreamService.activateLivestream(99L));
            verify(livestreamRepository, never()).findByActiveTrueOrderByScheduledStartDesc();
        }

        @Test
        @DisplayName("does not deactivate the target even if it already appears in the active list")
        void activateLivestream_targetAlreadyActive_notDeactivated() {
            // Arrange
            Long targetId = 10L;

            Livestream target = new Livestream();
            target.setId(targetId);
            target.setActive(true);

            LivestreamDto expectedDto = buildLivestreamDto(targetId, "https://www.youtube.com/embed/abc", true);

            when(livestreamRepository.findById(targetId)).thenReturn(Optional.of(target));
            // active list contains only the target itself
            when(livestreamRepository.findByActiveTrueOrderByScheduledStartDesc())
                    .thenReturn(List.of(target));
            when(livestreamRepository.save(target)).thenReturn(target);
            when(livestreamMapper.toDto(target)).thenReturn(expectedDto);

            // Act
            livestreamService.activateLivestream(targetId);

            // Assert – target must still be active; save only called once (for the activation)
            assertTrue(target.isActive());
            ArgumentCaptor<Livestream> captor = ArgumentCaptor.forClass(Livestream.class);
            verify(livestreamRepository, times(1)).save(captor.capture());
            assertEquals(targetId, captor.getValue().getId());
        }
    }

    // -------------------------------------------------------------------------
    // getActiveLivestream tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getActiveLivestream")
    class GetActiveLivestream {

        @Test
        @DisplayName("returns empty Optional when no livestream is active")
        void getActiveLivestream_noneActive_returnsEmptyOptional() {
            // Arrange
            when(livestreamRepository.findFirstByActiveTrueOrderByScheduledStartDesc())
                    .thenReturn(Optional.empty());

            // Act
            Optional<LivestreamDto> result = livestreamService.getActiveLivestream();

            // Assert
            assertTrue(result.isEmpty());
            verify(livestreamMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("returns mapped DTO when an active livestream exists")
        void getActiveLivestream_activeExists_returnsDto() {
            // Arrange
            Livestream active = new Livestream();
            active.setId(7L);
            active.setActive(true);
            active.setTitle("Live Now");

            LivestreamDto dto = buildLivestreamDto(7L, "https://www.youtube.com/embed/live7", true);

            when(livestreamRepository.findFirstByActiveTrueOrderByScheduledStartDesc())
                    .thenReturn(Optional.of(active));
            when(livestreamMapper.toDto(active)).thenReturn(dto);

            // Act
            Optional<LivestreamDto> result = livestreamService.getActiveLivestream();

            // Assert
            assertTrue(result.isPresent());
            assertEquals(7L, result.get().getId());
            assertTrue(result.get().isActive());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateLivestreamRequest buildCreateRequest(String embedUrl) {
        CreateLivestreamRequest request = new CreateLivestreamRequest();
        request.setTitle("Sunday Service Live");
        request.setYoutubeEmbedUrl(embedUrl);
        request.setScheduledStart(LocalDateTime.of(2025, 6, 8, 9, 0));
        return request;
    }

    private LivestreamDto buildLivestreamDto(Long id, String embedUrl, boolean active) {
        LivestreamDto dto = new LivestreamDto();
        dto.setId(id);
        dto.setTitle("Sunday Service Live");
        dto.setYoutubeEmbedUrl(embedUrl);
        dto.setActive(active);
        return dto;
    }
}
