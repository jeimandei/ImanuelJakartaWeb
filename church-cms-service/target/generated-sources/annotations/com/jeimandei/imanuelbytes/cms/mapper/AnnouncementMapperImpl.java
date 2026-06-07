package com.jeimandei.imanuelbytes.cms.mapper;

import com.jeimandei.imanuelbytes.cms.dto.AnnouncementDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateAnnouncementRequest;
import com.jeimandei.imanuelbytes.cms.entity.Announcement;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T11:12:10+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class AnnouncementMapperImpl implements AnnouncementMapper {

    @Override
    public AnnouncementDto toDto(Announcement announcement) {
        if ( announcement == null ) {
            return null;
        }

        AnnouncementDto announcementDto = new AnnouncementDto();

        announcementDto.setId( announcement.getId() );
        announcementDto.setTitle( announcement.getTitle() );
        announcementDto.setMessage( announcement.getMessage() );
        announcementDto.setStartDate( announcement.getStartDate() );
        announcementDto.setEndDate( announcement.getEndDate() );
        announcementDto.setActive( announcement.isActive() );
        announcementDto.setPriority( announcement.getPriority() );
        announcementDto.setCreatedAt( announcement.getCreatedAt() );
        announcementDto.setUpdatedAt( announcement.getUpdatedAt() );

        return announcementDto;
    }

    @Override
    public Announcement toEntity(CreateAnnouncementRequest request) {
        if ( request == null ) {
            return null;
        }

        Announcement announcement = new Announcement();

        announcement.setTitle( request.getTitle() );
        announcement.setMessage( request.getMessage() );
        announcement.setStartDate( request.getStartDate() );
        announcement.setEndDate( request.getEndDate() );
        announcement.setActive( request.isActive() );
        announcement.setPriority( request.getPriority() );

        return announcement;
    }

    @Override
    public void updateEntity(Announcement announcement, CreateAnnouncementRequest request) {
        if ( request == null ) {
            return;
        }

        announcement.setTitle( request.getTitle() );
        announcement.setMessage( request.getMessage() );
        announcement.setStartDate( request.getStartDate() );
        announcement.setEndDate( request.getEndDate() );
        announcement.setActive( request.isActive() );
        announcement.setPriority( request.getPriority() );
    }
}
