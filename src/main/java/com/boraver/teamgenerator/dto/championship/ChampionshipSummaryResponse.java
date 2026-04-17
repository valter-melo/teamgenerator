package com.boraver.teamgenerator.dto.championship;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChampionshipSummaryResponse(
    UUID id,
    String name,
    LocalDateTime createdAt,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    int teamCount,
    int groupsCount,
    String status
) {}