package com.tournament.tournament.service;

import com.tournament.tournament.model.Match;
import com.tournament.tournament.model.Tournament;

import java.util.List;

public interface IBracketGeneratorService {
    public List<Match> generateBracket(Tournament tournament);
    public List<Match> generateNextRound(Tournament tournament, int completedRound);
    public boolean isRoundComplete(Tournament tournament, int round);
    public Long getChampion(Tournament tournament);
}