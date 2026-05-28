package com.boraver.teamgenerator.dto.player;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlayerResponse(
        UUID id,
        String name,
        String sex,
        boolean active,
        String email,
        String phone,
        LocalDate birthDate,
        List<PositionInfo> positions       // NOVO
) {
  public record PositionInfo(
          UUID positionId,
          String name,
          int priority
  ) {}
}