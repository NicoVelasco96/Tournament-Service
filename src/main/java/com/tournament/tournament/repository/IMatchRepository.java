package com.tournament.tournament.repository;

import com.tournament.tournament.model.Match;
import com.tournament.tournament.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournamentId(Long tournamentId);
    List<Match> findByTournamentIdAndRound(Long tournamentId, Integer round);
    List<Match> findByPlayer1IdOrPlayer2Id(Long player1Id, Long player2Id);
    boolean existsByTournamentIdAndStatus(Long tournamentId, MatchStatus status);
}
