package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.audit.AuditClientService;
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
    private final AuditClientService auditClient;

    public CmsPageServiceImpl(CmsPageRepository cmsPageRepository, CmsPageMapper cmsPageMapper,
                              AuditClientService auditClient) {
        this.cmsPageRepository = cmsPageRepository;
        this.cmsPageMapper = cmsPageMapper;
        this.auditClient = auditClient;
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
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_PAGE", "CmsPage",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for CREATE_PAGE {}: {}", saved.getId(), e.getMessage());
        }
        return cmsPageMapper.toDto(saved);
    }

    @Override
    public CmsPageDto updatePage(Long id, UpdateCmsPageRequest request) {
        CmsPage page = cmsPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "id", id));
        cmsPageMapper.updateEntity(page, request);
        CmsPage saved = cmsPageRepository.save(page);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_PAGE", "CmsPage",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_PAGE {}: {}", id, e.getMessage());
        }
        return cmsPageMapper.toDto(saved);
    }

    @Override
    public CmsPageDto publishPage(Long id) {
        CmsPage page = cmsPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "id", id));
        page.setStatus(ContentStatus.PUBLISHED);
        page.setPublishedAt(LocalDateTime.now());
        CmsPage saved = cmsPageRepository.save(page);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "PUBLISH_PAGE", "CmsPage",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for PUBLISH_PAGE {}: {}", id, e.getMessage());
        }
        return cmsPageMapper.toDto(saved);
    }

    @Override
    public CmsPageDto unpublishPage(Long id) {
        CmsPage page = cmsPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "id", id));
        page.setStatus(ContentStatus.DRAFT);
        CmsPage saved = cmsPageRepository.save(page);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UNPUBLISH_PAGE", "CmsPage",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for UNPUBLISH_PAGE {}: {}", id, e.getMessage());
        }
        return cmsPageMapper.toDto(saved);
    }

    @Override
    public void deletePage(Long id) {
        CmsPage page = cmsPageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CmsPage", "id", id));
        cmsPageRepository.deleteById(id);
        log.info("CMS page deleted: id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "DELETE_PAGE", "CmsPage",
                    String.valueOf(id), page.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for DELETE_PAGE {}: {}", id, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CmsPageDto> searchPages(String query, Pageable pageable) {
        return cmsPageRepository.searchPages(query, pageable).map(cmsPageMapper::toDto);
    }

    private String getCurrentActor() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String getCurrentActorRole() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("UNKNOWN");
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }
}
