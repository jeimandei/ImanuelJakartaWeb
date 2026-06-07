package com.jeimandei.imanuelbytes.media.mapper;

import com.jeimandei.imanuelbytes.media.dto.CreateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.dto.LivestreamDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.entity.Livestream;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T11:12:30+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class LivestreamMapperImpl implements LivestreamMapper {

    @Override
    public LivestreamDto toDto(Livestream livestream) {
        if ( livestream == null ) {
            return null;
        }

        LivestreamDto livestreamDto = new LivestreamDto();

        livestreamDto.setId( livestream.getId() );
        livestreamDto.setTitle( livestream.getTitle() );
        livestreamDto.setYoutubeEmbedUrl( livestream.getYoutubeEmbedUrl() );
        livestreamDto.setDescription( livestream.getDescription() );
        livestreamDto.setActive( livestream.isActive() );
        livestreamDto.setScheduledStart( livestream.getScheduledStart() );
        livestreamDto.setCreatedAt( livestream.getCreatedAt() );
        livestreamDto.setUpdatedAt( livestream.getUpdatedAt() );

        return livestreamDto;
    }

    @Override
    public Livestream toEntity(CreateLivestreamRequest request) {
        if ( request == null ) {
            return null;
        }

        Livestream livestream = new Livestream();

        livestream.setTitle( request.getTitle() );
        livestream.setYoutubeEmbedUrl( request.getYoutubeEmbedUrl() );
        livestream.setDescription( request.getDescription() );
        livestream.setActive( request.isActive() );
        livestream.setScheduledStart( request.getScheduledStart() );

        return livestream;
    }

    @Override
    public void updateEntity(Livestream livestream, UpdateLivestreamRequest request) {
        if ( request == null ) {
            return;
        }

        livestream.setTitle( request.getTitle() );
        livestream.setYoutubeEmbedUrl( request.getYoutubeEmbedUrl() );
        livestream.setDescription( request.getDescription() );
        if ( request.getActive() != null ) {
            livestream.setActive( request.getActive() );
        }
        livestream.setScheduledStart( request.getScheduledStart() );
    }
}
