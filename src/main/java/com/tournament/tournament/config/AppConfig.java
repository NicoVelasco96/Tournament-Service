package com.tournament.tournament.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${player.service.url}")
    private String playerServiceUrl;

    @Bean
    public WebClient playerWebClient() {
        return WebClient.builder()
                .baseUrl(playerServiceUrl)
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tournament Service API")
                        .version("1.0")
                        .description("Gestión de torneos y brackets"));
    }
}