package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.dto.player.*;
import com.boraver.teamgenerator.dto.position.PositionResponse;
import com.boraver.teamgenerator.entity.Player;
import com.boraver.teamgenerator.entity.Skill;
import com.boraver.teamgenerator.entity.PlayerPosition;
import com.boraver.teamgenerator.repository.PlayerPositionRepository;
import com.boraver.teamgenerator.repository.PlayerRepository;
import com.boraver.teamgenerator.repository.RatingRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerService {
  private final PlayerRepository playerRepository;
  private final RatingRepository ratingRepository;
  private final SkillService skillService;;
  private final RatingService ratingService;
  private final PlayerPositionRepository playerPositionRepository;
  private final PositionService positionService;

  private UUID tenantId() {
    return UUID.fromString(Objects.requireNonNull(TenantContext.getTenantId(), "tenant missing"));
  }

  @Transactional
  public PlayerResponse create(CreatePlayerRequest req) {
    UUID tenant = tenantId();
    String name = req.name().trim();

    if (playerRepository.findByTenantIdAndNameIgnoreCase(tenant, name).isPresent()) {
      throw new IllegalArgumentException("Já existe um jogador com o nome '" + name + "'");
    }

    Player p = new Player();
    p.setTenantId(tenant);
    p.setName(name);
    p.setSex(req.sex().charAt(0));
    p.setEmail(req.email());
    p.setPhone(req.phone());
    p.setBirthDate(req.birthDate());
    p = playerRepository.save(p);

    // Salva posições
    if (req.positions() != null && !req.positions().isEmpty()) {
      savePlayerPositions(p.getId(), tenant, req.positions());
    }

    return toResponse(p);
  }

  @Transactional
  public PlayerResponse update(UUID id, UpdatePlayerRequest req) {
    UUID tenant = tenantId();
    Player player = playerRepository.findByIdAndTenantId(id, tenant)
            .orElseThrow(() -> new RuntimeException("Jogador não encontrado"));

    if (req.name() != null) player.setName(req.name());
    if (req.sex() != null) player.setSex(req.sex().charAt(0));
    if (req.active() != null) player.setActive(req.active());
    if (req.email() != null) player.setEmail(req.email());
    if (req.phone() != null) player.setPhone(req.phone());
    if (req.birthDate() != null) player.setBirthDate(req.birthDate());

    playerRepository.save(player);

    if (req.positions() != null) {
      playerPositionRepository.deleteAllByPlayerId(id);
      if (!req.positions().isEmpty()) {
        updatePlayerPositions(id, tenant, req.positions());
      }
    }

    return toResponse(player);
  }

  private void savePlayerPositions(UUID playerId, UUID tenant, List<CreatePlayerRequest.PositionAssignment> assignments) {
    List<PlayerPosition> pps = assignments.stream().map(ass -> {
      PlayerPosition pp = new PlayerPosition();
      pp.setPlayerId(playerId);
      pp.setPositionId(ass.positionId());
      pp.setPriority(ass.priority());
      pp.setTenantId(tenant);
      return pp;
    }).collect(Collectors.toList());
    playerPositionRepository.saveAll(pps);
  }

  private void updatePlayerPositions(UUID playerId, UUID tenant, List<UpdatePlayerRequest.PositionAssignment> assignments) {
    List<PlayerPosition> pps = assignments.stream().map(ass -> {
      PlayerPosition pp = new PlayerPosition();
      pp.setPlayerId(playerId);
      pp.setPositionId(ass.positionId());
      pp.setPriority(ass.priority());
      pp.setTenantId(tenant);
      return pp;
    }).collect(Collectors.toList());
    playerPositionRepository.saveAll(pps);
  }

  private List<PlayerResponse.PositionInfo> getPlayerPositions(UUID playerId) {
    List<PlayerPosition> pps = playerPositionRepository.findAllByPlayerIdOrderByPriorityAsc(playerId);
    Map<UUID, String> positionNames = positionService.listActive().stream()
            .collect(Collectors.toMap(PositionResponse::id, PositionResponse::name));
    return pps.stream()
            .map(pp -> new PlayerResponse.PositionInfo(
                    pp.getPositionId(),
                    positionNames.getOrDefault(pp.getPositionId(), "?"),
                    pp.getPriority()))
            .collect(Collectors.toList());
  }

  public List<PlayerResponse> listActive() {
    Sort sort = Sort.by("name").ascending();
    return playerRepository.findAllByTenantIdAndActiveTrue(tenantId(), sort).stream().map(this::toResponse).toList();
  }

  public PlayerResponse get(UUID id) {
    Player p = playerRepository.findByIdAndTenantId(id, tenantId()).orElseThrow(() -> new IllegalArgumentException("Player not found"));
    return toResponse(p);
  }

  @Transactional
  public void delete(UUID id) {
    playerRepository.deleteById(id);
  }

  private PlayerResponse toResponse(Player p) {
    return new PlayerResponse(
            p.getId(),
            p.getName(),
            String.valueOf(p.getSex()),
            p.isActive(),
            p.getEmail(),
            p.getPhone(),
            p.getBirthDate(),
            getPlayerPositions(p.getId())   // NOVO
    );
  }

  public long countActive(UUID tenantId) {
    return playerRepository.countByTenantIdAndActiveTrue(tenantId);
  }

  public List<InactivePlayerDTO> getInactivePlayers(UUID tenantId) {
    List<Object[]> rows = playerRepository.findInactivePlayersRaw(tenantId.toString());
    return rows.stream().map(row -> {
      String name = (String) row[0];
      LocalDateTime lastParticipation = (row[1] != null) ? (LocalDateTime) row[1] : null;
      return new InactivePlayerDTO(name, lastParticipation);
    }).collect(Collectors.toList());
  }

  public List<PlayerPerformanceDTO> getPerformanceData(UUID tenantId) {
    List<Player> players = playerRepository.findAllByTenantIdAndActiveTrue(tenantId, Sort.by("name"));
    List<Skill> skills = skillService.getActiveSkills();

    Map<UUID, String> skillNames = skills.stream()
            .collect(Collectors.toMap(Skill::getId, Skill::getName));

    List<PlayerPerformanceDTO> result = new ArrayList<>();
    for (Player player : players) {
      Map<UUID, Integer> currentRatings;
      try {
        currentRatings = ratingService.currentRatingsMap(player.getId());
      } catch (Exception e) {
        currentRatings = Collections.emptyMap();
      }

      // Calcula overall (média simples de todas as skills avaliadas)
      double avg = currentRatings.values().stream()
              .filter(v -> v != null)
              .mapToInt(Integer::intValue)
              .average()
              .orElse(0.0);

      // Define nível baseado na média
      String nivel;
      if (avg >= 4.5) nivel = "Elite";
      else if (avg >= 4.0) nivel = "Muito alto";
      else if (avg >= 3.5) nivel = "Alto";
      else if (avg >= 3.0) nivel = "Médio";
      else if (avg >= 2.0) nivel = "A desenvolver";
      else nivel = "Iniciante";

      // Melhor e pior habilidade
      String bestSkill = null, worstSkill = null;
      int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
      for (Map.Entry<UUID, Integer> entry : currentRatings.entrySet()) {
        int val = entry.getValue();
        if (val > max) { max = val; bestSkill = skillNames.get(entry.getKey()); }
        if (val < min) { min = val; worstSkill = skillNames.get(entry.getKey()); }
      }

      // Converte para o DTO com nomes das skills
      Map<String, Integer> skillsMap = new LinkedHashMap<>();
      for (Map.Entry<UUID, Integer> entry : currentRatings.entrySet()) {
        String skillName = skillNames.get(entry.getKey());
        if (skillName != null) skillsMap.put(skillName, entry.getValue());
      }

      // Data da última atualização – buscamos a data mais recente entre os ratings
      LocalDateTime lastUpdate = ratingRepository.findLastRatingDateByPlayerId(player.getId());

      result.add(new PlayerPerformanceDTO(
              player.getId(),
              player.getName(),
              String.valueOf(player.getSex()),
              Math.round(avg * 10.0) / 10.0,
              nivel,
              bestSkill,
              worstSkill,
              skillsMap,
              lastUpdate != null ? lastUpdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—"
      ));
    }
    return result;
  }
}
