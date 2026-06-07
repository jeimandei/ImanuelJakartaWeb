package com.jeimandei.imanuelbytes.interaction.repository;

import com.jeimandei.imanuelbytes.interaction.entity.NewsletterSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {

    Optional<NewsletterSubscription> findByEmail(String email);

    Page<NewsletterSubscription> findByActive(boolean active, Pageable pageable);

    boolean existsByEmail(String email);
}
