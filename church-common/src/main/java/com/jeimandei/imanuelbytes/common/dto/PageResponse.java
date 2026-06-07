package com.jeimandei.imanuelbytes.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response wrapper.  Microservices use this to return
 * Spring Data {@link Page} results in a serialisable, framework-agnostic form.
 *
 * @param <T> the type of individual items in the page
 */
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public PageResponse(List<T> content,
                        int page,
                        int size,
                        long totalElements,
                        int totalPages,
                        boolean first,
                        boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    /**
     * Convenience factory: build a {@code PageResponse} directly from a Spring Data
     * {@link Page} whose content has already been mapped to the target type {@code T}.
     *
     * @param springPage the Spring Data page (content already mapped to T)
     * @param <T>        element type
     * @return a new {@code PageResponse}
     */
    public static <T> PageResponse<T> from(Page<T> springPage) {
        return new PageResponse<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                springPage.isFirst(),
                springPage.isLast()
        );
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }
}
