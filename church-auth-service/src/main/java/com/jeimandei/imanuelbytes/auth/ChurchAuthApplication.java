package com.jeimandei.imanuelbytes.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Church Authentication Service.
 *
 * <p>Runs on port 8081 and is responsible for user registration, login,
 * JWT issuance, and basic user-info retrieval.</p>
 */
@SpringBootApplication(scanBasePackages = "com.jeimandei.imanuelbytes")
public class ChurchAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChurchAuthApplication.class, args);
    }
}
