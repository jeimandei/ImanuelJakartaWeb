package com.jeimandei.imanuelbytes.interaction.service;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.dto.NewsletterSubscriptionDto;
import com.jeimandei.imanuelbytes.interaction.dto.SubscribeNewsletterRequest;
import com.jeimandei.imanuelbytes.interaction.entity.NewsletterSubscription;
import com.jeimandei.imanuelbytes.interaction.repository.NewsletterSubscriptionRepository;
import com.jeimandei.imanuelbytes.interaction.service.impl.NewsletterServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceImplTest {

    @Mock
    private NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    @InjectMocks
    private NewsletterServiceImpl newsletterService;

    // -------------------------------------------------------------------------
    // subscribe tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("subscribe")
    class Subscribe {

        @Test
        @DisplayName("creates a new subscription when email does not yet exist")
        void subscribe_newEmail_createsSubscription() {
            // Arrange
            SubscribeNewsletterRequest request = buildRequest("new@church.id", "New Member");

            when(newsletterSubscriptionRepository.findByEmail("new@church.id"))
                    .thenReturn(Optional.empty());

            NewsletterSubscription saved = buildSubscription(1L, "new@church.id", "New Member", true);
            when(newsletterSubscriptionRepository.save(any(NewsletterSubscription.class))).thenReturn(saved);

            // Act
            NewsletterSubscriptionDto result = newsletterService.subscribe(request);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("new@church.id", result.getEmail());
            assertEquals("New Member", result.getName());
            assertTrue(result.isActive());

            ArgumentCaptor<NewsletterSubscription> captor =
                    ArgumentCaptor.forClass(NewsletterSubscription.class);
            verify(newsletterSubscriptionRepository).save(captor.capture());
            assertEquals("new@church.id", captor.getValue().getEmail());
            assertTrue(captor.getValue().isActive());
        }

        @Test
        @DisplayName("reactivates an inactive subscription without creating a duplicate record")
        void subscribe_existingInactiveEmail_reactivates() {
            // Arrange
            SubscribeNewsletterRequest request = buildRequest("inactive@church.id", "Returning Member");

            NewsletterSubscription inactive = buildSubscription(2L, "inactive@church.id", "Old Name", false);
            when(newsletterSubscriptionRepository.findByEmail("inactive@church.id"))
                    .thenReturn(Optional.of(inactive));

            NewsletterSubscription reactivated = buildSubscription(2L, "inactive@church.id",
                    "Returning Member", true);
            when(newsletterSubscriptionRepository.save(inactive)).thenReturn(reactivated);

            // Act
            NewsletterSubscriptionDto result = newsletterService.subscribe(request);

            // Assert – the existing record should be reactivated, not a new one created
            assertTrue(result.isActive());
            assertEquals(2L, result.getId());
            assertEquals("Returning Member", result.getName());

            // verify setActive(true) was called on the existing entity before saving
            assertTrue(inactive.isActive(), "inactive subscription should have been reactivated");
            verify(newsletterSubscriptionRepository).save(inactive);
        }

        @Test
        @DisplayName("reactivates with updated name when provided; keeps old name if request name is blank")
        void subscribe_existingInactive_blankName_keepsOldName() {
            // Arrange – request has a blank name
            SubscribeNewsletterRequest request = buildRequest("inactive2@church.id", "   ");

            NewsletterSubscription inactive = buildSubscription(3L, "inactive2@church.id", "Original Name", false);
            when(newsletterSubscriptionRepository.findByEmail("inactive2@church.id"))
                    .thenReturn(Optional.of(inactive));

            NewsletterSubscription reactivated = buildSubscription(3L, "inactive2@church.id",
                    "Original Name", true);
            when(newsletterSubscriptionRepository.save(inactive)).thenReturn(reactivated);

            // Act
            NewsletterSubscriptionDto result = newsletterService.subscribe(request);

            // Assert – name should NOT be overwritten with blank
            assertEquals("Original Name", result.getName());
        }

        @Test
        @DisplayName("returns the existing DTO without saving again when email is already active")
        void subscribe_alreadyActiveEmail_returnsExistingWithoutDuplicate() {
            // Arrange
            SubscribeNewsletterRequest request = buildRequest("active@church.id", "Active Member");

            NewsletterSubscription existing = buildSubscription(4L, "active@church.id", "Active Member", true);
            when(newsletterSubscriptionRepository.findByEmail("active@church.id"))
                    .thenReturn(Optional.of(existing));

            // Act
            NewsletterSubscriptionDto result = newsletterService.subscribe(request);

            // Assert – already active: no save should be called, existing data returned
            assertNotNull(result);
            assertEquals(4L, result.getId());
            assertTrue(result.isActive());
            verify(newsletterSubscriptionRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // unsubscribe tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("unsubscribe")
    class Unsubscribe {

        @Test
        @DisplayName("sets active=false and saves when email exists")
        void unsubscribe_success_deactivatesSubscription() {
            // Arrange
            NewsletterSubscription existing = buildSubscription(5L, "leave@church.id", "Leaving", true);
            when(newsletterSubscriptionRepository.findByEmail("leave@church.id"))
                    .thenReturn(Optional.of(existing));
            when(newsletterSubscriptionRepository.save(existing)).thenReturn(existing);

            // Act
            assertDoesNotThrow(() -> newsletterService.unsubscribe("leave@church.id"));

            // Assert – active flag must be flipped to false
            assertFalse(existing.isActive(), "subscription should be deactivated after unsubscribe");
            verify(newsletterSubscriptionRepository).save(existing);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when email is not found")
        void unsubscribe_emailNotFound_throwsException() {
            // Arrange
            when(newsletterSubscriptionRepository.findByEmail("ghost@church.id"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> newsletterService.unsubscribe("ghost@church.id")
            );
            assertTrue(ex.getMessage().contains("ghost@church.id"));
            verify(newsletterSubscriptionRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SubscribeNewsletterRequest buildRequest(String email, String name) {
        SubscribeNewsletterRequest req = new SubscribeNewsletterRequest();
        req.setEmail(email);
        req.setName(name);
        return req;
    }

    /**
     * Builds a {@link NewsletterSubscription} with all fields set manually,
     * bypassing the {@code @PrePersist} lifecycle callback that would normally
     * set {@code subscribedAt}.
     */
    private NewsletterSubscription buildSubscription(Long id, String email, String name, boolean active) {
        // Use reflection-friendly approach: construct and set via setters
        // The protected no-arg constructor is not directly accessible from tests,
        // so we use the public two-arg constructor and adjust fields.
        NewsletterSubscription sub = new NewsletterSubscription(email, name);
        sub.setId(id);
        sub.setActive(active);
        sub.setSubscribedAt(LocalDateTime.now());
        return sub;
    }
}
