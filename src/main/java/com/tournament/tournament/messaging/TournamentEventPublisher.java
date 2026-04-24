package com.tournament.tournament.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TournamentEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishTournamentStarted(Long tournamentId, String tournamentName) {
        String message = String.format("{\"tournamentId\": %d, \"name\": \"%s\", \"event\": \"TOURNAMENT_STARTED\"}",
                tournamentId, tournamentName);
        rabbitTemplate.convertAndSend("tournament.exchange", "tournament.started", message);
        log.info("Evento TOURNAMENT_STARTED publicado para torneo {}", tournamentId);
    }

    public void publishTournamentFinished(Long tournamentId, Long championId) {
        String message = String.format("{\"tournamentId\": %d, \"championId\": %d, \"event\": \"TOURNAMENT_FINISHED\"}",
                tournamentId, championId);
        rabbitTemplate.convertAndSend("tournament.exchange", "tournament.finished", message);
        log.info("Evento TOURNAMENT_FINISHED publicado para torneo {}, campeón: {}", tournamentId, championId);
    }

    public void publishMatchFinished(Long matchId, Long winnerId, Long loserId) {
        String message = String.format(
                "{\"matchId\": %d, \"winnerId\": %d, \"loserId\": %d, \"event\": \"MATCH_FINISHED\"}",
                matchId, winnerId, loserId);
        rabbitTemplate.convertAndSend("tournament.exchange", "match.finished", message);
        log.info("Evento MATCH_FINISHED publicado — match: {}, winner: {}, loser: {}", matchId, winnerId, loserId);
    }
}