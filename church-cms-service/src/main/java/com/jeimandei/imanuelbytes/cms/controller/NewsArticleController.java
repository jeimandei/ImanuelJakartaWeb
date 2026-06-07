package com.jeimandei.imanuelbytes.cms.controller;

import com.jeimandei.imanuelbytes.cms.dto.CreateNewsArticleRequest;
import com.jeimandei.imanuelbytes.cms.dto.NewsArticleDto;
import com.jeimandei.imanuelbytes.cms.service.NewsArticleService;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    public NewsArticleController(NewsArticleService newsArticleService) {
        this.newsArticleService = newsArticleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NewsArticleDto>>> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(
                newsArticleService.getPublishedArticles(
                        PageRequest.of(page, size, Sort.by("publishedAt").descending())))));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<PageResponse<NewsArticleDto>>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(
                newsArticleService.getAllArticles(
                        PageRequest.of(page, size, Sort.by("createdAt").descending())))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsArticleDto>> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(newsArticleService.getArticleById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<NewsArticleDto>> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(newsArticleService.getArticleBySlug(slug)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<NewsArticleDto>> createArticle(
            @Valid @RequestBody CreateNewsArticleRequest request,
            @RequestParam(required = false) Long authorId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created", newsArticleService.createArticle(request, authorId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<NewsArticleDto>> updateArticle(
            @PathVariable Long id, @Valid @RequestBody CreateNewsArticleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Article updated", newsArticleService.updateArticle(id, request)));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<NewsArticleDto>> publishArticle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Article published", newsArticleService.publishArticle(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        newsArticleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted", null));
    }
}
