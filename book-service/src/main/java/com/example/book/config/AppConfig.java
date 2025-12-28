package com.example.book.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration de l'application
 * Définit les beans nécessaires comme RestTemplate
 */
@Configuration
public class AppConfig {

    /**
     * Bean RestTemplate pour les appels HTTP vers pricing-service
     * Configuré avec des timeouts raisonnables
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
