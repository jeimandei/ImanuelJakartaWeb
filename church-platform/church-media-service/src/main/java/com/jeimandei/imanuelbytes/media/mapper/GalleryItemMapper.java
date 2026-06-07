package com.jeimandei.imanuelbytes.media.mapper;

import com.jeimandei.imanuelbytes.media.dto.CreateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.entity.GalleryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GalleryItemMapper {

    GalleryItemDto toDto(GalleryItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    GalleryItem toEntity(CreateGalleryItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget GalleryItem item, UpdateGalleryItemRequest request);
}
