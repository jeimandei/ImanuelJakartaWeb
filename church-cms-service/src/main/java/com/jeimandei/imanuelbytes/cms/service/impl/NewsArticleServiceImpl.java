package com.jeimandei.imanuelbytes.cms.service.impl;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class NewsArticleServiceImpl implements NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final NewsArticleMapper newsArticleMapper;

    public NewsArticleServiceImpl(NewsArticleRepository newsArticleRepository,
                                   NewsArticleMapper newsArticleMapper) {
        this.newsArticleRepository = newsArticleRepository;
        this.newsArticleMapper = newsArticleMapper;
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
        return newsArticleRepository.findBySlug(slug)
                .map(newsArticleMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("NewsArticle", "slug", slug));
    }

    @Override
    public NewsArticleDto createArticle(CreateNewsArticleRequest request, Long authorId) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? SlugUtils.toSlug(request.getSlug())
                : SlugUtils.toSlug(request.getTitle());
        if (newsArticleRepository.existsBySlug(slug)) {
            throw new ValidationException("slug", "An article with this slug already exists: " + slug, true);
        }
        NewsArticle article = newsArticleMapper.toEntity(request);
        article.setSlug(slug);
        article.setAuthorId(authorId);
        if (article.getStatus() == null) {
            article.setStatus(ContentStatus.DRAFT);
        }
        return newsArticleMapper.toDto(newsArticleRepository.save(article));
    }

    @Override
    public NewsArticleDto updateArticle(Long id, CreateNewsArticleRequest request) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NewsArticle", "id", id));
        newsArticleMapper.updateEntity(article, request);
        return newsArticleMapper.toDto(newsArticleRepository.save(article));
    }

    @Override
    public NewsArticleDto publishArticle(Long id) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NewsArticle", "id", id));
        article.setStatus(ContentStatus.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());
        return newsArticleMapper.toDto(newsArticleRepository.save(article));
    }

    @Override
    public void deleteArticle(Long id) {
        if (!newsArticleRepository.existsById(id)) {
            throw new ResourceNotFoundException("NewsArticle", "id", id);
        }
        newsArticleRepository.deleteById(id);
    }
}
