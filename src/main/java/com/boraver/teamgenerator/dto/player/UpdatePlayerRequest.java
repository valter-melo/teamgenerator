package com.boraver.teamgenerator.dto.player;

public record UpdatePlayerRequest(
    String name,
    String sex,
    Boolean active
) {}
