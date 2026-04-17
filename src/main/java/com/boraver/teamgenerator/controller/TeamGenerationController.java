package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.dto.teams.*;
import com.boraver.teamgenerator.service.GameSessionService;
import com.boraver.teamgenerator.service.TeamGenerationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams")
public class TeamGenerationController {

  private final TeamGenerationService service;
  private final GameSessionService gameSessionService;

  public TeamGenerationController(TeamGenerationService service, GameSessionService gameSessionService) {
    this.service = service;
    this.gameSessionService = gameSessionService;
  }

  @PostMapping("/generate/db")
  public GenerateTeamsResponse generateFromDb(@Valid @RequestBody GenerateTeamsRequest req, Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    UUID userId = (UUID) auth.getPrincipal();

    if (gameSessionService.hasActiveSession(tenantId)) {
      throw new IllegalStateException("Não é possível gerar novos times enquanto uma sessão de jogos está em andamento.");
    }

    return service.generateFromDb(req, userId);
  }

  @PostMapping("/generate-from-pots")
  public ResponseEntity<SaveGeneratedResponse> saveGenerated(
      @Valid @RequestBody SaveGeneratedRequest request,
      Authentication auth
  ) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    UUID userId = (UUID) auth.getPrincipal();
    SaveGeneratedResponse response = service.generateFromPots(tenantId, userId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/session/{sessionId}")
  public ResponseEntity<List<GenerateTeamsResponse.Team>> getTeamsBySession(@PathVariable UUID sessionId, Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    List<GenerateTeamsResponse.Team> teams = service.getTeamsBySession(sessionId, tenantId);
    return ResponseEntity.ok(teams);
  }

  @PostMapping("/save-manual")
  public ResponseEntity<SaveManualTeamsResponse> saveManualTeams(
      @Valid @RequestBody SaveManualTeamsRequest request,
      Authentication auth
  ) throws JsonProcessingException {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    UUID userId = (UUID) auth.getPrincipal();
    SaveManualTeamsResponse response = service.saveManualTeams(tenantId, userId, request);
    return ResponseEntity.ok(response);
  }
}
