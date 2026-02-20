package com.boraver.teamgenerator.dto.auth;

public record AuthResponse(String token, String tenantId, String userId, String role) {}
