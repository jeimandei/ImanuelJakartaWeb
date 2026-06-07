package com.jeimandei.imanuelbytes.media.mapper;

import com.jeimandei.imanuelbytes.media.dto.CreateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.dto.LivestreamDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateLivestreamRequest;
import com.jeimandei.imanuelbytes.media.entity.Livestream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LivestreamMapper {

    LivestreamDto toDto(Livestream livestream);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Livestream toEntity(CreateLivestreamRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Livestream livestream, UpdateLivestreamRequest request);
}
