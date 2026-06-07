package com.jeimandei.imanuelbytes.cms.service;

import com.jeimandei.imanuelbytes.cms.dto.CmsPageDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateCmsPageRequest;
import com.jeimandei.imanuelbytes.cms.entity.CmsPage;
import com.jeimandei.imanuelbytes.cms.entity.ContentStatus;
import com.jeimandei.imanuelbytes.cms.mapper.CmsPageMapper;
import com.jeimandei.imanuelbytes.cms.repository.CmsPageRepository;
import com.jeimandei.imanuelbytes.cms.service.impl.CmsPageServiceImpl;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("CmsPageServiceImpl")
class CmsPageServiceImplTest {

    @Mock
    private CmsPageRepository cmsPageRepository;

    @Mock
    private CmsPageMapper cmsPageMapper;

    @InjectMocks
    private CmsPageServiceImpl cmsPageService;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private CmsPage buildPage(Long id, String title, String slug, ContentStatus status) {
        CmsPage page = new CmsPage();
        page.setId(id);
        page.setTitle(title);
        page.setSlug(slug);
        page.setStatus(status);
        page.setCreatedBy("admin");
        return page;
    }

    private CmsPageDto buildDto(Long id, String title, String slug, ContentStatus status) {
        CmsPageDto dto = new CmsPageDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setSlug(slug);
        dto.setStatus(status);
        dto.setCreatedBy("admin");
        return dto;
    }

    // ---------------------------------------------------------------------------
    // getPageById
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("getPageById")
    class GetPageById {

        @Test
        @DisplayName("returns DTO when page exists")
        void returnsDto_whenPageExists() {
            CmsPage page = buildPage(1L, "About Us", "about-us", ContentStatus.PUBLISHED);
            CmsPageDto expectedDto = buildDto(1L, "About Us", "about-us", ContentStatus.PUBLISHED);

            when(cmsPageRepository.findById(1L)).thenReturn(Optional.of(page));
            when(cmsPageMapper.toDto(page)).thenReturn(expectedDto);

            CmsPageDto result = cmsPageService.getPageById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("About Us", result.getTitle());
            assertEquals("about-us", result.getSlug());
            assertEquals(ContentStatus.PUBLISHED, result.getStatus());

            verify(cmsPageRepository).findById(1L);
            verify(cmsPageMapper).toDto(page);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when page does not exist")
        void throwsResourceNotFoundException_whenPageNotFound() {
            when(cmsPageRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> cmsPageService.getPageById(99L)
            );

            assertTrue(ex.getMessage().contains("99"));
            verify(cmsPageRepository).findById(99L);
            verifyNoInteractions(cmsPageMapper);
        }
    }

    // ---------------------------------------------------------------------------
    // createPage
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("createPage")
    class CreatePage {

        @Test
        @DisplayName("creates page with slug generated from title when request slug is blank")
        void createsPage_withSlugFromTitle_whenRequestSlugIsBlank() {
            CreateCmsPageRequest request = new CreateCmsPageRequest();
            request.setTitle("Welcome Home");
            request.setSlug(""); // blank – should fall back to title
            request.setStatus(ContentStatus.DRAFT);

            // The slug generated from "Welcome Home" by SlugUtils is "welcome-home"
            String expectedSlug = "welcome-home";

            CmsPage mappedEntity = buildPage(null, "Welcome Home", null, ContentStatus.DRAFT);
            CmsPage savedEntity = buildPage(1L, "Welcome Home", expectedSlug, ContentStatus.DRAFT);
            CmsPageDto expectedDto = buildDto(1L, "Welcome Home", expectedSlug, ContentStatus.DRAFT);

            when(cmsPageRepository.existsBySlug(expectedSlug)).thenReturn(false);
            when(cmsPageMapper.toEntity(request)).thenReturn(mappedEntity);
            when(cmsPageRepository.save(any(CmsPage.class))).thenReturn(savedEntity);
            when(cmsPageMapper.toDto(savedEntity)).thenReturn(expectedDto);

            CmsPageDto result = cmsPageService.createPage(request, "admin");

            assertNotNull(result);
            assertEquals(expectedSlug, result.getSlug());
            assertEquals(ContentStatus.DRAFT, result.getStatus());

            // Verify the slug was set on the entity before save
            ArgumentCaptor<CmsPage> pageCaptor = ArgumentCaptor.forClass(CmsPage.class);
            verify(cmsPageRepository).save(pageCaptor.capture());
            assertEquals(expectedSlug, pageCaptor.getValue().getSlug());
            assertEquals("admin", pageCaptor.getValue().getCreatedBy());

            verify(cmsPageRepository).existsBySlug(expectedSlug);
        }

        @Test
        @DisplayName("creates page using explicit slug from request when provided")
        void createsPage_usingExplicitSlug_whenProvided() {
            CreateCmsPageRequest request = new CreateCmsPageRequest();
            request.setTitle("Welcome Home");
            request.setSlug("My Custom Slug"); // will be slugified to "my-custom-slug"
            request.setStatus(ContentStatus.DRAFT);

            String expectedSlug = "my-custom-slug";

            CmsPage mappedEntity = buildPage(null, "Welcome Home", null, ContentStatus.DRAFT);
            CmsPage savedEntity = buildPage(2L, "Welcome Home", expectedSlug, ContentStatus.DRAFT);
            CmsPageDto expectedDto = buildDto(2L, "Welcome Home", expectedSlug, ContentStatus.DRAFT);

            when(cmsPageRepository.existsBySlug(expectedSlug)).thenReturn(false);
            when(cmsPageMapper.toEntity(request)).thenReturn(mappedEntity);
            when(cmsPageRepository.save(any(CmsPage.class))).thenReturn(savedEntity);
            when(cmsPageMapper.toDto(savedEntity)).thenReturn(expectedDto);

            CmsPageDto result = cmsPageService.createPage(request, "admin");

            assertNotNull(result);
            assertEquals(expectedSlug, result.getSlug());

            verify(cmsPageRepository).existsBySlug(expectedSlug);
        }

