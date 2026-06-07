package com.jeimandei.imanuelbytes.cms.dto;

import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing CMS page.
 *
 * <p>All fields are optional. Only non-null fields provided by the client
 * will be applied to the existing entity.</p>
 */
public class UpdateCmsPageRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 50, message = "Page type must not exceed 50 characters")
    private String pageType;

    private String content;

    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;

    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;

    private ContentStatus status;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UpdateCmsPageRequest() {
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
