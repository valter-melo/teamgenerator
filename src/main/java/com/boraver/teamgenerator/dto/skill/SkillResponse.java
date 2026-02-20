package com.boraver.teamgenerator.dto.skill;

import java.util.UUID;

public record SkillResponse(UUID id, String code, String name, boolean active) {}