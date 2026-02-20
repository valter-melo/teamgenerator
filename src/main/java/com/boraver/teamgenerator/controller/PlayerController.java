package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.dto.player.*;
import com.boraver.teamgenerator.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/players")
public class PlayerController {

  private final PlayerService service;

  public PlayerController(PlayerService service) { this.service = service; }

  @PostMapping
  public PlayerResponse create(@Valid @RequestBody CreatePlayerRequest req) {
    return service.create(req);
  }

  @GetMapping
  public List<PlayerResponse> listActive() {
    return service.listActive();
  }

  @GetMapping("/{id}")
  public PlayerResponse get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  public PlayerResponse update(@PathVariable UUID id, @RequestBody UpdatePlayerRequest req) {
    return service.update(id, req);
  }
}
