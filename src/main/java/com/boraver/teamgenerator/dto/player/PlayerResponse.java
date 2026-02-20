package com.boraver.teamgenerator.dto.player;

import java.util.UUID;

public record PlayerResponse(UUID id, String name, String sex, boolean active) {}
