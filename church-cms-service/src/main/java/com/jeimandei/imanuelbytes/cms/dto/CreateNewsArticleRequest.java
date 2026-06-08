package com.jeimandei.imanuelbytes.cms.dto;

import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateNewsArticleRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    private String slug;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500)
    private String excerpt;

    private String imageUrl;
    private Long authorId;
    private ContentStatus status = ContentStatus.DRAFT;

    public CreateNewsArticleRequest() {}

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
    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }
}