        @Test
        @DisplayName("throws ValidationException when slug already exists")
        void throwsValidationException_whenSlugAlreadyExists() {
            CreateCmsPageRequest request = new CreateCmsPageRequest();
            request.setTitle("About Page");
            request.setSlug("about-page");
            request.setStatus(ContentStatus.DRAFT);

            when(cmsPageRepository.existsBySlug("about-page")).thenReturn(true);

            ValidationException ex = assertThrows(
                    ValidationException.class,
                    () -> cmsPageService.createPage(request, "admin")
            );

            assertTrue(ex.getMessage().contains("about-page"));
            assertTrue(ex.getErrors().containsKey("slug"));

            verify(cmsPageRepository).existsBySlug("about-page");
            verify(cmsPageRepository, never()).save(any());
            verifyNoInteractions(cmsPageMapper);
        }

        @Test
        @DisplayName("sets status to DRAFT when mapper returns entity with null status")
        void setsStatusToDraft_whenEntityStatusIsNull() {
            CreateCmsPageRequest request = new CreateCmsPageRequest();
            request.setTitle("Draft Page");
            request.setSlug("draft-page");
            // do not set status so entity will have null status after mapping

            CmsPage mappedEntity = new CmsPage();
            mappedEntity.setTitle("Draft Page");
            mappedEntity.setStatus(null); // mapper returns null status

            CmsPage savedEntity = buildPage(3L, "Draft Page", "draft-page", ContentStatus.DRAFT);
            CmsPageDto expectedDto = buildDto(3L, "Draft Page", "draft-page", ContentStatus.DRAFT);

            when(cmsPageRepository.existsBySlug("draft-page")).thenReturn(false);
            when(cmsPageMapper.toEntity(request)).thenReturn(mappedEntity);
            when(cmsPageRepository.save(any(CmsPage.class))).thenReturn(savedEntity);
            when(cmsPageMapper.toDto(savedEntity)).thenReturn(expectedDto);

            cmsPageService.createPage(request, "editor");

            ArgumentCaptor<CmsPage> captor = ArgumentCaptor.forClass(CmsPage.class);
            verify(cmsPageRepository).save(captor.capture());
            assertEquals(ContentStatus.DRAFT, captor.getValue().getStatus());
        }
    }

    // ---------------------------------------------------------------------------
    // publishPage
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("publishPage")
    class PublishPage {

        @Test
        @DisplayName("sets status to PUBLISHED and sets publishedAt timestamp")
        void setsPublishedStatusAndPublishedAt() {
            CmsPage page = buildPage(1L, "Home", "home", ContentStatus.DRAFT);
            // publishedAt is initially null
            CmsPage savedPage = buildPage(1L, "Home", "home", ContentStatus.PUBLISHED);
            CmsPageDto expectedDto = buildDto(1L, "Home", "home", ContentStatus.PUBLISHED);
            expectedDto.setPublishedAt(LocalDateTime.now());

            when(cmsPageRepository.findById(1L)).thenReturn(Optional.of(page));
            when(cmsPageRepository.save(any(CmsPage.class))).thenReturn(savedPage);
            when(cmsPageMapper.toDto(savedPage)).thenReturn(expectedDto);

            CmsPageDto result = cmsPageService.publishPage(1L);

            assertNotNull(result);
            assertEquals(ContentStatus.PUBLISHED, result.getStatus());
            assertNotNull(result.getPublishedAt());

            ArgumentCaptor<CmsPage> captor = ArgumentCaptor.forClass(CmsPage.class);
            verify(cmsPageRepository).save(captor.capture());
            CmsPage captured = captor.getValue();
            assertEquals(ContentStatus.PUBLISHED, captured.getStatus());
            assertNotNull(captured.getPublishedAt());

            verify(cmsPageRepository).findById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when page to publish does not exist")
        void throwsResourceNotFoundException_whenPageNotFound() {
            when(cmsPageRepository.findById(42L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> cmsPageService.publishPage(42L)
            );

            verify(cmsPageRepository).findById(42L);
            verify(cmsPageRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // deletePage
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("deletePage")
    class DeletePage {

        @Test
        @DisplayName("deletes page successfully when it exists")
        void deletesPage_whenExists() {
            when(cmsPageRepository.existsById(1L)).thenReturn(true);
            doNothing().when(cmsPageRepository).deleteById(1L);

            assertDoesNotThrow(() -> cmsPageService.deletePage(1L));

            verify(cmsPageRepository).existsById(1L);
            verify(cmsPageRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when page to delete does not exist")
        void throwsResourceNotFoundException_whenPageNotFound() {
            when(cmsPageRepository.existsById(99L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> cmsPageService.deletePage(99L)
            );

            assertTrue(ex.getMessage().contains("99"));
            verify(cmsPageRepository).existsById(99L);
            verify(cmsPageRepository, never()).deleteById(any());
        }
    }
}
