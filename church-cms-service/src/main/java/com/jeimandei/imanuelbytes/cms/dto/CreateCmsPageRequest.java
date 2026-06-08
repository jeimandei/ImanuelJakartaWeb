package com.jeimandei.imanuelbytes.cms.dto;

import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new CMS page.
 */
public class CreateCmsPageRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 50, message = "Page type must not exceed 50 characters")
    private String pageType;

    private String content;

    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;

    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;

    @NotNull(message = "Status is required")
    private ContentStatus status;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CreateCmsPageRequest() {
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }
}
