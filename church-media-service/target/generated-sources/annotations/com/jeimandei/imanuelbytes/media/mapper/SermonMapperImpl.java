package com.jeimandei.imanuelbytes.media.mapper;

import com.jeimandei.imanuelbytes.media.dto.CreateSermonRequest;
import com.jeimandei.imanuelbytes.media.dto.SermonDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateSermonRequest;
import com.jeimandei.imanuelbytes.media.entity.Sermon;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T11:12:30+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class SermonMapperImpl implements SermonMapper {

    @Override
    public SermonDto toDto(Sermon sermon) {
        if ( sermon == null ) {
            return null;
        }

        SermonDto sermonDto = new SermonDto();

        sermonDto.setId( sermon.getId() );
        sermonDto.setTitle( sermon.getTitle() );
        sermonDto.setSpeaker( sermon.getSpeaker() );
        sermonDto.setSermonDate( sermon.getSermonDate() );
        sermonDto.setDescription( sermon.getDescription() );
        sermonDto.setYoutubeUrl( sermon.getYoutubeUrl() );
        sermonDto.setAudioUrl( sermon.getAudioUrl() );
        sermonDto.setScriptureReference( sermon.getScriptureReference() );
        sermonDto.setSeriesName( sermon.getSeriesName() );
        sermonDto.setCreatedAt( sermon.getCreatedAt() );
        sermonDto.setUpdatedAt( sermon.getUpdatedAt() );

        return sermonDto;
    }

    @Override
    public Sermon toEntity(CreateSermonRequest request) {
        if ( request == null ) {
            return null;
        }

        Sermon sermon = new Sermon();

        sermon.setTitle( request.getTitle() );
        sermon.setSpeaker( request.getSpeaker() );
        sermon.setSermonDate( request.getSermonDate() );
        sermon.setDescription( request.getDescription() );
        sermon.setYoutubeUrl( request.getYoutubeUrl() );
        sermon.setAudioUrl( request.getAudioUrl() );
        sermon.setScriptureReference( request.getScriptureReference() );
        sermon.setSeriesName( request.getSeriesName() );

        return sermon;
    }

    @Override
    public void updateEntity(Sermon sermon, UpdateSermonRequest request) {
        if ( request == null ) {
            return;
        }

        sermon.setTitle( request.getTitle() );
        sermon.setSpeaker( request.getSpeaker() );
        sermon.setSermonDate( request.getSermonDate() );
        sermon.setDescription( request.getDescription() );
        sermon.setYoutubeUrl( request.getYoutubeUrl() );
        sermon.setAudioUrl( request.getAudioUrl() );
        sermon.setScriptureReference( request.getScriptureReference() );
        sermon.setSeriesName( request.getSeriesName() );
    }
}
