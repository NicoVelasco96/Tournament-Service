package com.tournament.tournament.service;

import com.tournament.tournament.dto.TournamentDTO;
import com.tournament.tournament.model.TournamentStatus;

import java.util.List;

public interface ITournamentService {
    public TournamentDTO.TournamentResponse create(TournamentDTO.CreateRequest request);
    public TournamentDTO.TournamentResponse getById(Long id);
    public List<TournamentDTO.TournamentResponse> getByStatus(TournamentStatus status);
    public TournamentDTO.TournamentResponse addPlayer(Long tournamentId, Long playerId);
    public TournamentDTO.TournamentResponse startTournament(Long tournamentId);
    public TournamentDTO.MatchResponse reportResult(Long matchId, TournamentDTO.ReportResultRequest request);
    public List<TournamentDTO.MatchResponse> getMatchesByTournament(Long tournamentId);
    public TournamentDTO.TournamentResponse openTournament(Long tournamentId);
    public List<Object> getPlayersByTournament(Long tournamentId);
}