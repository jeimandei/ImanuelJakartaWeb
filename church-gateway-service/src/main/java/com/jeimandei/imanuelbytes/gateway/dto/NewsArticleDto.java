package com.jeimandei.imanuelbytes.gateway.dto;

public class NewsArticleDto {

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String imageUrl;
    private Long authorId;
    private String status;
    private String publishedAt;
    private String createdAt;

    public NewsArticleDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
