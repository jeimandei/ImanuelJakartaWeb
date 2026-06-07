package com.jeimandei.imanuelbytes.media.mapper;

import com.jeimandei.imanuelbytes.media.dto.CreateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.media.dto.UpdateGalleryItemRequest;
import com.jeimandei.imanuelbytes.media.entity.GalleryItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T11:12:30+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class GalleryItemMapperImpl implements GalleryItemMapper {

    @Override
    public GalleryItemDto toDto(GalleryItem item) {
        if ( item == null ) {
            return null;
        }

        GalleryItemDto galleryItemDto = new GalleryItemDto();

        galleryItemDto.setId( item.getId() );
        galleryItemDto.setTitle( item.getTitle() );
        galleryItemDto.setDescription( item.getDescription() );
        galleryItemDto.setImageUrl( item.getImageUrl() );
        galleryItemDto.setAlbumName( item.getAlbumName() );
        galleryItemDto.setDisplayOrder( item.getDisplayOrder() );
        galleryItemDto.setActive( item.isActive() );
        galleryItemDto.setCreatedAt( item.getCreatedAt() );

        return galleryItemDto;
    }

    @Override
    public GalleryItem toEntity(CreateGalleryItemRequest request) {
        if ( request == null ) {
            return null;
        }

        GalleryItem galleryItem = new GalleryItem();

        galleryItem.setTitle( request.getTitle() );
        galleryItem.setDescription( request.getDescription() );
        galleryItem.setImageUrl( request.getImageUrl() );
        galleryItem.setAlbumName( request.getAlbumName() );
        galleryItem.setDisplayOrder( request.getDisplayOrder() );
        galleryItem.setActive( request.isActive() );

        return galleryItem;
    }

    @Override
    public void updateEntity(GalleryItem item, UpdateGalleryItemRequest request) {
        if ( request == null ) {
            return;
        }

        item.setTitle( request.getTitle() );
        item.setDescription( request.getDescription() );
        item.setImageUrl( request.getImageUrl() );
        item.setAlbumName( request.getAlbumName() );
        if ( request.getDisplayOrder() != null ) {
            item.setDisplayOrder( request.getDisplayOrder() );
        }
        if ( request.getActive() != null ) {
            item.setActive( request.getActive() );
        }
    }
}
