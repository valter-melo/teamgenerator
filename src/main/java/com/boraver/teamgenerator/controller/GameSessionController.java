package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.service.GameSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Controle de Sessão de Jogos", description = "Controla a sessão ativa dos jogos para gestão de placar")
public class GameSessionController {
  private final GameSessionService service;

  @Operation(summary = "Inicia a sessão de jogos")
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

  @Operation(summary = "Encerra a sessão de jogos")
  @PostMapping("/end")
  public void endSession(Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    service.endSession(tenantId);
  }

  @Operation(summary = "Verifica se há sessão ativa")
  @GetMapping("/active")
  public boolean isActive(Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    return service.hasActiveSession(tenantId);
  }
}
