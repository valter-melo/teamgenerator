package com.boraver.teamgenerator.dto.championship;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MatchResultRequest(
        @NotNull UUID matchId,
        int homeScore,
        int awayScore,
        Boolean walkover,
        Integer winnerTeamIndex,
        Integer woWinnerPoints
) {}