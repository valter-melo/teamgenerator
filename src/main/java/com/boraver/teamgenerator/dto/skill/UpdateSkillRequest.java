package com.boraver.teamgenerator.dto.skill;

public record UpdateSkillRequest(
    String name,
    Boolean active
) {}