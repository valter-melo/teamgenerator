package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.dto.player.*;
import com.boraver.teamgenerator.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/players")
@Tag(name = "Jogadores", description = "Endpoint para gestão de jogadores")
public class PlayerController {

  private final PlayerService service;

  public PlayerController(PlayerService service) { this.service = service; }

  @Operation(summary = "Cadastrar novo jogador")
  @PostMapping
  public PlayerResponse create(@Valid @RequestBody CreatePlayerRequest req) {
    return service.create(req);
  }

  @Operation(summary = "Listar jogadores ativos")
  @GetMapping
  public List<PlayerResponse> listActive() {
    return service.listActive();
  }

  @Operation(summary = "Obter jogador pelo ID")
  @GetMapping("/{id}")
  public PlayerResponse get(@PathVariable UUID id) {
    return service.get(id);
  }

  @Operation(summary = "Atualizar jogador")
  @PatchMapping("/{id}")
  public PlayerResponse update(@PathVariable UUID id, @RequestBody UpdatePlayerRequest req) {
    return service.update(id, req);
  }
}
