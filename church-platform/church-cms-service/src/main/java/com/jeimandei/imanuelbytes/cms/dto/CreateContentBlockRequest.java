package com.jeimandei.imanuelbytes.cms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a CMS content block.
 */
public class CreateContentBlockRequest {

    @NotNull(message = "Page ID is required")
    private Long pageId;

    @Size(max = 50, message = "Block type must not exceed 50 characters")
    private String blockType;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String body;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    private int displayOrder;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CreateContentBlockRequest() {
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
