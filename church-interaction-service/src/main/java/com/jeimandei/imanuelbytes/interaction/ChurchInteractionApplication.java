package com.jeimandei.imanuelbytes.interaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Church Interaction Service.
 *
 * <p>Runs on port 8086 and is responsible for handling prayer requests,
 * contact messages, newsletter subscriptions, testimony submissions, and
 * volunteer applications.</p>
 */
@SpringBootApplication(scanBasePackages = "com.jeimandei.imanuelbytes")
public class ChurchInteractionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChurchInteractionApplication.class, args);
    }
}
