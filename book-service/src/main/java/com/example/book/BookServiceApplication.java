package com.example.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Book Service Application - TP26
 * Microservice de gestion des livres avec résilience (Resilience4j)
 * 
 * Fonctionnalités:
 * - CRUD des livres (JPA/MySQL ou H2)
 * - Emprunt de livres avec appel au pricing-service
 * - Circuit Breaker + Retry + Fallback via Resilience4j
 * - Observabilité via Actuator
 * 
 * @author Karzouz Saad
 */
@SpringBootApplication
public class BookServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApplication.class, args);
    }
}
