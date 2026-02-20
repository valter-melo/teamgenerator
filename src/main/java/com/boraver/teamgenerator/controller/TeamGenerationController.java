package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.dto.teams.*;
import com.boraver.teamgenerator.service.TeamGenerationService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/teams")
public class TeamGenerationController {

  private final TeamGenerationService service;

  public TeamGenerationController(TeamGenerationService service) {
    this.service = service;
  }

  @PostMapping("/generate/db")
  public GenerateTeamsResponse generateFromDb(@Valid @RequestBody GenerateTeamsRequest req, Authentication auth) {
    UUID userId = (UUID) auth.getPrincipal();
    return service.generateFromDb(req, userId);
  }
}
