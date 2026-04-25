package com.tournament.tournament.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class PaymentServiceClient {

    private final WebClient webClient;

    @Value("${SERVICE_TOKEN}")
    private String serviceToken;

    public PaymentServiceClient(@Value("${payment.service.url}") String paymentServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    public String getSubscriptionPlan(Long playerId) {
        try {
            return webClient.get()
                    .uri("/api/payments/subscriptions/{playerId}", playerId)
                    .header("Authorization", "Bearer " + serviceToken)
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .map(response -> response.get("plan").toString())
                    .block();
        } catch (Exception e) {
            log.warn("No se pudo obtener el plan del player {}: {}. Asumiendo FREE.", playerId, e.getMessage());
            return "FREE";
        }
    }
}