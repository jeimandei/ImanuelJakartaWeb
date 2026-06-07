package com.jeimandei.imanuelbytes.event.mapper;

import com.jeimandei.imanuelbytes.event.dto.CreateEventRequest;
import com.jeimandei.imanuelbytes.event.dto.EventDto;
import com.jeimandei.imanuelbytes.event.dto.FeaturedEventDto;
import com.jeimandei.imanuelbytes.event.entity.Event;
import com.jeimandei.imanuelbytes.event.entity.EventStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;

/**
 * MapStruct mapper that converts between {@link Event} entities and their
 * DTO representations.
 *
 * <p>The {@code upcoming} computed field is derived from the entity's
 * {@code eventStart} and {@code status} fields inside
 * {@link #toDto(Event)}.</p>
 */
@Mapper(componentModel = "spring")
public interface EventMapper {

    /**
     * Maps an {@link Event} entity to a full {@link EventDto}.
     *
     * <p>The {@code upcoming} flag is computed after mapping via
     * {@link #afterToDto(EventDto, Event)}.</p>
     *
     * @param event the source entity
     * @return the populated DTO
     */
    @Mapping(target = "upcoming", ignore = true)
    EventDto toDto(Event event);

    /**
     * Post-mapping step that computes the {@code upcoming} derived flag.
     *
     * @param dto   the partially-mapped DTO (target)
     * @param event the source entity
     */
    default void afterToDto(EventDto dto, Event event) {
        boolean isUpcoming = event.getEventStart() != null
                && event.getEventStart().isAfter(LocalDateTime.now())
                && EventStatus.PUBLISHED.equals(event.getStatus());
        dto.setUpcoming(isUpcoming);
    }

    /**
     * Full mapping with the computed upcoming flag applied.
     *
     * @param event the source entity
     * @return fully populated DTO including the upcoming flag
     */
    default EventDto toDtoWithUpcoming(Event event) {
        EventDto dto = toDto(event);
        afterToDto(dto, event);
        return dto;
    }

    /**
     * Maps an {@link Event} entity to the lightweight {@link FeaturedEventDto}.
     *
     * @param event the source entity
     * @return the projected DTO
     */
    FeaturedEventDto toFeaturedDto(Event event);

    /**
     * Creates a new {@link Event} entity from a {@link CreateEventRequest}.
     *
     * <p>The {@code id}, {@code createdAt}, and {@code updatedAt} audit fields
     * are managed by JPA and must not be set by the mapper.</p>
     *
     * @param request the incoming creation request
     * @return a new, transient Event entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(CreateEventRequest request);

    /**
     * Applies non-null fields from an update request onto an existing entity.
     *
     * <p>The {@code id}, {@code createdAt}, and {@code updatedAt} fields are
     * never overwritten.</p>
     *
     * @param target  the managed entity to update (mutated in place)
     * @param source  map of field names to new values supplied by the caller
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Event target,
                      com.jeimandei.imanuelbytes.event.dto.UpdateEventRequest source);
}
