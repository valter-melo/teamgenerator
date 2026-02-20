package com.boraver.teamgenerator.dto.player;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatePlayerRequest(
    @NotBlank String name,
    @Pattern(regexp = "M|F") String sex
) {}