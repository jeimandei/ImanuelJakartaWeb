package com.jeimandei.imanuelbytes.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Church User Service microservice.
 *
 * <p>Handles user CRUD operations and profile management, running on port 8082.
 * Shares the same database schema as church-auth-service for the users and roles tables.</p>
 */
@SpringBootApplication(scanBasePackages = "com.jeimandei.imanuelbytes")
public class ChurchUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChurchUserApplication.class, args);
    }
}
