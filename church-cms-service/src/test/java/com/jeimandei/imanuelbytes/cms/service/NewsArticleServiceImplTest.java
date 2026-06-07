package com.jeimandei.imanuelbytes.cms.service;

import com.jeimandei.imanuelbytes.cms.dto.CreateNewsArticleRequest;
import com.jeimandei.imanuelbytes.cms.dto.NewsArticleDto;
import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import com.jeimandei.imanuelbytes.cms.entity.NewsArticle;
import com.jeimandei.imanuelbytes.cms.mapper.NewsArticleMapper;
import com.jeimandei.imanuelbytes.cms.repository.NewsArticleRepository;
import com.jeimandei.imanuelbytes.cms.service.impl.NewsArticleServiceImpl;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsArticleServiceImpl")
class NewsArticleServiceImplTest {

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @Mock
    private NewsArticleMapper newsArticleMapper;

    @InjectMocks
    private NewsArticleServiceImpl newsArticleService;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private NewsArticle buildArticle(Long id, String title, String slug, ContentStatus status) {
        NewsArticle article = new NewsArticle();
        article.setId(id);
        article.setTitle(title);
        article.setSlug(slug);
        article.setStatus(status);
        article.setAuthorId(10L);
        article.setContent("Some content");
        return article;
    }

    private NewsArticleDto buildDto(Long id, String title, String slug, ContentStatus status) {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setSlug(slug);
        dto.setStatus(status);
        dto.setAuthorId(10L);
        return dto;
    }

    // ---------------------------------------------------------------------------
    // createArticle
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("createArticle")
    class CreateArticle {

        @Test
        @DisplayName("creates article with slug generated from title when request slug is blank")
        void createsArticle_withSlugFromTitle_whenRequestSlugIsBlank() {
            CreateNewsArticleRequest request = new CreateNewsArticleRequest();
            request.setTitle("Easter Sunday Sermon");
            request.setSlug(null); // null means generate from title
            request.setContent("This is the article content.");
            request.setStatus(ContentStatus.DRAFT);

            String expectedSlug = "easter-sunday-sermon";

            NewsArticle mappedEntity = buildArticle(null, "Easter Sunday Sermon", null, ContentStatus.DRAFT);
            NewsArticle savedEntity = buildArticle(1L, "Easter Sunday Sermon", expectedSlug, ContentStatus.DRAFT);
            NewsArticleDto expectedDto = buildDto(1L, "Easter Sunday Sermon", expectedSlug, ContentStatus.DRAFT);

            when(newsArticleRepository.existsBySlug(expectedSlug)).thenReturn(false);
            when(newsArticleMapper.toEntity(request)).thenReturn(mappedEntity);
            when(newsArticleRepository.save(any(NewsArticle.class))).thenReturn(savedEntity);
            when(newsArticleMapper.toDto(savedEntity)).thenReturn(expectedDto);

            NewsArticleDto result = newsArticleService.createArticle(request, 10L);

            assertNotNull(result);
            assertEquals(expectedSlug, result.getSlug());
            assertEquals(ContentStatus.DRAFT, result.getStatus());

            ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
            verify(newsArticleRepository).save(captor.capture());
            assertEquals(expectedSlug, captor.getValue().getSlug());
            assertEquals(10L, captor.getValue().getAuthorId());

            verify(newsArticleRepository).existsBySlug(expectedSlug);
        }

        @Test
        @DisplayName("creates article with explicit slug when provided in request")
        void createsArticle_withExplicitSlug_whenProvided() {
            CreateNewsArticleRequest request = new CreateNewsArticleRequest();
            request.setTitle("Easter Sunday Sermon");
            request.setSlug("Custom Slug Value");
            request.setContent("Content here.");
            request.setStatus(ContentStatus.DRAFT);

            String expectedSlug = "custom-slug-value";

            NewsArticle mappedEntity = buildArticle(null, "Easter Sunday Sermon", null, ContentStatus.DRAFT);
            NewsArticle savedEntity = buildArticle(2L, "Easter Sunday Sermon", expectedSlug, ContentStatus.DRAFT);
            NewsArticleDto expectedDto = buildDto(2L, "Easter Sunday Sermon", expectedSlug, ContentStatus.DRAFT);

            when(newsArticleRepository.existsBySlug(expectedSlug)).thenReturn(false);
            when(newsArticleMapper.toEntity(request)).thenReturn(mappedEntity);
            when(newsArticleRepository.save(any(NewsArticle.class))).thenReturn(savedEntity);
            when(newsArticleMapper.toDto(savedEntity)).thenReturn(expectedDto);

            NewsArticleDto result = newsArticleService.createArticle(request, 10L);

            assertNotNull(result);
            assertEquals(expectedSlug, result.getSlug());
            verify(newsArticleRepository).existsBySlug(expectedSlug);
        }

