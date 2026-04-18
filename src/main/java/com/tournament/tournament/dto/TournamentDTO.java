package com.tournament.tournament.dto;

import com.tournament.tournament.model.MatchStatus;
import com.tournament.tournament.model.TournamentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class TournamentDTO {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String name;

        private String description;

        @NotNull
        @Min(2)
        private Integer maxPlayers;

        @NotNull
        private Long organizerId;
    }

    @Data
    public static class TournamentResponse {
        private Long id;
        private String name;
        private String description;
        private Integer maxPlayers;
        private Integer currentPlayers;
        private TournamentStatus status;
        private Long organizerId;
        private LocalDateTime createdAt;
    }

    @Data
    public static class MatchResponse {
        private Long id;
        private Integer round;
        private Integer matchNumber;
        private Long player1Id;
        private Long player2Id;
        private Long winnerId;
        private MatchStatus status;
        private LocalDateTime scheduledAt;
        private LocalDateTime finishedAt;
    }

    @Data
    public static class ReportResultRequest {
        @NotNull
        private Long winnerId;
    }
}