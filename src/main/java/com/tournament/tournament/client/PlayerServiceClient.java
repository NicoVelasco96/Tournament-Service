package com.tournament.tournament.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class PlayerServiceClient {

    @Autowired
    private WebClient playerWebClient;

    @Value("${service.token}")
    private String serviceToken;

    public boolean playerExists(Long playerId) {
        try {
            playerWebClient.get()
                    .uri("/api/players/" + playerId)
                    .header("Authorization", "Bearer " + serviceToken)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Player {} no encontrado en player-service: {}", playerId, e.getMessage());
            return false;
        }
    }

    public Object getPlayer(Long playerId) {
        try {
            return playerWebClient.get()
                    .uri("/api/players/" + playerId)
                    .header("Authorization", "Bearer " + serviceToken)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception e) {
            log.warn("Error obteniendo player {}: {}", playerId, e.getMessage());
            return null;
        }
    }
}