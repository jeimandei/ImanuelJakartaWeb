package com.jeimandei.imanuelbytes.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Church Event microservice (port 8084).
 *
 * <p>Handles creation, publishing and querying of church events such as
 * services, concerts, retreats, and community gatherings.</p>
 */
@SpringBootApplication
public class ChurchEventApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChurchEventApplication.class, args);
    }
}
