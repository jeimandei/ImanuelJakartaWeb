package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
import com.jeimandei.imanuelbytes.gateway.service.EventClientService;
import com.jeimandei.imanuelbytes.gateway.service.LivestreamClientService;
import com.jeimandei.imanuelbytes.gateway.service.SermonClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link HomeController}.
 *
 * No Spring context is loaded. MockMvc is configured via
 * {@link MockMvcBuilders#standaloneSetup} so only the controller under test
 * and its mocked collaborators are involved.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HomeController unit tests")
class HomeControllerTest {

    @Mock
    private EventClientService eventClientService;

    @Mock
    private SermonClientService sermonClientService;

    @Mock
    private CmsClientService cmsClientService;

    @Mock
    private LivestreamClientService livestreamClientService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HomeController homeController = new HomeController(
                eventClientService,
                sermonClientService,
                cmsClientService,
                livestreamClientService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    // =========================================================================
    // GET /
    // =========================================================================

    @Nested
    @DisplayName("GET /")
    class Home {

        @Test
        @DisplayName("returns HTTP 200 and resolves view 'public/home'")
        void home_returns200AndHomeView() throws Exception {
            when(eventClientService.getFeaturedEvents()).thenReturn(Collections.emptyList());
            when(sermonClientService.getLatestSermons()).thenReturn(Collections.emptyList());
            when(cmsClientService.getActiveAnnouncements()).thenReturn(Collections.emptyList());
            when(livestreamClientService.getActiveLivestream()).thenReturn(Optional.empty());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/home"));
        }

        @Test
        @DisplayName("adds featuredEvents, latestSermons, announcements, activeLivestream model attributes")
        void home_populatesModelAttributes() throws Exception {
            when(eventClientService.getFeaturedEvents()).thenReturn(Collections.emptyList());
            when(sermonClientService.getLatestSermons()).thenReturn(Collections.emptyList());
            when(cmsClientService.getActiveAnnouncements()).thenReturn(Collections.emptyList());
            when(livestreamClientService.getActiveLivestream()).thenReturn(Optional.empty());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("featuredEvents"))
                    .andExpect(model().attributeExists("latestSermons"))
                    .andExpect(model().attributeExists("announcements"));
        }

        @Test
        @DisplayName("still returns 200 and home view when eventClientService throws an exception (graceful degradation)")
        void home_eventServiceThrows_stillRendersHomePage() throws Exception {
            when(eventClientService.getFeaturedEvents())
                    .thenThrow(new RuntimeException("Event service is down"));
            when(sermonClientService.getLatestSermons()).thenReturn(Collections.emptyList());
            when(cmsClientService.getActiveAnnouncements()).thenReturn(Collections.emptyList());
            when(livestreamClientService.getActiveLivestream()).thenReturn(Optional.empty());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/home"));
        }

        @Test
        @DisplayName("still returns 200 when sermonClientService throws an exception (graceful degradation)")
        void home_sermonServiceThrows_stillRendersHomePage() throws Exception {
            when(eventClientService.getFeaturedEvents()).thenReturn(Collections.emptyList());
            when(sermonClientService.getLatestSermons())
                    .thenThrow(new RuntimeException("Sermon service unavailable"));
            when(cmsClientService.getActiveAnnouncements()).thenReturn(Collections.emptyList());
            when(livestreamClientService.getActiveLivestream()).thenReturn(Optional.empty());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/home"));
        }

        @Test
        @DisplayName("still returns 200 when cmsClientService throws an exception (graceful degradation)")
        void home_cmsServiceThrows_stillRendersHomePage() throws Exception {
            when(eventClientService.getFeaturedEvents()).thenReturn(Collections.emptyList());
            when(sermonClientService.getLatestSermons()).thenReturn(Collections.emptyList());
            when(cmsClientService.getActiveAnnouncements())
                    .thenThrow(new RuntimeException("CMS service unavailable"));
            when(livestreamClientService.getActiveLivestream()).thenReturn(Optional.empty());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/home"));
        }

        @Test
        @DisplayName("still returns 200 when livestreamClientService throws an exception (graceful degradation)")
        void home_livestreamServiceThrows_stillRendersHomePage() throws Exception {
            when(eventClientService.getFeaturedEvents()).thenReturn(Collections.emptyList());
            when(sermonClientService.getLatestSermons()).thenReturn(Collections.emptyList());
            when(cmsClientService.getActiveAnnouncements()).thenReturn(Collections.emptyList());
            when(livestreamClientService.getActiveLivestream())
                    .thenThrow(new RuntimeException("Livestream service unavailable"));

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/home"));
        }

        @Test
        @DisplayName("still returns 200 when ALL backend services throw exceptions (complete degradation)")
        void home_allServicesThrow_stillRendersHomePage() throws Exception {
            when(eventClientService.getFeaturedEvents())
                    .thenThrow(new RuntimeException("Event service is down"));
            when(sermonClientService.getLatestSermons())
                    .thenThrow(new RuntimeException("Sermon service is down"));
            when(cmsClientService.getActiveAnnouncements())
                    .thenThrow(new RuntimeException("CMS is down"));
            when(livestreamClientService.getActiveLivestream())
                    .thenThrow(new RuntimeException("Livestream is down"));

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/home"));
        }
    }

    // =========================================================================
    // GET /about
    // =========================================================================

    @Nested
    @DisplayName("GET /about")
    class About {

        @Test
        @DisplayName("returns HTTP 200 and resolves view 'public/about'")
        void about_returns200AndAboutView() throws Exception {
            mockMvc.perform(get("/about"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/about"));
        }
    }

    // =========================================================================
    // GET /services
    // =========================================================================

    @Nested
    @DisplayName("GET /services")
    class Services {

        @Test
        @DisplayName("returns HTTP 200 and resolves view 'public/services'")
        void services_returns200() throws Exception {
            mockMvc.perform(get("/services"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/services"));
        }
    }

    // =========================================================================
    // GET /faq
    // =========================================================================

    @Nested
    @DisplayName("GET /faq")
    class Faq {

        @Test
        @DisplayName("returns HTTP 200 and resolves view 'public/faq'")
        void faq_returns200() throws Exception {
            mockMvc.perform(get("/faq"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/faq"));
        }
    }

    // =========================================================================
    // Other static pages (smoke tests)
    // =========================================================================

    @Nested
    @DisplayName("Other static pages")
    class OtherStaticPages {

        @Test
        @DisplayName("GET /ministries returns 200")
        void ministries_returns200() throws Exception {
            mockMvc.perform(get("/ministries"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/ministries"));
        }

        @Test
        @DisplayName("GET /giving returns 200")
        void giving_returns200() throws Exception {
            mockMvc.perform(get("/giving"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/giving"));
        }

        @Test
        @DisplayName("GET /leadership returns 200")
        void leadership_returns200() throws Exception {
            mockMvc.perform(get("/leadership"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/leadership"));
        }
    }
}
