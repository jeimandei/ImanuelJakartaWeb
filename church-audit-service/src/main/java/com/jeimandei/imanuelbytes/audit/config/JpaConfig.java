package com.jeimandei.imanuelbytes.audit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for the audit service.
 *
 * <p>Enables Spring Data JPA auditing so that {@code @CreatedDate} /
 * {@code @LastModifiedDate} annotations work if they are added to entities
 * in the future.  Current audit-log entities use {@code @PrePersist} directly.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
