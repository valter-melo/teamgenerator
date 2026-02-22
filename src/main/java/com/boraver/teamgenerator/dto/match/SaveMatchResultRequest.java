package com.boraver.teamgenerator.dto.match;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class SaveMatchResultRequest {
  @NotNull
  private Set<UUID> winningPlayerIds; // IDs dos jogadores do time vencedor
  private int teamScore;
  private int opponentScore;
}