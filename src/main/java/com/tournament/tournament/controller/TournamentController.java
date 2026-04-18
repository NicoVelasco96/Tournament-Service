package com.tournament.tournament.controller;

import com.tournament.tournament.dto.TournamentDTO;
import com.tournament.tournament.model.TournamentStatus;
import com.tournament.tournament.service.ITournamentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private ITournamentService tournamentService;

    @PostMapping
    public ResponseEntity<TournamentDTO.TournamentResponse> create(
            @Valid @RequestBody TournamentDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO.TournamentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<TournamentDTO.TournamentResponse>> getByStatus(
            @RequestParam(required = false, defaultValue = "OPEN") TournamentStatus status) {
        return ResponseEntity.ok(tournamentService.getByStatus(status));
    }

    @PostMapping("/{id}/players/{playerId}")
    public ResponseEntity<TournamentDTO.TournamentResponse> addPlayer(
            @PathVariable Long id,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(tournamentService.addPlayer(id, playerId));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<TournamentDTO.TournamentResponse> startTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.startTournament(id));
    }

    @PostMapping("/matches/{matchId}/result")
    public ResponseEntity<TournamentDTO.MatchResponse> reportResult(
            @PathVariable Long matchId,
            @Valid @RequestBody TournamentDTO.ReportResultRequest request) {
        return ResponseEntity.ok(tournamentService.reportResult(matchId, request));
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<TournamentDTO.MatchResponse>> getMatches(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getMatchesByTournament(id));
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<TournamentDTO.TournamentResponse> openTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.openTournament(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<List<Object>> getPlayers(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getPlayersByTournament(id));
    }
}