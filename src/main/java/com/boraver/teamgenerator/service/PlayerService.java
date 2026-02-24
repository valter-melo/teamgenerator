package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.dto.player.*;
import com.boraver.teamgenerator.model.Player;
import com.boraver.teamgenerator.repository.PlayerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PlayerService {
  private final PlayerRepository repo;

  public PlayerService(PlayerRepository repo) { this.repo = repo; }

  private UUID tenantId() {
    return UUID.fromString(Objects.requireNonNull(TenantContext.getTenantId(), "tenant missing"));
  }

  @Transactional
  public PlayerResponse create(CreatePlayerRequest req) {
    Player p = new Player();
    p.setTenantId(tenantId());
    p.setName(req.name());
    p.setSex(req.sex().charAt(0));
    p = repo.save(p);
    return toResponse(p);
  }

  public List<PlayerResponse> listActive() {
    Sort sort = Sort.by("name").ascending();
    return repo.findAllByTenantIdAndActiveTrue(tenantId(), sort).stream().map(this::toResponse).toList();
  }

  public PlayerResponse get(UUID id) {
    Player p = repo.findByIdAndTenantId(id, tenantId()).orElseThrow(() -> new IllegalArgumentException("Player not found"));
    return toResponse(p);
  }

  @Transactional
  public PlayerResponse update(UUID id, UpdatePlayerRequest req) {
    UUID tenant = tenantId();
    Player player = repo.findByIdAndTenantId(id, tenant)
        .orElseThrow(() -> new RuntimeException("Jogador não encontrado"));

    if (req.name() != null) {
      player.setName(req.name());
    }
    if (req.sex() != null) {
      player.setSex(req.sex().charAt(0));
    }
    if (req.active() != null) {
      player.setActive(req.active()); // atualiza o status
    }

    repo.save(player);
    return new PlayerResponse(player.getId(), player.getName(), String.valueOf(player.getSex()), player.isActive());
  }

  @Transactional
  public void delete(UUID id) {
    repo.deleteById(id);
  }

  private PlayerResponse toResponse(Player p) {
    return new PlayerResponse(p.getId(), p.getName(), String.valueOf(p.getSex()), p.isActive());
  }
}
