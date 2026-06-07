package com.jeimandei.imanuelbytes.cms.repository;

import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import com.jeimandei.imanuelbytes.cms.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    Optional<NewsArticle> findBySlug(String slug);

    Page<NewsArticle> findByStatus(ContentStatus status, Pageable pageable);

    boolean existsBySlug(String slug);
}
