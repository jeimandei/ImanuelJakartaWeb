package com.jeimandei.imanuelbytes.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Church CMS Service microservice.
 *
 * <p>Manages CMS pages, content blocks, announcements, news articles, and site
 * settings. Runs on port 8083.</p>
 */
@SpringBootApplication
public class ChurchCmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChurchCmsApplication.class, args);
    }
}
