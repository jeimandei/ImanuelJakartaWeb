package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.audit.AuditClientService;
import com.jeimandei.imanuelbytes.interaction.dto.NewsletterSubscriptionDto;
import com.jeimandei.imanuelbytes.interaction.dto.SubscribeNewsletterRequest;
import com.jeimandei.imanuelbytes.interaction.entity.NewsletterSubscription;
import com.jeimandei.imanuelbytes.interaction.repository.NewsletterSubscriptionRepository;
import com.jeimandei.imanuelbytes.interaction.service.NewsletterService;
import com.jeimandei.imanuelbytes.interaction.token.UnsubscribeTokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class NewsletterServiceImpl implements NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterServiceImpl.class);

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final AuditClientService auditClient;
    private final JavaMailSender mailSender;
    private final UnsubscribeTokenStore tokenStore;

    @Value("${app.public-url:https://gmimimanueljakarta.or.id}")
    private String publicUrl;

    public NewsletterServiceImpl(NewsletterSubscriptionRepository newsletterSubscriptionRepository,
                                 AuditClientService auditClient,
                                 JavaMailSender mailSender,
                                 UnsubscribeTokenStore tokenStore) {
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
        this.auditClient = auditClient;
        this.mailSender = mailSender;
        this.tokenStore = tokenStore;
    }

    @Override
    public NewsletterSubscriptionDto subscribe(SubscribeNewsletterRequest request) {
        log.debug("Processing newsletter subscription for email='{}'", request.getEmail());
        Optional<NewsletterSubscription> existing = newsletterSubscriptionRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {
            NewsletterSubscription subscription = existing.get();
            if (subscription.isActive()) {
                log.debug("Email '{}' is already an active subscriber", request.getEmail());
                return toDto(subscription);
            }
            // Reactivate
            log.info("Reactivating newsletter subscription for email='{}'", request.getEmail());
            subscription.setActive(true);
            if (request.getName() != null && !request.getName().isBlank()) {
                subscription.setName(request.getName());
            }
            NewsletterSubscription saved = newsletterSubscriptionRepository.save(subscription);
            sendWelcomeEmail(saved.getEmail(), saved.getName());
            auditSafe("SUBSCRIBE", saved.getId(), saved.getEmail());
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
        sendWelcomeEmail(saved.getEmail(), saved.getName());
        auditSafe("SUBSCRIBE", saved.getId(), saved.getEmail());
        return toDto(saved);
    }

    @Override
    public void unsubscribe(String email) {
        log.debug("Processing newsletter unsubscribe for email='{}'", email);
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("NewsletterSubscription", "email", email));
        subscription.setActive(false);
        NewsletterSubscription saved = newsletterSubscriptionRepository.save(subscription);
        log.info("Newsletter unsubscribed: email='{}'", email);
        auditSafe("UNSUBSCRIBE", saved.getId(), email);
    }

    @Override
    public void requestUnsubscribeConfirmation(String email) {
        log.debug("Sending unsubscribe confirmation email to '{}'", email);
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findByEmail(email)
                .filter(NewsletterSubscription::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("NewsletterSubscription", "email", email));

        String token = UUID.randomUUID().toString();
        tokenStore.put(token, email);

        String confirmUrl = publicUrl + "/newsletter/confirm-unsubscribe?token=" + token;
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(subscription.getEmail());
            mail.setSubject("Confirm Newsletter Unsubscribe - GMIM Imanuel Jakarta");
            mail.setText(
                "Dear " + (subscription.getName() != null && !subscription.getName().isBlank()
                        ? subscription.getName() : "Subscriber") + ",\n\n" +
                "You requested to unsubscribe from the GMIM Imanuel Jakarta newsletter.\n\n" +
                "Please click the link below to confirm your unsubscription:\n\n" +
                "  " + confirmUrl + "\n\n" +
                "This link is valid for 24 hours. If you did not request this, please ignore this email.\n\n" +
                "GMIM Imanuel Jakarta"
            );
            mailSender.send(mail);
            log.info("Unsubscribe confirmation email sent to '{}'", email);
        } catch (Exception e) {
            tokenStore.remove(token);
            log.error("Failed to send unsubscribe confirmation email to '{}': {}", email, e.getMessage());
            throw new RuntimeException("Failed to send confirmation email: " + e.getMessage());
        }
    }

    @Override
    public void confirmUnsubscribe(String token) {
        String email = tokenStore.getEmail(token);
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired unsubscribe token.");
        }
        tokenStore.remove(token);
        newsletterSubscriptionRepository.findByEmail(email).ifPresent(sub -> {
            sub.setActive(false);
            newsletterSubscriptionRepository.save(sub);
            log.info("Newsletter unsubscribed via token confirmation: email='{}'", email);
            auditSafe("UNSUBSCRIBE", sub.getId(), email);
        });
    }

    @Override
    public void deleteSubscriber(Long id) {
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NewsletterSubscription", "id", id));
        newsletterSubscriptionRepository.delete(subscription);
        log.info("Newsletter subscriber hard-deleted: id={}, email='{}'", id, subscription.getEmail());
        auditSafe("DELETE_SUBSCRIBER", id, subscription.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsletterSubscriptionDto> getAllSubscriptions(Pageable pageable) {
        return newsletterSubscriptionRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubscribed(String email) {
        return newsletterSubscriptionRepository.findByEmail(email)
                .map(NewsletterSubscription::isActive)
                .orElse(false);
    }

    @Override
    public void sendNewsNotification(String title, String excerpt, String articleUrl) {
        List<NewsletterSubscription> subscribers = newsletterSubscriptionRepository.findByActive(true, Pageable.unpaged())
                .getContent();
        if (subscribers.isEmpty()) {
            log.info("No active subscribers to notify about news: '{}'", title);
            return;
        }
        log.info("Sending news notification to {} subscribers for article '{}'", subscribers.size(), title);
        for (NewsletterSubscription sub : subscribers) {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(sub.getEmail());
                mail.setSubject("New Article: " + title + " - GMIM Imanuel Jakarta");
                mail.setText(
                    "Dear " + (sub.getName() != null && !sub.getName().isBlank()
                            ? sub.getName() : "Subscriber") + ",\n\n" +
                    "A new article has been published on GMIM Imanuel Jakarta:\n\n" +
                    "  " + title + "\n\n" +
                    (excerpt != null && !excerpt.isBlank() ? excerpt + "\n\n" : "") +
                    "Read the full article here:\n  " + articleUrl + "\n\n" +
                    "To unsubscribe from our newsletter, visit your profile settings.\n\n" +
                    "GMIM Imanuel Jakarta"
                );
                mailSender.send(mail);
            } catch (Exception e) {
                log.error("Failed to send news notification to '{}': {}", sub.getEmail(), e.getMessage());
            }
        }
        log.info("News notification sent to {} subscribers", subscribers.size());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void sendWelcomeEmail(String email, String name) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("Welcome to GMIM Imanuel Jakarta Newsletter!");
            mail.setText(
                "Dear " + (name != null && !name.isBlank() ? name : "Subscriber") + ",\n\n" +
                "Thank you for subscribing to the GMIM Imanuel Jakarta newsletter!\n\n" +
                "You will receive updates about our latest news, events, and announcements.\n\n" +
                "God bless you!\n\n" +
                "GMIM Imanuel Jakarta"
            );
            mailSender.send(mail);
            log.info("Welcome email sent to '{}'", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to '{}': {}", email, e.getMessage());
        }
    }

    private void auditSafe(String action, Long id, String detail) {
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), action, "NewsletterSubscription",
                    String.valueOf(id), detail);
        } catch (Exception e) {
            log.warn("Audit log failed for {} {}: {}", action, detail, e.getMessage());
        }
    }

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
