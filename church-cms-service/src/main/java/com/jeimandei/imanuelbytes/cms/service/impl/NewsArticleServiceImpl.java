package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.audit.AuditClientService;
import com.jeimandei.imanuelbytes.cms.dto.CreateNewsArticleRequest;
import com.jeimandei.imanuelbytes.cms.dto.NewsArticleDto;
import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import com.jeimandei.imanuelbytes.cms.entity.NewsArticle;
import com.jeimandei.imanuelbytes.cms.mapper.NewsArticleMapper;
import com.jeimandei.imanuelbytes.cms.repository.NewsArticleRepository;
import com.jeimandei.imanuelbytes.cms.service.NewsArticleService;
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
public class NewsArticleServiceImpl implements NewsArticleService {

    private static final Logger log = LoggerFactory.getLogger(NewsArticleServiceImpl.class);

    private final NewsArticleRepository newsArticleRepository;
    private final NewsArticleMapper newsArticleMapper;
    private final AuditClientService auditClient;

    public NewsArticleServiceImpl(NewsArticleRepository newsArticleRepository,
                                   NewsArticleMapper newsArticleMapper,
                                   AuditClientService auditClient) {
        this.newsArticleRepository = newsArticleRepository;
        this.newsArticleMapper = newsArticleMapper;
        this.auditClient = auditClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsArticleDto> getAllArticles(Pageable pageable) {
        return newsArticleRepository.findAll(pageable).map(newsArticleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsArticleDto> getPublishedArticles(Pageable pageable) {
        return newsArticleRepository.findByStatus(ContentStatus.PUBLISHED, pageable)
                .map(newsArticleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public NewsArticleDto getArticleById(Long id) {
        return newsArticleRepository.findById(id)
                .map(newsArticleMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("NewsArticle", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public NewsArticleDto getArticleBySlug(String slug) {
        log.debug("Fetching news article by slug='{}'", slug);
        return newsArticleRepository.findBySlug(slug)
                .map(newsArticleMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("News article not found for slug='{}'", slug);
                    return new ResourceNotFoundException("NewsArticle", "slug", slug);
                });
    }

    @Override
    public NewsArticleDto createArticle(CreateNewsArticleRequest request, Long authorId) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? SlugUtils.toSlug(request.getSlug())
                : SlugUtils.toSlug(request.getTitle());
        log.debug("Creating news article with title='{}', slug='{}', authorId={}", request.getTitle(), slug, authorId);
        if (newsArticleRepository.existsBySlug(slug)) {
            log.warn("News article slug conflict: slug='{}' already exists", slug);
            throw new ValidationException("slug", "An article with this slug already exists: " + slug, true);
        }
        NewsArticle article = newsArticleMapper.toEntity(request);
        article.setSlug(slug);
        article.setAuthorId(authorId);
        if (article.getStatus() == null) {
            article.setStatus(ContentStatus.DRAFT);
        }
        NewsArticleDto created = newsArticleMapper.toDto(newsArticleRepository.save(article));
        log.info("Created news article id={}, slug='{}'", created.getId(), created.getSlug());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_ARTICLE", "NewsArticle",
                    String.valueOf(created.getId()), created.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for CREATE_ARTICLE {}: {}", created.getId(), e.getMessage());
        }
        return created;
    }

    @Override
    public NewsArticleDto updateArticle(Long id, CreateNewsArticleRequest request) {
        log.debug("Updating news article id={}", id);
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("News article not found for id={}", id);
                    return new ResourceNotFoundException("NewsArticle", "id", id);
                });
        newsArticleMapper.updateEntity(article, request);
        NewsArticleDto updated = newsArticleMapper.toDto(newsArticleRepository.save(article));
        log.info("Updated news article id={}", updated.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_ARTICLE", "NewsArticle",
                    String.valueOf(updated.getId()), updated.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_ARTICLE {}: {}", id, e.getMessage());
        }
        return updated;
    }

    @Override
    public NewsArticleDto publishArticle(Long id) {
        log.debug("Publishing news article id={}", id);
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("News article not found for id={}", id);
                    return new ResourceNotFoundException("NewsArticle", "id", id);
                });
        article.setStatus(ContentStatus.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());
        NewsArticleDto published = newsArticleMapper.toDto(newsArticleRepository.save(article));
        log.info("Published news article id={}, slug='{}'", published.getId(), published.getSlug());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "PUBLISH_ARTICLE", "NewsArticle",
                    String.valueOf(published.getId()), published.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for PUBLISH_ARTICLE {}: {}", id, e.getMessage());
        }
        return published;
    }

    @Override
    public void deleteArticle(Long id) {
        log.debug("Deleting news article id={}", id);
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("News article not found for id={}", id);
                    return new ResourceNotFoundException("NewsArticle", "id", id);
                });
        newsArticleRepository.deleteById(id);
        log.info("Deleted news article id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "DELETE_ARTICLE", "NewsArticle",
                    String.valueOf(id), article.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for DELETE_ARTICLE {}: {}", id, e.getMessage());
        }
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
