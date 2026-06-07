package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
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

    public NewsletterServiceImpl(NewsletterSubscriptionRepository newsletterSubscriptionRepository) {
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
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
        newsletterSubscriptionRepository.save(subscription);
        log.info("Newsletter unsubscribed: email='{}'", email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsletterSubscriptionDto> getAllSubscriptions(Pageable pageable) {
        return newsletterSubscriptionRepository.findAll(pageable)
                .map(this::toDto);
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
