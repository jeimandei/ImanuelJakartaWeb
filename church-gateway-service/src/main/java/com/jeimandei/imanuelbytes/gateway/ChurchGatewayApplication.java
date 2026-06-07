package com.jeimandei.imanuelbytes.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ChurchGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChurchGatewayApplication.class, args);
    }
}
