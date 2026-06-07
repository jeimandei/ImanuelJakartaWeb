package com.jeimandei.imanuelbytes.cms.mapper;

import com.jeimandei.imanuelbytes.cms.dto.CmsPageDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateCmsPageRequest;
import com.jeimandei.imanuelbytes.cms.dto.UpdateCmsPageRequest;
import com.jeimandei.imanuelbytes.cms.entity.CmsPage;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T11:12:10+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class CmsPageMapperImpl implements CmsPageMapper {

    @Override
    public CmsPageDto toDto(CmsPage page) {
        if ( page == null ) {
            return null;
        }

        CmsPageDto cmsPageDto = new CmsPageDto();

        cmsPageDto.setId( page.getId() );
        cmsPageDto.setTitle( page.getTitle() );
        cmsPageDto.setSlug( page.getSlug() );
        cmsPageDto.setPageType( page.getPageType() );
        cmsPageDto.setContent( page.getContent() );
        cmsPageDto.setMetaTitle( page.getMetaTitle() );
        cmsPageDto.setMetaDescription( page.getMetaDescription() );
        cmsPageDto.setStatus( page.getStatus() );
        cmsPageDto.setPublishedAt( page.getPublishedAt() );
        cmsPageDto.setCreatedBy( page.getCreatedBy() );
        cmsPageDto.setCreatedAt( page.getCreatedAt() );
        cmsPageDto.setUpdatedAt( page.getUpdatedAt() );

        return cmsPageDto;
    }

    @Override
    public CmsPage toEntity(CreateCmsPageRequest request) {
        if ( request == null ) {
            return null;
        }

        CmsPage cmsPage = new CmsPage();

        cmsPage.setTitle( request.getTitle() );
        cmsPage.setSlug( request.getSlug() );
        cmsPage.setPageType( request.getPageType() );
        cmsPage.setContent( request.getContent() );
        cmsPage.setMetaTitle( request.getMetaTitle() );
        cmsPage.setMetaDescription( request.getMetaDescription() );
        cmsPage.setStatus( request.getStatus() );

        return cmsPage;
    }

    @Override
    public void updateEntity(CmsPage page, UpdateCmsPageRequest request) {
        if ( request == null ) {
            return;
        }

        page.setTitle( request.getTitle() );
        page.setSlug( request.getSlug() );
        page.setPageType( request.getPageType() );
        page.setContent( request.getContent() );
        page.setMetaTitle( request.getMetaTitle() );
        page.setMetaDescription( request.getMetaDescription() );
        page.setStatus( request.getStatus() );
    }
}
