package com.jeimandei.imanuelbytes.interaction.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import com.jeimandei.imanuelbytes.interaction.dto.NewsletterSubscriptionDto;
import com.jeimandei.imanuelbytes.interaction.dto.SubscribeNewsletterRequest;
import com.jeimandei.imanuelbytes.interaction.service.NewsletterService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private static final Logger log = LoggerFactory.getLogger(NewsletterController.class);

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<NewsletterSubscriptionDto>> subscribe(
            @Valid @RequestBody SubscribeNewsletterRequest request) {
        log.debug("Newsletter subscribe request for email='{}'", request.getEmail());
        NewsletterSubscriptionDto result = newsletterService.subscribe(request);
        log.info("Newsletter subscription processed: id={}, email='{}'", result.getId(), result.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscribed successfully", result));
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@RequestParam String email) {
        log.debug("Newsletter unsubscribe request for email='{}'", email);
        newsletterService.unsubscribe(email);
        return ResponseEntity.ok(ApiResponse.success("Unsubscribed successfully"));
    }

    @PostMapping("/request-unsubscribe")
    public ResponseEntity<ApiResponse<Void>> requestUnsubscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        log.debug("Unsubscribe confirmation request for email='{}'", email);
        newsletterService.requestUnsubscribeConfirmation(email);
        return ResponseEntity.ok(ApiResponse.success("Confirmation email sent"));
    }

    @GetMapping("/confirm-unsubscribe")
    public ResponseEntity<ApiResponse<Void>> confirmUnsubscribe(@RequestParam String token) {
        log.debug("Confirm unsubscribe with token");
        newsletterService.confirmUnsubscribe(token);
        return ResponseEntity.ok(ApiResponse.success("Unsubscribed successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriber(@PathVariable Long id) {
        log.debug("Admin delete subscriber id={}", id);
        newsletterService.deleteSubscriber(id);
        return ResponseEntity.ok(ApiResponse.success("Subscriber deleted successfully"));
    }

    @PostMapping("/notify-news")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Void>> notifyNews(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "");
        String excerpt = body.getOrDefault("excerpt", "");
        String articleUrl = body.getOrDefault("articleUrl", "");
        log.info("Sending news notification to subscribers: '{}'", title);
        newsletterService.sendNewsNotification(title, excerpt, articleUrl);
        return ResponseEntity.ok(ApiResponse.success("News notification sent"));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkSubscription(@RequestParam String email) {
        return ResponseEntity.ok(newsletterService.isSubscribed(email));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<NewsletterSubscriptionDto>>> getAllSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterSubscriptionDto> result = newsletterService.getAllSubscriptions(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }
}
