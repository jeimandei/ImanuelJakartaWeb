package com.jeimandei.imanuelbytes.cms.repository;

import com.jeimandei.imanuelbytes.cms.entity.CmsPage;
import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {

    Optional<CmsPage> findBySlug(String slug);

    Optional<CmsPage> findBySlugAndStatus(String slug, ContentStatus status);

    Page<CmsPage> findByStatus(ContentStatus status, Pageable pageable);

    boolean existsBySlug(String slug);

    @Query("SELECT p FROM CmsPage p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.slug) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<CmsPage> searchPages(@Param("query") String query, Pageable pageable);
}
