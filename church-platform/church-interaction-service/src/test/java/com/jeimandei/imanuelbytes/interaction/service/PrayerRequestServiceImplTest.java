package com.jeimandei.imanuelbytes.interaction.service;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.dto.CreatePrayerRequestRequest;
import com.jeimandei.imanuelbytes.interaction.dto.PrayerRequestDto;
import com.jeimandei.imanuelbytes.interaction.dto.UpdatePrayerRequestStatusRequest;
import com.jeimandei.imanuelbytes.interaction.entity.PrayerRequest;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.repository.PrayerRequestRepository;
import com.jeimandei.imanuelbytes.interaction.service.impl.PrayerRequestServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrayerRequestServiceImplTest {

    @Mock
    private PrayerRequestRepository prayerRequestRepository;

    @InjectMocks
    private PrayerRequestServiceImpl prayerRequestService;

    // -------------------------------------------------------------------------
    // submitPrayerRequest tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("submitPrayerRequest")
    class SubmitPrayerRequest {

        @Test
        @DisplayName("saves prayer request and returns DTO with NEW status")
        void submitPrayerRequest_success_returnsDto() {
            // Arrange
            CreatePrayerRequestRequest request = buildCreateRequest("Ana", "ana@church.id",
                    "Please pray for my family", false);

            // PrayerRequestServiceImpl builds the entity directly (no mapper), so we
            // capture what gets passed to save()
            PrayerRequest persisted = new PrayerRequest(
                    request.getName(), request.getEmail(), request.getPhone(),
                    request.getMessage(), request.isConfidential());
            persisted.setId(1L);
            persisted.setCreatedAt(LocalDateTime.now());

            when(prayerRequestRepository.save(any(PrayerRequest.class))).thenReturn(persisted);

            // Act
            PrayerRequestDto result = prayerRequestService.submitPrayerRequest(request);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Ana", result.getName());
            assertEquals("ana@church.id", result.getEmail());
            assertEquals("Please pray for my family", result.getMessage());
            assertFalse(result.isConfidential());
            assertEquals(RequestStatus.NEW, result.getStatus());

            // Verify the entity passed to save has the correct values
            ArgumentCaptor<PrayerRequest> captor = ArgumentCaptor.forClass(PrayerRequest.class);
            verify(prayerRequestRepository).save(captor.capture());
            PrayerRequest capturedEntity = captor.getValue();
            assertEquals("Ana", capturedEntity.getName());
            assertEquals(RequestStatus.NEW, capturedEntity.getStatus());
        }

        @Test
        @DisplayName("marks confidential request with confidential=true")
        void submitPrayerRequest_confidential_flagIsSet() {
            // Arrange
            CreatePrayerRequestRequest request = buildCreateRequest("Budi", "budi@church.id",
                    "Confidential matter", true);

            PrayerRequest persisted = new PrayerRequest(
                    request.getName(), request.getEmail(), request.getPhone(),
                    request.getMessage(), request.isConfidential());
            persisted.setId(2L);

            when(prayerRequestRepository.save(any(PrayerRequest.class))).thenReturn(persisted);

            // Act
            PrayerRequestDto result = prayerRequestService.submitPrayerRequest(request);

            // Assert
            assertTrue(result.isConfidential());
        }
    }

    // -------------------------------------------------------------------------
    // updateStatus tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("updates status and returns DTO when prayer request exists")
        void updateStatus_success_returnsUpdatedDto() {
            // Arrange
            PrayerRequest existing = new PrayerRequest("Sari", "sari@church.id", null,
                    "Please pray for healing", false);
            existing.setId(10L);
            existing.setCreatedAt(LocalDateTime.now());
            // status starts as NEW (set by constructor)

            UpdatePrayerRequestStatusRequest statusRequest = new UpdatePrayerRequestStatusRequest();
            statusRequest.setStatus(RequestStatus.PRAYED);

            when(prayerRequestRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(prayerRequestRepository.save(existing)).thenReturn(existing);

            // Act
            PrayerRequestDto result = prayerRequestService.updateStatus(10L, statusRequest);

            // Assert
            assertEquals(RequestStatus.PRAYED, result.getStatus());
            assertEquals(RequestStatus.PRAYED, existing.getStatus()); // mutation on entity
            verify(prayerRequestRepository).save(existing);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when prayer request does not exist")
        void updateStatus_notFound_throwsException() {
            // Arrange
            when(prayerRequestRepository.findById(99L)).thenReturn(Optional.empty());

            UpdatePrayerRequestStatusRequest statusRequest = new UpdatePrayerRequestStatusRequest();
            statusRequest.setStatus(RequestStatus.REVIEWED);

            // Act & Assert
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> prayerRequestService.updateStatus(99L, statusRequest)
            );
            assertTrue(ex.getMessage().contains("99"));
            verify(prayerRequestRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // getAllPrayerRequests tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getAllPrayerRequests")
    class GetAllPrayerRequests {

        @Test
        @DisplayName("returns a page of PrayerRequestDtos")
        void getAllPrayerRequests_returnsPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            PrayerRequest pr1 = new PrayerRequest("Alice", "alice@church.id", null, "Pray for me", false);
            pr1.setId(1L);
            pr1.setCreatedAt(LocalDateTime.now());

            PrayerRequest pr2 = new PrayerRequest("Bob", "bob@church.id", null, "Healing please", true);
            pr2.setId(2L);
            pr2.setCreatedAt(LocalDateTime.now());

            Page<PrayerRequest> repoPage = new PageImpl<>(List.of(pr1, pr2), pageable, 2);
            when(prayerRequestRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(repoPage);

            // Act
            Page<PrayerRequestDto> result = prayerRequestService.getAllPrayerRequests(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());
            assertEquals("Alice", result.getContent().get(0).getName());
            assertEquals("Bob", result.getContent().get(1).getName());
            verify(prayerRequestRepository).findAllByOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("returns empty page when there are no prayer requests")
        void getAllPrayerRequests_emptyPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<PrayerRequest> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(prayerRequestRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(emptyPage);

            // Act
            Page<PrayerRequestDto> result = prayerRequestService.getAllPrayerRequests(pageable);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreatePrayerRequestRequest buildCreateRequest(String name, String email,
                                                          String message, boolean confidential) {
        CreatePrayerRequestRequest req = new CreatePrayerRequestRequest();
        req.setName(name);
        req.setEmail(email);
        req.setMessage(message);
        req.setConfidential(confidential);
        return req;
    }
}