        @Test
        @DisplayName("throws ValidationException when slug is already taken")
        void throwsValidationException_whenSlugAlreadyExists() {
            CreateNewsArticleRequest request = new CreateNewsArticleRequest();
            request.setTitle("Easter Sunday Sermon");
            request.setSlug("easter-sunday-sermon");
            request.setContent("Content.");
            request.setStatus(ContentStatus.DRAFT);

            when(newsArticleRepository.existsBySlug("easter-sunday-sermon")).thenReturn(true);

            ValidationException ex = assertThrows(
                    ValidationException.class,
                    () -> newsArticleService.createArticle(request, 10L)
            );

            assertTrue(ex.getMessage().contains("easter-sunday-sermon"));
            assertTrue(ex.getErrors().containsKey("slug"));

            verify(newsArticleRepository).existsBySlug("easter-sunday-sermon");
            verify(newsArticleRepository, never()).save(any());
            verifyNoInteractions(newsArticleMapper);
        }

        @Test
        @DisplayName("sets status to DRAFT when mapped entity has null status")
        void setsStatusToDraft_whenEntityStatusIsNull() {
            CreateNewsArticleRequest request = new CreateNewsArticleRequest();
            request.setTitle("Null Status Article");
            request.setSlug("null-status-article");
            request.setContent("Body.");

            NewsArticle mappedEntity = new NewsArticle();
            mappedEntity.setTitle("Null Status Article");
            mappedEntity.setStatus(null);

            NewsArticle savedEntity = buildArticle(3L, "Null Status Article", "null-status-article", ContentStatus.DRAFT);
            NewsArticleDto expectedDto = buildDto(3L, "Null Status Article", "null-status-article", ContentStatus.DRAFT);

            when(newsArticleRepository.existsBySlug("null-status-article")).thenReturn(false);
            when(newsArticleMapper.toEntity(request)).thenReturn(mappedEntity);
            when(newsArticleRepository.save(any(NewsArticle.class))).thenReturn(savedEntity);
            when(newsArticleMapper.toDto(savedEntity)).thenReturn(expectedDto);

            newsArticleService.createArticle(request, 5L);

            ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
            verify(newsArticleRepository).save(captor.capture());
            assertEquals(ContentStatus.DRAFT, captor.getValue().getStatus());
        }
    }

    // ---------------------------------------------------------------------------
    // publishArticle
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("publishArticle")
    class PublishArticle {

        @Test
        @DisplayName("sets status to PUBLISHED and sets publishedAt timestamp")
        void setsPublishedStatusAndPublishedAt() {
            NewsArticle article = buildArticle(1L, "Breaking News", "breaking-news", ContentStatus.DRAFT);
            // publishedAt is initially null

            NewsArticle savedArticle = buildArticle(1L, "Breaking News", "breaking-news", ContentStatus.PUBLISHED);
            NewsArticleDto expectedDto = buildDto(1L, "Breaking News", "breaking-news", ContentStatus.PUBLISHED);
            expectedDto.setPublishedAt(LocalDateTime.now());

            when(newsArticleRepository.findById(1L)).thenReturn(Optional.of(article));
            when(newsArticleRepository.save(any(NewsArticle.class))).thenReturn(savedArticle);
            when(newsArticleMapper.toDto(savedArticle)).thenReturn(expectedDto);

            NewsArticleDto result = newsArticleService.publishArticle(1L);

            assertNotNull(result);
            assertEquals(ContentStatus.PUBLISHED, result.getStatus());
            assertNotNull(result.getPublishedAt());

            ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
            verify(newsArticleRepository).save(captor.capture());
            assertEquals(ContentStatus.PUBLISHED, captor.getValue().getStatus());
            assertNotNull(captor.getValue().getPublishedAt());

            verify(newsArticleRepository).findById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when article to publish does not exist")
        void throwsResourceNotFoundException_whenArticleNotFound() {
            when(newsArticleRepository.findById(55L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> newsArticleService.publishArticle(55L)
            );

            verify(newsArticleRepository).findById(55L);
            verify(newsArticleRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // deleteArticle
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteArticle")
    class DeleteArticle {

        @Test
        @DisplayName("deletes article successfully when it exists")
        void deletesArticle_whenExists() {
            when(newsArticleRepository.existsById(1L)).thenReturn(true);
            doNothing().when(newsArticleRepository).deleteById(1L);

            assertDoesNotThrow(() -> newsArticleService.deleteArticle(1L));

            verify(newsArticleRepository).existsById(1L);
            verify(newsArticleRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when article to delete does not exist")
        void throwsResourceNotFoundException_whenArticleNotFound() {
            when(newsArticleRepository.existsById(77L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> newsArticleService.deleteArticle(77L)
            );

            assertTrue(ex.getMessage().contains("77"));
            verify(newsArticleRepository).existsById(77L);
            verify(newsArticleRepository, never()).deleteById(any());
        }
    }
}
