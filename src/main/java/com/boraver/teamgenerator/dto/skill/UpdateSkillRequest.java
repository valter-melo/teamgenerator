package com.boraver.teamgenerator.dto.skill;

public record UpdateSkillRequest(
    String code,
    String name,
    Boolean active
) {}