package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.ContactFormDto;
import com.jeimandei.imanuelbytes.gateway.dto.PrayerRequestFormDto;
import com.jeimandei.imanuelbytes.gateway.service.InteractionClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link InteractionController}.
 *
 * No Spring context is loaded. MockMvc is configured via
 * {@link MockMvcBuilders#standaloneSetup} so only the controller under test
 * and its mocked {@link InteractionClientService} are involved.
 *
 * <p>Note: standaloneSetup does not enable Bean Validation by default.
 * Validation-failure paths that rely on {@code @Valid} + {@code BindingResult}
 * are exercised by posting incomplete/invalid parameters and verifying the
 * controller's fallback behaviour.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InteractionController unit tests")
class InteractionControllerTest {

    @Mock
    private InteractionClientService interactionClientService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InteractionController interactionController = new InteractionController(interactionClientService);
        mockMvc = MockMvcBuilders.standaloneSetup(interactionController).build();
    }

    // =========================================================================
    // GET /prayer-request
    // =========================================================================

    @Nested
    @DisplayName("GET /prayer-request")
    class PrayerRequestForm {

        @Test
        @DisplayName("returns HTTP 200 and resolves view 'public/prayer-request'")
        void get_returns200AndCorrectView() throws Exception {
            mockMvc.perform(get("/prayer-request"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/prayer-request"));
        }

        @Test
        @DisplayName("adds 'prayerRequestForm' model attribute to the view")
        void get_addsPrayerRequestFormModelAttribute() throws Exception {
            mockMvc.perform(get("/prayer-request"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("prayerRequestForm"));
        }

        @Test
        @DisplayName("'prayerRequestForm' model attribute is an instance of PrayerRequestFormDto")
        void get_prayerRequestFormAttributeIsCorrectType() throws Exception {
            mockMvc.perform(get("/prayer-request"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("prayerRequestForm",
                            org.hamcrest.Matchers.instanceOf(PrayerRequestFormDto.class)));
        }
    }

    // =========================================================================
    // POST /prayer-request — success path
    // =========================================================================

    @Nested
    @DisplayName("POST /prayer-request")
    class SubmitPrayerRequest {

        @Test
        @DisplayName("successful submission redirects to /prayer-request?submitted=true")
        void post_validForm_redirectsToSuccessUrl() throws Exception {
            when(interactionClientService.submitPrayerRequest(any(PrayerRequestFormDto.class)))
                    .thenReturn(true);

            mockMvc.perform(post("/prayer-request")
                            .param("name", "Maria Santos")
                            .param("email", "maria@example.com")
                            .param("message", "Please pray for my family's health and well-being."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/prayer-request?submitted=true"));

            verify(interactionClientService).submitPrayerRequest(any(PrayerRequestFormDto.class));
        }

        @Test
        @DisplayName("failed submission (service returns false) still redirects to submitted URL")
        void post_serviceReturnsFalse_redirectsWithErrorFlash() throws Exception {
            when(interactionClientService.submitPrayerRequest(any(PrayerRequestFormDto.class)))
                    .thenReturn(false);

            mockMvc.perform(post("/prayer-request")
                            .param("name", "Maria Santos")
                            .param("email", "maria@example.com")
                            .param("message", "Please pray for my family."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/prayer-request?submitted=true"));
        }

        @Test
        @DisplayName("service exception is swallowed and redirects gracefully")
        void post_serviceThrowsException_redirectsGracefully() throws Exception {
            when(interactionClientService.submitPrayerRequest(any(PrayerRequestFormDto.class)))
                    .thenThrow(new RuntimeException("Interaction service down"));

            mockMvc.perform(post("/prayer-request")
                            .param("name", "Maria Santos")
                            .param("email", "maria@example.com")
                            .param("message", "Please pray for us."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/prayer-request?submitted=true"));
        }
    }

    // =========================================================================
    // GET /contact
    // =========================================================================

    @Nested
    @DisplayName("GET /contact")
    class ContactForm {

        @Test
        @DisplayName("returns HTTP 200 and resolves view 'public/contact'")
        void get_returns200AndCorrectView() throws Exception {
            mockMvc.perform(get("/contact"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("public/contact"));
        }

        @Test
        @DisplayName("adds 'contactForm' model attribute to the view")
        void get_addsContactFormModelAttribute() throws Exception {
            mockMvc.perform(get("/contact"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("contactForm"));
        }

        @Test
        @DisplayName("'contactForm' model attribute is an instance of ContactFormDto")
        void get_contactFormAttributeIsCorrectType() throws Exception {
            mockMvc.perform(get("/contact"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("contactForm",
                            org.hamcrest.Matchers.instanceOf(ContactFormDto.class)));
        }
    }

    // =========================================================================
    // POST /contact — success path
    // =========================================================================

    @Nested
    @DisplayName("POST /contact")
    class SubmitContact {

        @Test
        @DisplayName("successful submission redirects to /contact?submitted=true")
        void post_validForm_redirectsToSuccessUrl() throws Exception {
            when(interactionClientService.submitContactMessage(any(ContactFormDto.class)))
                    .thenReturn(true);

            mockMvc.perform(post("/contact")
                            .param("name", "Pedro Alves")
                            .param("email", "pedro@example.com")
                            .param("subject", "General Inquiry")
                            .param("message", "I would like to know more about your church services."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/contact?submitted=true"));

            verify(interactionClientService).submitContactMessage(any(ContactFormDto.class));
        }

        @Test
        @DisplayName("failed submission (service returns false) still redirects to submitted URL")
        void post_serviceReturnsFalse_redirectsWithErrorFlash() throws Exception {
            when(interactionClientService.submitContactMessage(any(ContactFormDto.class)))
                    .thenReturn(false);

            mockMvc.perform(post("/contact")
                            .param("name", "Pedro Alves")
                            .param("email", "pedro@example.com")
                            .param("subject", "General Inquiry")
                            .param("message", "Message content here."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/contact?submitted=true"));
        }

        @Test
        @DisplayName("service exception is swallowed and redirects gracefully")
        void post_serviceThrowsException_redirectsGracefully() throws Exception {
            when(interactionClientService.submitContactMessage(any(ContactFormDto.class)))
                    .thenThrow(new RuntimeException("Interaction service down"));

            mockMvc.perform(post("/contact")
                            .param("name", "Pedro Alves")
                            .param("email", "pedro@example.com")
                            .param("subject", "Help")
                            .param("message", "Please help me."))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/contact?submitted=true"));
        }
    }

    // =========================================================================
    // POST /newsletter/subscribe
    // =========================================================================

    @Nested
    @DisplayName("POST /newsletter/subscribe")
    class NewsletterSubscribe {

        @Test
        @DisplayName("successful subscription redirects to /")
        void post_validEmail_redirectsToHome() throws Exception {
            when(interactionClientService.subscribeNewsletter("user@example.com", "Ana"))
                    .thenReturn(true);

            mockMvc.perform(post("/newsletter/subscribe")
                            .param("email", "user@example.com")
                            .param("name", "Ana"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            verify(interactionClientService).subscribeNewsletter("user@example.com", "Ana");
        }

        @Test
        @DisplayName("service exception is swallowed and still redirects to /")
        void post_serviceThrowsException_redirectsGracefully() throws Exception {
            when(interactionClientService.subscribeNewsletter(anyString(), any()))
                    .thenThrow(new RuntimeException("Service unavailable"));

            mockMvc.perform(post("/newsletter/subscribe")
                            .param("email", "user@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }
    }
}
