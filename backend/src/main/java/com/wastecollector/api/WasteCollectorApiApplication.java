package com.wastecollector.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Waste Collector Ethiopia API.
 *
 * @SpringBootApplication combines three annotations:
 *
 *   @Configuration
 *     → This class can define Spring beans
 *
 *   @EnableAutoConfiguration
 *     → Spring Boot auto-configures beans based on the classpath.
 *       Sees PostgreSQL driver on classpath → sets up DataSource.
 *       Sees Spring Security → locks all endpoints by default.
 *       Sees Flyway → runs migrations on startup.
 *
 *   @ComponentScan
 *     → Scans com.wastecollector.api and all sub-packages for:
 *       @Component, @Service, @Repository, @Controller, @RestController
 *       Registers them as Spring beans (managed objects).
 *
 * @EnableScheduling
 *   → Activates the @Scheduled annotation in scheduler classes.
 *     Without this, your cron jobs are defined but never run.
 */
@SpringBootApplication
@EnableScheduling
public class WasteCollectorApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WasteCollectorApiApplication.class, args);
    }
}
