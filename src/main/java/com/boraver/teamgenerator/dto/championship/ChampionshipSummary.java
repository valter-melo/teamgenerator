package com.boraver.teamgenerator.dto.championship;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChampionshipSummary(
    UUID id,
    String name,
    LocalDateTime createdAt,
    String status,
    int teamCount,
    int groupsCount
) {}
