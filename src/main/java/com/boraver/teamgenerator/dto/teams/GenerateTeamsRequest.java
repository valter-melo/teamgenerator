package com.boraver.teamgenerator.dto.teams;

import java.util.*;

public record GenerateTeamsRequest(
    List<UUID> playerIds,
    int teamCount,
    int playersPerTeam,
    List<SelectedSkill> selectedSkills,
    Map<String, Double> sexMultiplier, // {"M":1.0,"F":0.92}
    SexBalance sexBalance,
    String extrasPolicy // "BENCH"
) {
  public record SelectedSkill(UUID skillId, double weight) {}
  public record SexBalance(boolean enabled, int maxMaleDiff) {}
}
