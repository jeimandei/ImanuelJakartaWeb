package com.jeimandei.imanuelbytes.cms.mapper;

import com.jeimandei.imanuelbytes.cms.dto.AnnouncementDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateAnnouncementRequest;
import com.jeimandei.imanuelbytes.cms.entity.Announcement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface AnnouncementMapper {

    AnnouncementDto toDto(Announcement announcement);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Announcement toEntity(CreateAnnouncementRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Announcement announcement, CreateAnnouncementRequest request);
}
