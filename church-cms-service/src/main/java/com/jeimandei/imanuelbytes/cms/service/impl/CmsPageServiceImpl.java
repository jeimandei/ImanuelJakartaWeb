package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.dto.CmsPageDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateCmsPageRequest;
import com.jeimandei.imanuelbytes.cms.dto.UpdateCmsPageRequest;
import com.jeimandei.imanuelbytes.cms.entity.CmsPage;
import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import com.jeimandei.imanuelbytes.cms.mapper.CmsPageMapper;
import com.jeimandei.imanuelbytes.cms.repository.CmsPageRepository;
import com.jeimandei.imanuelbytes.cms.service.CmsPageService;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.common.util.SlugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CmsPageServiceImpl implements CmsPageService {

    private static final Logger log = LoggerFactory.getLogger(CmsPageServiceImpl.class);

    private final CmsPageRepository cmsPageRepository;
    private final CmsPageMapper cmsPageMapper;

    public CmsPageServiceImpl(CmsPageRepository cmsPageRepository, CmsPageMapper cmsPageMapper) {
        this.cmsPageRepository = cmsPageRepository;
        this.cmsPageMapper = cmsPageMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsPageDto> getAllPages(Pageable pageable) {
        return cmsPageRepository.findAll(pageable).map(cmsPageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsPageDto> getPublishedPages(Pageable pageable) {
        return cmsPageRepository.findByStatus(ContentStatus.PUBLISHED, pageable).map(cmsPageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CmsPageDto getPageById(Long id) {
        return cmsPageRepository.findById(id)
                .map(cmsPageMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public CmsPageDto getPageBySlug(String slug) {
        return cmsPageRepository.findBySlug(slug)
                .map(cmsPageMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "slug", slug));
    }

    @Override
    @Transactional(readOnly = true)
    public CmsPageDto getPublishedPageBySlug(String slug) {
        return cmsPageRepository.findBySlugAndStatus(slug, ContentStatus.PUBLISHED)
                .map(cmsPageMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "slug", slug));
    }

    @Override
    public CmsPageDto createPage(CreateCmsPageRequest request, String createdBy) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? SlugUtils.toSlug(request.getSlug())
                : SlugUtils.toSlug(request.getTitle());
        if (cmsPageRepository.existsBySlug(slug)) {
            throw new ValidationException("slug", "A page with this slug already exists: " + slug, true);
        }
        CmsPage page = cmsPageMapper.toEntity(request);
        page.setSlug(slug);
        page.setCreatedBy(createdBy);
        if (page.getStatus() == null) {
            page.setStatus(ContentStatus.DRAFT);
        }
        CmsPage saved = cmsPageRepository.save(page);
        log.info("CMS page created: {} by {}", saved.getSlug(), createdBy);
        return cmsPageMapper.toDto(saved);
    }

    @Override
    public CmsPageDto updatePage(Long id, UpdateCmsPageRequest request) {
        CmsPage page = cmsPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "id", id));
        cmsPageMapper.updateEntity(page, request);
        return cmsPageMapper.toDto(cmsPageRepository.save(page));
    }

    @Override
    public CmsPageDto publishPage(Long id) {
        CmsPage page = cmsPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "id", id));
        page.setStatus(ContentStatus.PUBLISHED);
        page.setPublishedAt(LocalDateTime.now());
        return cmsPageMapper.toDto(cmsPageRepository.save(page));
    }

    @Override
    public CmsPageDto unpublishPage(Long id) {
        CmsPage page = cmsPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "id", id));
        page.setStatus(ContentStatus.DRAFT);
        return cmsPageMapper.toDto(cmsPageRepository.save(page));
    }

    @Override
    public void deletePage(Long id) {
        if (!cmsPageRepository.existsById(id)) {
            throw new ResourceNotFoundException("CmsPage", "id", id);
        }
        cmsPageRepository.deleteById(id);
        log.info("CMS page deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsPageDto> searchPages(String query, Pageable pageable) {
        return cmsPageRepository.searchPages(query, pageable).map(cmsPageMapper::toDto);
    }
}
