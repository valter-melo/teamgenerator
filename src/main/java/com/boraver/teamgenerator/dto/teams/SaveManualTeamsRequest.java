package com.boraver.teamgenerator.dto.teams;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record SaveManualTeamsRequest(
    @NotEmpty String name,
    int groupsCount,
    String matchesType,  // "SINGLE" ou "HOME_AND_AWAY"
    int qualifiedPerGroup,
    @NotEmpty List<ManualTeamDTO> teams
) {
  public record ManualTeamDTO(
      int teamIndex,
      @NotEmpty List<UUID> playerIds,
      int groupId
  ) {}
}