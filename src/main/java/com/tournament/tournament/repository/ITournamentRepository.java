package com.tournament.tournament.repository;

import com.tournament.tournament.model.Tournament;
import com.tournament.tournament.model.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ITournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByStatus(TournamentStatus status);
    List<Tournament> findByOrganizerId(Long organizerId);
    boolean existsByNameAndStatus(String name, TournamentStatus status);
}
