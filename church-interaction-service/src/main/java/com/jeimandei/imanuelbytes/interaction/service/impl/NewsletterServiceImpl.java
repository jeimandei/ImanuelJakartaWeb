package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.audit.AuditClientService;
import com.jeimandei.imanuelbytes.interaction.dto.NewsletterSubscriptionDto;
import com.jeimandei.imanuelbytes.interaction.dto.SubscribeNewsletterRequest;
import com.jeimandei.imanuelbytes.interaction.entity.NewsletterSubscription;
import com.jeimandei.imanuelbytes.interaction.repository.NewsletterSubscriptionRepository;
import com.jeimandei.imanuelbytes.interaction.service.NewsletterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class NewsletterServiceImpl implements NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterServiceImpl.class);

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final AuditClientService auditClient;

    public NewsletterServiceImpl(NewsletterSubscriptionRepository newsletterSubscriptionRepository,
                                 AuditClientService auditClient) {
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
        this.auditClient = auditClient;
    }

    @Override
    public NewsletterSubscriptionDto subscribe(SubscribeNewsletterRequest request) {
        log.debug("Processing newsletter subscription for email='{}'", request.getEmail());
        Optional<NewsletterSubscription> existing = newsletterSubscriptionRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {
            NewsletterSubscription subscription = existing.get();
            if (subscription.isActive()) {
                // Already subscribed and active — return as-is
                log.debug("Email '{}' is already an active subscriber", request.getEmail());
                return toDto(subscription);
            }
            // Previously unsubscribed — reactivate
            log.info("Reactivating newsletter subscription for email='{}'", request.getEmail());
            subscription.setActive(true);
            if (request.getName() != null && !request.getName().isBlank()) {
                subscription.setName(request.getName());
            }
            NewsletterSubscription saved = newsletterSubscriptionRepository.save(subscription);
            try {
                auditClient.log(getCurrentActor(), getCurrentActorRole(), "SUBSCRIBE", "NewsletterSubscription",
                        String.valueOf(saved.getId()), saved.getEmail());
            } catch (Exception e) {
                log.warn("Audit log failed for SUBSCRIBE {}: {}", saved.getEmail(), e.getMessage());
            }
            return toDto(saved);
        }

        // New subscriber
        log.info("Creating new newsletter subscription for email='{}'", request.getEmail());
        NewsletterSubscription subscription = new NewsletterSubscription(
                request.getEmail(),
                request.getName()
        );
        NewsletterSubscription saved = newsletterSubscriptionRepository.save(subscription);
        log.info("Newsletter subscription created: id={}, email='{}'", saved.getId(), saved.getEmail());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "SUBSCRIBE", "NewsletterSubscription",
                    String.valueOf(saved.getId()), saved.getEmail());
        } catch (Exception e) {
            log.warn("Audit log failed for SUBSCRIBE {}: {}", saved.getEmail(), e.getMessage());
        }
        return toDto(saved);
    }

    @Override
    public void unsubscribe(String email) {
        log.debug("Processing newsletter unsubscribe for email='{}'", email);
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Newsletter subscription not found for email='{}'", email);
                    return new ResourceNotFoundException("NewsletterSubscription", "email", email);
                });
        subscription.setActive(false);
        NewsletterSubscription saved = newsletterSubscriptionRepository.save(subscription);
        log.info("Newsletter unsubscribed: email='{}'", email);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UNSUBSCRIBE", "NewsletterSubscription",
                    String.valueOf(saved.getId()), email);
        } catch (Exception e) {
            log.warn("Audit log failed for UNSUBSCRIBE {}: {}", email, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsletterSubscriptionDto> getAllSubscriptions(Pageable pageable) {
        return newsletterSubscriptionRepository.findAll(pageable)
                .map(this::toDto);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String getCurrentActor() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String getCurrentActorRole() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("UNKNOWN");
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private NewsletterSubscriptionDto toDto(NewsletterSubscription entity) {
        NewsletterSubscriptionDto dto = new NewsletterSubscriptionDto();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setActive(entity.isActive());
        dto.setSubscribedAt(entity.getSubscribedAt());
        return dto;
    }
}
