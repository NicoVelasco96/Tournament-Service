package com.tournament.tournament.service;

import com.tournament.tournament.client.PlayerServiceClient;
import com.tournament.tournament.dto.TournamentDTO;
import com.tournament.tournament.messaging.TournamentEventPublisher;
import com.tournament.tournament.model.*;
import com.tournament.tournament.repository.IMatchRepository;
import com.tournament.tournament.repository.ITournamentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TournamentService implements ITournamentService {

    @Autowired
    private ITournamentRepository tournamentRepository;

    @Autowired
    private IMatchRepository matchRepository;

    @Autowired
    private IBracketGeneratorService bracketGeneratorService;

    @Autowired
    private TournamentEventPublisher eventPublisher;

    @Autowired
    private PlayerServiceClient playerServiceClient;

    @Override
    @Transactional
    public TournamentDTO.TournamentResponse create(TournamentDTO.CreateRequest request) {
        if (tournamentRepository.existsByNameAndStatus(request.getName(), TournamentStatus.OPEN)) {
            throw new IllegalArgumentException("Ya existe un torneo abierto con ese nombre");
        }

        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxPlayers(request.getMaxPlayers())
                .organizerId(request.getOrganizerId())
                .build();

        Tournament saved = tournamentRepository.save(tournament);
        log.info("Torneo creado: {}", saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TournamentDTO.TournamentResponse getById(Long id) {
        return tournamentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + id));
    }

    @Override
    @Transactional
    public List<TournamentDTO.TournamentResponse> getByStatus(TournamentStatus status) {
        return tournamentRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TournamentDTO.TournamentResponse addPlayer(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + tournamentId));

        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new IllegalArgumentException("El torneo no está abierto para inscripciones");
        }

        if (tournament.getPlayerIds().contains(playerId)) {
            throw new IllegalArgumentException("El jugador ya está inscripto en este torneo");
        }

        if (tournament.getPlayerIds().size() >= tournament.getMaxPlayers()) {
            throw new IllegalArgumentException("El torneo está completo");
        }

        if (!playerServiceClient.playerExists(playerId)) {
            throw new IllegalArgumentException("El jugador no existe: " + playerId);
        }

        tournament.getPlayerIds().add(playerId);
        return toResponse(tournamentRepository.save(tournament));
    }

    @Override
    @Transactional
    public TournamentDTO.TournamentResponse startTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + tournamentId));

        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new IllegalArgumentException("El torneo no está en estado OPEN");
        }

        if (tournament.getPlayerIds().size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 jugadores para iniciar");
        }

        List<Match> matches = bracketGeneratorService.generateBracket(tournament);
        tournament.getMatches().addAll(matches);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);

        Tournament saved = tournamentRepository.save(tournament);

        eventPublisher.publishTournamentStarted(saved.getId(), saved.getName());
        log.info("Torneo {} iniciado con {} partidos", saved.getName(), matches.size());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TournamentDTO.MatchResponse reportResult(Long matchId, TournamentDTO.ReportResultRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado: " + matchId));

        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalArgumentException("El partido ya tiene resultado reportado");
        }

        if (!request.getWinnerId().equals(match.getPlayer1Id())
                && !request.getWinnerId().equals(match.getPlayer2Id())) {
            throw new IllegalArgumentException("El ganador debe ser uno de los jugadores del partido");
        }

        match.setWinnerId(request.getWinnerId());
        match.setStatus(MatchStatus.FINISHED);
        match.setFinishedAt(LocalDateTime.now());
        matchRepository.save(match);

        Tournament tournament = match.getTournament();

        if (bracketGeneratorService.isRoundComplete(tournament, match.getRound())) {
            List<Match> nextRound = bracketGeneratorService.generateNextRound(tournament, match.getRound());

            if (nextRound.isEmpty()) {
                tournament.setStatus(TournamentStatus.FINISHED);
                Long champion = bracketGeneratorService.getChampion(tournament);
                eventPublisher.publishTournamentFinished(tournament.getId(), champion);
                log.info("Torneo {} finalizado. Campeón: {}", tournament.getName(), champion);
            } else {
                tournament.getMatches().addAll(nextRound);
            }

            tournamentRepository.save(tournament);
        }

        return toMatchResponse(match);
    }

    @Override
    public List<TournamentDTO.MatchResponse> getMatchesByTournament(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId)
                .stream()
                .map(this::toMatchResponse)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private TournamentDTO.TournamentResponse toResponse(Tournament t) {
        TournamentDTO.TournamentResponse res = new TournamentDTO.TournamentResponse();
        res.setId(t.getId());
        res.setName(t.getName());
        res.setDescription(t.getDescription());
        res.setMaxPlayers(t.getMaxPlayers());
        res.setCurrentPlayers(t.getPlayerIds().size());
        res.setStatus(t.getStatus());
        res.setOrganizerId(t.getOrganizerId());
        res.setCreatedAt(t.getCreatedAt());
        return res;
    }

    private TournamentDTO.MatchResponse toMatchResponse(Match m) {
        TournamentDTO.MatchResponse res = new TournamentDTO.MatchResponse();
        res.setId(m.getId());
        res.setRound(m.getRound());
        res.setMatchNumber(m.getMatchNumber());
        res.setPlayer1Id(m.getPlayer1Id());
        res.setPlayer2Id(m.getPlayer2Id());
        res.setWinnerId(m.getWinnerId());
        res.setStatus(m.getStatus());
        res.setScheduledAt(m.getScheduledAt());
        res.setFinishedAt(m.getFinishedAt());
        return res;
    }

    @Override
    @Transactional
    public TournamentDTO.TournamentResponse openTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + tournamentId));

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new IllegalArgumentException("El torneo debe estar en estado DRAFT para abrirse");
        }

        tournament.setStatus(TournamentStatus.OPEN);
        return toResponse(tournamentRepository.save(tournament));
    }

    @Override
    @Transactional
    public List<Object> getPlayersByTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + tournamentId));

        return tournament.getPlayerIds().stream()
                .map(playerId -> playerServiceClient.getPlayer(playerId))
                .filter(player -> player != null)
                .toList();
    }
}