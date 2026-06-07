package com.jeimandei.imanuelbytes.event.mapper;

import com.jeimandei.imanuelbytes.event.dto.CreateEventRequest;
import com.jeimandei.imanuelbytes.event.dto.EventDto;
import com.jeimandei.imanuelbytes.event.dto.FeaturedEventDto;
import com.jeimandei.imanuelbytes.event.dto.UpdateEventRequest;
import com.jeimandei.imanuelbytes.event.entity.Event;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T11:12:11+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public EventDto toDto(Event event) {
        if ( event == null ) {
            return null;
        }

        EventDto eventDto = new EventDto();

        eventDto.setId( event.getId() );
        eventDto.setTitle( event.getTitle() );
        eventDto.setSlug( event.getSlug() );
        eventDto.setDescription( event.getDescription() );
        eventDto.setLocation( event.getLocation() );
        eventDto.setEventStart( event.getEventStart() );
        eventDto.setEventEnd( event.getEventEnd() );
        eventDto.setImageUrl( event.getImageUrl() );
        eventDto.setStatus( event.getStatus() );
        eventDto.setFeatured( event.isFeatured() );
        eventDto.setCreatedAt( event.getCreatedAt() );
        eventDto.setUpdatedAt( event.getUpdatedAt() );

        return eventDto;
    }

    @Override
    public FeaturedEventDto toFeaturedDto(Event event) {
        if ( event == null ) {
            return null;
        }

        FeaturedEventDto featuredEventDto = new FeaturedEventDto();

        featuredEventDto.setId( event.getId() );
        featuredEventDto.setTitle( event.getTitle() );
        featuredEventDto.setSlug( event.getSlug() );
        featuredEventDto.setImageUrl( event.getImageUrl() );
        featuredEventDto.setEventStart( event.getEventStart() );
        featuredEventDto.setLocation( event.getLocation() );

        return featuredEventDto;
    }

    @Override
    public Event toEntity(CreateEventRequest request) {
        if ( request == null ) {
            return null;
        }

        Event event = new Event();

        event.setTitle( request.getTitle() );
        event.setSlug( request.getSlug() );
        event.setDescription( request.getDescription() );
        event.setLocation( request.getLocation() );
        event.setEventStart( request.getEventStart() );
        event.setEventEnd( request.getEventEnd() );
        event.setImageUrl( request.getImageUrl() );
        event.setStatus( request.getStatus() );
        event.setFeatured( request.isFeatured() );

        return event;
    }

    @Override
    public void updateEntity(Event target, UpdateEventRequest source) {
        if ( source == null ) {
            return;
        }

        target.setTitle( source.getTitle() );
        target.setSlug( source.getSlug() );
        target.setDescription( source.getDescription() );
        target.setLocation( source.getLocation() );
        target.setEventStart( source.getEventStart() );
        target.setEventEnd( source.getEventEnd() );
        target.setImageUrl( source.getImageUrl() );
        target.setStatus( source.getStatus() );
        if ( source.getFeatured() != null ) {
            target.setFeatured( source.getFeatured() );
        }
    }
}
