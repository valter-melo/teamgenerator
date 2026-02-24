package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.service.GameSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/game-sessions")
@RequiredArgsConstructor
public class GameSessionController {
  private final GameSessionService service;

  @PostMapping("/start")
  public ResponseEntity<?> startSession(Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    try {
      service.startSession(tenantId);
      return ResponseEntity.ok().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  @PostMapping("/end")
  public void endSession(Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    service.endSession(tenantId);
  }

  @GetMapping("/active")
  public boolean isActive(Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    return service.hasActiveSession(tenantId);
  }
}
