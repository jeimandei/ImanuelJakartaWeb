package com.jeimandei.imanuelbytes.cms.service;

import com.jeimandei.imanuelbytes.cms.dto.CmsPageDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateCmsPageRequest;
import com.jeimandei.imanuelbytes.cms.dto.UpdateCmsPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CmsPageService {
    Page<CmsPageDto> getAllPages(Pageable pageable);
    Page<CmsPageDto> getPublishedPages(Pageable pageable);
    CmsPageDto getPageById(Long id);
    CmsPageDto getPageBySlug(String slug);
    CmsPageDto getPublishedPageBySlug(String slug);
    CmsPageDto createPage(CreateCmsPageRequest request, String createdBy);
    CmsPageDto updatePage(Long id, UpdateCmsPageRequest request);
    CmsPageDto publishPage(Long id);
    CmsPageDto unpublishPage(Long id);
    void deletePage(Long id);
    Page<CmsPageDto> searchPages(String query, Pageable pageable);
}
