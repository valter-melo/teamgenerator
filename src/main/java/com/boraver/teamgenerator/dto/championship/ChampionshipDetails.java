package com.boraver.teamgenerator.dto.championship;

import java.util.List;
import java.util.Map;

public record ChampionshipDetails(
    ChampionshipResponse championship,
    List<TeamInfo> teams,
    Map<Integer, List<StandingEntry>> standingsByGroup,
    Map<Integer, List<MatchDetails>> matchesByGroup,
    List<MatchDetails> knockoutMatches
) {}