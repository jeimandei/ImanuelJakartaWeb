package com.jeimandei.imanuelbytes.cms.service;

import com.jeimandei.imanuelbytes.cms.dto.CreateNewsArticleRequest;
import com.jeimandei.imanuelbytes.cms.dto.NewsArticleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsArticleService {
    Page<NewsArticleDto> getAllArticles(Pageable pageable);
    Page<NewsArticleDto> getPublishedArticles(Pageable pageable);
    NewsArticleDto getArticleById(Long id);
    NewsArticleDto getArticleBySlug(String slug);
    NewsArticleDto createArticle(CreateNewsArticleRequest request, Long authorId);
    NewsArticleDto updateArticle(Long id, CreateNewsArticleRequest request);
    NewsArticleDto publishArticle(Long id);
    void deleteArticle(Long id);
}
