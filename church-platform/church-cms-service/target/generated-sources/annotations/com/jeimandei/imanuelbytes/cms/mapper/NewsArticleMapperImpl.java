package com.jeimandei.imanuelbytes.cms.mapper;

import com.jeimandei.imanuelbytes.cms.dto.CreateNewsArticleRequest;
import com.jeimandei.imanuelbytes.cms.dto.NewsArticleDto;
import com.jeimandei.imanuelbytes.cms.entity.NewsArticle;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T11:12:10+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class NewsArticleMapperImpl implements NewsArticleMapper {

    @Override
    public NewsArticleDto toDto(NewsArticle article) {
        if ( article == null ) {
            return null;
        }

        NewsArticleDto newsArticleDto = new NewsArticleDto();

        newsArticleDto.setId( article.getId() );
        newsArticleDto.setTitle( article.getTitle() );
        newsArticleDto.setSlug( article.getSlug() );
        newsArticleDto.setContent( article.getContent() );
        newsArticleDto.setExcerpt( article.getExcerpt() );
        newsArticleDto.setImageUrl( article.getImageUrl() );
        newsArticleDto.setAuthorId( article.getAuthorId() );
        newsArticleDto.setStatus( article.getStatus() );
        newsArticleDto.setPublishedAt( article.getPublishedAt() );
        newsArticleDto.setCreatedAt( article.getCreatedAt() );
        newsArticleDto.setUpdatedAt( article.getUpdatedAt() );

        return newsArticleDto;
    }

    @Override
    public NewsArticle toEntity(CreateNewsArticleRequest request) {
        if ( request == null ) {
            return null;
        }

        NewsArticle newsArticle = new NewsArticle();

        newsArticle.setTitle( request.getTitle() );
        newsArticle.setSlug( request.getSlug() );
        newsArticle.setContent( request.getContent() );
        newsArticle.setExcerpt( request.getExcerpt() );
        newsArticle.setImageUrl( request.getImageUrl() );
        newsArticle.setAuthorId( request.getAuthorId() );
        newsArticle.setStatus( request.getStatus() );

        return newsArticle;
    }

    @Override
    public void updateEntity(NewsArticle article, CreateNewsArticleRequest request) {
        if ( request == null ) {
            return;
        }

        article.setTitle( request.getTitle() );
        article.setSlug( request.getSlug() );
        article.setContent( request.getContent() );
        article.setExcerpt( request.getExcerpt() );
        article.setImageUrl( request.getImageUrl() );
        article.setAuthorId( request.getAuthorId() );
        article.setStatus( request.getStatus() );
    }
}
