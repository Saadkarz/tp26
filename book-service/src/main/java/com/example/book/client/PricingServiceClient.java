package com.example.book.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client HTTP pour appeler le pricing-service
 * Utilise Resilience4j pour la résilience:
 * - Circuit Breaker: coupe les appels si trop d'échecs
 * - Retry: réessaie automatiquement en cas d'échec
 * - Fallback: retourne une valeur par défaut en cas d'échec
 */
@Component
public class PricingServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(PricingServiceClient.class);
    private static final Double FALLBACK_PRICE = 0.0;

    private final RestTemplate restTemplate;
    private final String pricingServiceUrl;

    public PricingServiceClient(
            RestTemplate restTemplate,
            @Value("${pricing.service.url:http://localhost:8081}") String pricingServiceUrl) {
        this.restTemplate = restTemplate;
        this.pricingServiceUrl = pricingServiceUrl;
        logger.info("PricingServiceClient initialized with URL: {}", pricingServiceUrl);
    }

    /**
     * Récupère le prix d'un livre depuis le pricing-service
     * Annotations Resilience4j:
     * - @Retry: réessaie 3 fois avant d'échouer
     * - @CircuitBreaker: ouvre le circuit après plusieurs échecs consécutifs
     * 
     * @param bookId ID du livre
     * @return prix du livre ou fallback en cas d'échec
     */
    @Retry(name = "pricing", fallbackMethod = "pricingFallback")
    @CircuitBreaker(name = "pricing", fallbackMethod = "pricingFallback")
    public Double getPrice(Long bookId) {
        String url = pricingServiceUrl + "/price/" + bookId;
        logger.info("Calling pricing-service: GET {}", url);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("price")) {
                Double price = ((Number) response.get("price")).doubleValue();
                logger.info("Received price {} from pricing-service for bookId={}", price, bookId);
                return price;
            }

            throw new RuntimeException("Invalid response from pricing-service");

        } catch (Exception e) {
            logger.error("Error calling pricing-service for bookId={}: {}", bookId, e.getMessage());
            throw e; // Laisse Resilience4j gérer le retry/fallback
        }
    }

    /**
     * Méthode de fallback appelée quand pricing-service est indisponible
     * Retourne un prix par défaut (0.0) pour permettre la continuation
     * 
     * @param bookId    ID du livre
     * @param throwable exception qui a causé le fallback
     * @return prix par défaut
     */
    public Double pricingFallback(Long bookId, Throwable throwable) {
        logger.warn("=== FALLBACK TRIGGERED ===");
        logger.warn("pricing-service unavailable for bookId={}, using fallback price={}",
                bookId, FALLBACK_PRICE);
        logger.warn("Cause: {} - {}",
                throwable.getClass().getSimpleName(),
                throwable.getMessage());
        logger.warn("==========================");

        return FALLBACK_PRICE;
    }
}
