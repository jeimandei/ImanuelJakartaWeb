package com.jeimandei.imanuelbytes.gateway.dto;

import java.time.LocalDateTime;

public class CmsPageDto {

    private Long id;
    private String title;
    private String slug;
    private String pageType;
    private String content;
    private String metaTitle;
    private String metaDescription;
    private String status;
    private LocalDateTime publishedAt;

    public CmsPageDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getPageType() { return pageType; }
    public void setPageType(String pageType) { this.pageType = pageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMetaTitle() { return metaTitle; }
    public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }

    public String getMetaDescription() { return metaDescription; }
    public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
