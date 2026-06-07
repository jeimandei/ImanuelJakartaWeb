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

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    public NewsletterServiceImpl(NewsletterSubscriptionRepository newsletterSubscriptionRepository) {
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
    }

    @Override
    public NewsletterSubscriptionDto subscribe(SubscribeNewsletterRequest request) {
        Optional<NewsletterSubscription> existing = newsletterSubscriptionRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {
            NewsletterSubscription subscription = existing.get();
            if (subscription.isActive()) {
                // Already subscribed and active — return as-is
                return toDto(subscription);
            }
            // Previously unsubscribed — reactivate
            subscription.setActive(true);
            if (request.getName() != null && !request.getName().isBlank()) {
                subscription.setName(request.getName());
            }
            NewsletterSubscription saved = newsletterSubscriptionRepository.save(subscription);
            return toDto(saved);
        }

        // New subscriber
        NewsletterSubscription subscription = new NewsletterSubscription(
                request.getEmail(),
                request.getName()
        );
        NewsletterSubscription saved = newsletterSubscriptionRepository.save(subscription);
        return toDto(saved);
    }

    @Override
    public void unsubscribe(String email) {
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("NewsletterSubscription", "email", email));
        subscription.setActive(false);
        newsletterSubscriptionRepository.save(subscription);
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
