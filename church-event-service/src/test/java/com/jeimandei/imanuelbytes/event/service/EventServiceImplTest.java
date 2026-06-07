package com.jeimandei.imanuelbytes.event.service;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.event.dto.CreateEventRequest;
import com.jeimandei.imanuelbytes.event.dto.EventDto;
import com.jeimandei.imanuelbytes.event.entity.Event;
import com.jeimandei.imanuelbytes.event.entity.EventStatus;
import com.jeimandei.imanuelbytes.event.mapper.EventMapper;
import com.jeimandei.imanuelbytes.event.repository.EventRepository;
import com.jeimandei.imanuelbytes.event.service.impl.EventServiceImpl;
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
@DisplayName("EventServiceImpl")
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private Event buildEvent(Long id, String title, String slug, EventStatus status, boolean featured) {
        Event event = new Event();
        event.setId(id);
        event.setTitle(title);
        event.setSlug(slug);
        event.setStatus(status);
        event.setFeatured(featured);
        event.setEventStart(LocalDateTime.now().plusDays(7));
        return event;
    }

    private EventDto buildDto(Long id, String title, String slug, EventStatus status, boolean featured) {
        EventDto dto = new EventDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setSlug(slug);
        dto.setStatus(status);
        dto.setFeatured(featured);
        return dto;
    }

    // ---------------------------------------------------------------------------
    // createEvent
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("createEvent")
    class CreateEvent {

        @Test
        @DisplayName("creates event with slug generated from title when request slug is blank")
        void createsEvent_withSlugFromTitle_whenRequestSlugIsBlank() {
            CreateEventRequest request = new CreateEventRequest();
            request.setTitle("Christmas Service");
            request.setSlug(null); // null – should generate from title
            request.setEventStart(LocalDateTime.now().plusDays(30));
            request.setStatus(EventStatus.DRAFT);

            // SlugUtils.toSlug("Christmas Service") → "christmas-service"
            String expectedSlug = "christmas-service";

            Event mappedEntity = buildEvent(null, "Christmas Service", null, EventStatus.DRAFT, false);
            Event savedEntity = buildEvent(1L, "Christmas Service", expectedSlug, EventStatus.DRAFT, false);
            EventDto expectedDto = buildDto(1L, "Christmas Service", expectedSlug, EventStatus.DRAFT, false);

            when(eventRepository.existsBySlug(expectedSlug)).thenReturn(false);
            when(eventMapper.toEntity(request)).thenReturn(mappedEntity);
            when(eventRepository.save(any(Event.class))).thenReturn(savedEntity);
            when(eventMapper.toDtoWithUpcoming(savedEntity)).thenReturn(expectedDto);

            EventDto result = eventService.createEvent(request);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(expectedSlug, result.getSlug());
            assertEquals(EventStatus.DRAFT, result.getStatus());

            ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(captor.capture());
            assertEquals(expectedSlug, captor.getValue().getSlug());

            verify(eventRepository).existsBySlug(expectedSlug);
        }

        @Test
        @DisplayName("creates event with explicit slug when provided in request")
        void createsEvent_withExplicitSlug_whenProvided() {
            CreateEventRequest request = new CreateEventRequest();
            request.setTitle("Christmas Service");
            request.setSlug("My Christmas Slug"); // will be slugified to "my-christmas-slug"
            request.setEventStart(LocalDateTime.now().plusDays(30));

            String expectedSlug = "my-christmas-slug";

            Event mappedEntity = buildEvent(null, "Christmas Service", null, EventStatus.DRAFT, false);
            Event savedEntity = buildEvent(2L, "Christmas Service", expectedSlug, EventStatus.DRAFT, false);
            EventDto expectedDto = buildDto(2L, "Christmas Service", expectedSlug, EventStatus.DRAFT, false);

            when(eventRepository.existsBySlug(expectedSlug)).thenReturn(false);
            when(eventMapper.toEntity(request)).thenReturn(mappedEntity);
            when(eventRepository.save(any(Event.class))).thenReturn(savedEntity);
            when(eventMapper.toDtoWithUpcoming(savedEntity)).thenReturn(expectedDto);

            EventDto result = eventService.createEvent(request);

            assertNotNull(result);
            assertEquals(expectedSlug, result.getSlug());
            verify(eventRepository).existsBySlug(expectedSlug);
        }

        @Test
        @DisplayName("throws ValidationException when slug is already taken")
        void throwsValidationException_whenSlugAlreadyExists() {
            CreateEventRequest request = new CreateEventRequest();
            request.setTitle("Christmas Service");
            request.setSlug("christmas-service");
            request.setEventStart(LocalDateTime.now().plusDays(30));

            when(eventRepository.existsBySlug("christmas-service")).thenReturn(true);

            ValidationException ex = assertThrows(
                    ValidationException.class,
                    () -> eventService.createEvent(request)
            );

            assertTrue(ex.getMessage().contains("christmas-service"));

            verify(eventRepository).existsBySlug("christmas-service");
            verify(eventRepository, never()).save(any());
            verifyNoInteractions(eventMapper);
        }

        @Test
        @DisplayName("defaults status to DRAFT when mapped entity has null status")
        void setsDefaultDraftStatus_whenEntityStatusIsNull() {
            CreateEventRequest request = new CreateEventRequest();
            request.setTitle("Null Status Event");
            request.setSlug("null-status-event");
            request.setEventStart(LocalDateTime.now().plusDays(10));

            Event mappedEntity = new Event();
            mappedEntity.setTitle("Null Status Event");
            mappedEntity.setStatus(null); // simulate mapper returning null status

            Event savedEntity = buildEvent(4L, "Null Status Event", "null-status-event", EventStatus.DRAFT, false);
            EventDto expectedDto = buildDto(4L, "Null Status Event", "null-status-event", EventStatus.DRAFT, false);

            when(eventRepository.existsBySlug("null-status-event")).thenReturn(false);
            when(eventMapper.toEntity(request)).thenReturn(mappedEntity);
            when(eventRepository.save(any(Event.class))).thenReturn(savedEntity);
            when(eventMapper.toDtoWithUpcoming(savedEntity)).thenReturn(expectedDto);

            eventService.createEvent(request);

            ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(captor.capture());
            assertEquals(EventStatus.DRAFT, captor.getValue().getStatus());
        }
    }

    // ---------------------------------------------------------------------------
    // getEventBySlug
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("getEventBySlug")
    class GetEventBySlug {

        @Test
        @DisplayName("returns DTO when event with given slug exists")
        void returnsDto_whenEventExists() {
            Event event = buildEvent(1L, "Easter Service", "easter-service", EventStatus.PUBLISHED, false);
            EventDto expectedDto = buildDto(1L, "Easter Service", "easter-service", EventStatus.PUBLISHED, false);

            when(eventRepository.findBySlug("easter-service")).thenReturn(Optional.of(event));
            when(eventMapper.toDtoWithUpcoming(event)).thenReturn(expectedDto);

            EventDto result = eventService.getEventBySlug("easter-service");

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("easter-service", result.getSlug());
            assertEquals(EventStatus.PUBLISHED, result.getStatus());

            verify(eventRepository).findBySlug("easter-service");
            verify(eventMapper).toDtoWithUpcoming(event);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when no event matches slug")
        void throwsResourceNotFoundException_whenSlugNotFound() {
            when(eventRepository.findBySlug("nonexistent-slug")).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> eventService.getEventBySlug("nonexistent-slug")
            );

            assertTrue(ex.getMessage().contains("nonexistent-slug"));
            verify(eventRepository).findBySlug("nonexistent-slug");
            verifyNoInteractions(eventMapper);
        }
    }

    // ---------------------------------------------------------------------------
    // publishEvent
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("publishEvent")
    class PublishEvent {

        @Test
        @DisplayName("changes event status to PUBLISHED")
        void changesStatusToPublished() {
            Event event = buildEvent(1L, "Sunday Worship", "sunday-worship", EventStatus.DRAFT, false);
            Event savedEvent = buildEvent(1L, "Sunday Worship", "sunday-worship", EventStatus.PUBLISHED, false);
            EventDto expectedDto = buildDto(1L, "Sunday Worship", "sunday-worship", EventStatus.PUBLISHED, false);

            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
            when(eventMapper.toDtoWithUpcoming(savedEvent)).thenReturn(expectedDto);

            EventDto result = eventService.publishEvent(1L);

            assertNotNull(result);
            assertEquals(EventStatus.PUBLISHED, result.getStatus());

            ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(captor.capture());
            assertEquals(EventStatus.PUBLISHED, captor.getValue().getStatus());

            verify(eventRepository).findById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when event to publish does not exist")
        void throwsResourceNotFoundException_whenEventNotFound() {
            when(eventRepository.findById(88L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> eventService.publishEvent(88L)
            );

            verify(eventRepository).findById(88L);
            verify(eventRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // cancelEvent
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("cancelEvent")
    class CancelEvent {

        @Test
        @DisplayName("changes event status to CANCELLED")
        void changesStatusToCancelled() {
            Event event = buildEvent(1L, "Youth Camp", "youth-camp", EventStatus.PUBLISHED, false);
            Event savedEvent = buildEvent(1L, "Youth Camp", "youth-camp", EventStatus.CANCELLED, false);
            EventDto expectedDto = buildDto(1L, "Youth Camp", "youth-camp", EventStatus.CANCELLED, false);

            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
            when(eventMapper.toDtoWithUpcoming(savedEvent)).thenReturn(expectedDto);

            EventDto result = eventService.cancelEvent(1L);

            assertNotNull(result);
            assertEquals(EventStatus.CANCELLED, result.getStatus());

            ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(captor.capture());
            assertEquals(EventStatus.CANCELLED, captor.getValue().getStatus());

            verify(eventRepository).findById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when event to cancel does not exist")
        void throwsResourceNotFoundException_whenEventNotFound() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> eventService.cancelEvent(99L)
            );

            verify(eventRepository).findById(99L);
            verify(eventRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // toggleFeatured
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("toggleFeatured")
    class ToggleFeatured {

        @Test
        @DisplayName("flips featured from false to true")
        void togglesFeatured_fromFalseToTrue() {
            Event event = buildEvent(1L, "Special Event", "special-event", EventStatus.PUBLISHED, false);
            Event savedEvent = buildEvent(1L, "Special Event", "special-event", EventStatus.PUBLISHED, true);
            EventDto expectedDto = buildDto(1L, "Special Event", "special-event", EventStatus.PUBLISHED, true);

            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
            when(eventMapper.toDtoWithUpcoming(savedEvent)).thenReturn(expectedDto);

            EventDto result = eventService.toggleFeatured(1L);

            assertNotNull(result);
            assertTrue(result.isFeatured());

            ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(captor.capture());
            assertTrue(captor.getValue().isFeatured()); // was false, now true

            verify(eventRepository).findById(1L);
        }

        @Test
        @DisplayName("flips featured from true to false")
        void togglesFeatured_fromTrueToFalse() {
            Event event = buildEvent(2L, "Featured Event", "featured-event", EventStatus.PUBLISHED, true);
            Event savedEvent = buildEvent(2L, "Featured Event", "featured-event", EventStatus.PUBLISHED, false);
            EventDto expectedDto = buildDto(2L, "Featured Event", "featured-event", EventStatus.PUBLISHED, false);

            when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
            when(eventMapper.toDtoWithUpcoming(savedEvent)).thenReturn(expectedDto);

            EventDto result = eventService.toggleFeatured(2L);

            assertNotNull(result);
            assertFalse(result.isFeatured());

            ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(captor.capture());
            assertFalse(captor.getValue().isFeatured()); // was true, now false

            verify(eventRepository).findById(2L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when event to toggle does not exist")
        void throwsResourceNotFoundException_whenEventNotFound() {
            when(eventRepository.findById(123L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> eventService.toggleFeatured(123L)
            );

            verify(eventRepository).findById(123L);
            verify(eventRepository, never()).save(any());
        }
    }
}
