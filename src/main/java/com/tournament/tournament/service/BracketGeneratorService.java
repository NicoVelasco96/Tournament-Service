package com.tournament.tournament.service;

import com.tournament.tournament.model.Match;
import com.tournament.tournament.model.MatchStatus;
import com.tournament.tournament.model.Tournament;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class BracketGeneratorService implements IBracketGeneratorService {

    @Override
    public List<Match> generateBracket(Tournament tournament) {
        List<Long> playerIds = new ArrayList<>(tournament.getPlayerIds());

        if (playerIds.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 jugadores para generar el bracket");
        }

        int bracketSize = nextPowerOfTwo(playerIds.size());
        while (playerIds.size() < bracketSize) {
            playerIds.add(null);
        }

        Collections.shuffle(playerIds);

        List<Match> matches = new ArrayList<>();
        int matchNumber = 1;

        for (int i = 0; i < playerIds.size(); i += 2) {
            Long player1 = playerIds.get(i);
            Long player2 = playerIds.get(i + 1);

            Match match = Match.builder()
                    .tournament(tournament)
                    .round(1)
                    .matchNumber(matchNumber++)
                    .player1Id(player1)
                    .player2Id(player2)
                    .build();

            if (player1 == null || player2 == null) {
                match.setWinnerId(player1 != null ? player1 : player2);
                match.setStatus(MatchStatus.FINISHED);
            }

            matches.add(match);
        }

        log.info("Bracket generado para torneo {}: {} partidos en ronda 1",
                tournament.getId(), matches.size());

        return matches;
    }

    @Override
    public List<Match> generateNextRound(Tournament tournament, int completedRound) {
        List<Match> completedMatches = tournament.getMatches().stream()
                .filter(m -> m.getRound() == completedRound
                        && m.getStatus() == MatchStatus.FINISHED)
                .sorted((a, b) -> a.getMatchNumber().compareTo(b.getMatchNumber()))
                .toList();

        List<Long> winners = completedMatches.stream()
                .map(Match::getWinnerId)
                .toList();

        if (winners.size() == 1) {
            log.info("Torneo {} finalizado. Ganador: {}", tournament.getId(), winners.get(0));
            return List.of();
        }

        List<Match> nextRoundMatches = new ArrayList<>();
        int matchNumber = 1;
        int nextRound = completedRound + 1;

        for (int i = 0; i < winners.size(); i += 2) {
            Match match = Match.builder()
                    .tournament(tournament)
                    .round(nextRound)
                    .matchNumber(matchNumber++)
                    .player1Id(winners.get(i))
                    .player2Id(i + 1 < winners.size() ? winners.get(i + 1) : null)
                    .build();

            if (match.getPlayer2Id() == null) {
                match.setWinnerId(match.getPlayer1Id());
                match.setStatus(MatchStatus.FINISHED);
            }

            nextRoundMatches.add(match);
        }

        log.info("Ronda {} generada para torneo {}: {} partidos",
                nextRound, tournament.getId(), nextRoundMatches.size());

        return nextRoundMatches;
    }

    @Override
    public boolean isRoundComplete(Tournament tournament, int round) {
        return tournament.getMatches().stream()
                .filter(m -> m.getRound() == round)
                .allMatch(m -> m.getStatus() == MatchStatus.FINISHED);
    }

    @Override
    public Long getChampion(Tournament tournament) {
        int maxRound = tournament.getMatches().stream()
                .mapToInt(Match::getRound)
                .max()
                .orElse(0);

        return tournament.getMatches().stream()
                .filter(m -> m.getRound() == maxRound
                        && m.getStatus() == MatchStatus.FINISHED)
                .map(Match::getWinnerId)
                .findFirst()
                .orElse(null);
    }

    private int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
}