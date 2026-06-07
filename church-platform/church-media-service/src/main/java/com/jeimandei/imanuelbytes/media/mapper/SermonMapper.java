package com.jeimandei.imanuelbytes.media.mapper;

import com.jeimandei.imanuelbytes.media.dto.CreateSermonRequest;
import com.jeimandei.imanuelbytes.media.dto.SermonDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateSermonRequest;
import com.jeimandei.imanuelbytes.media.entity.Sermon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SermonMapper {

    SermonDto toDto(Sermon sermon);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Sermon toEntity(CreateSermonRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Sermon sermon, UpdateSermonRequest request);
}
