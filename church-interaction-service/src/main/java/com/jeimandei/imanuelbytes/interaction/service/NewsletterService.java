package com.jeimandei.imanuelbytes.interaction.service;

import com.jeimandei.imanuelbytes.interaction.dto.NewsletterSubscriptionDto;
import com.jeimandei.imanuelbytes.interaction.dto.SubscribeNewsletterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsletterService {

    NewsletterSubscriptionDto subscribe(SubscribeNewsletterRequest request);

    void unsubscribe(String email);

    Page<NewsletterSubscriptionDto> getAllSubscriptions(Pageable pageable);
}
