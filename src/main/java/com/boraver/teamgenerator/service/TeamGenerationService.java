package com.boraver.teamgenerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.dto.teams.*;
import com.boraver.teamgenerator.entity.*;
import com.boraver.teamgenerator.repository.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamGenerationService {

  private final PlayerRepository playerRepository;
  private final RatingRepository ratingRepository;
  private final TeamSessionRepository sessionRepository;
  private final GeneratedTeamRepository teamRepository;
  private final GeneratedTeamPlayerRepository teamPlayerRepository;
  private final ChampionshipRepository championshipRepository;
  private final ChampionshipService championshipService;
  private final ObjectMapper mapper;
  private final JdbcTemplate jdbcTemplate;

  private UUID tenantId() {
    return UUID.fromString(Objects.requireNonNull(TenantContext.getTenantId(), "tenant missing"));
  }

  @Transactional
  public GenerateTeamsResponse generateFromDb(GenerateTeamsRequest req, UUID createdBy) {
    UUID tenant = tenantId();

    if (req.teamCount() <= 0 || req.playersPerTeam() <= 0) {
      throw new IllegalArgumentException("teamCount and playersPerTeam must be > 0");
    }

    int needed = req.teamCount() * req.playersPerTeam();
    if (req.playerIds() == null || req.playerIds().size() < needed) {
      throw new IllegalArgumentException("Not enough players. Needed=" + needed);
    }
    if (req.selectedSkills() == null || req.selectedSkills().isEmpty()) {
      throw new IllegalArgumentException("selectedSkills cannot be empty");
    }

    List<Player> players = playerRepository.findAllByTenantIdAndIdIn(tenant, req.playerIds())
        .stream()
        .filter(Player::isActive)
        .toList();

    if (players.size() < needed) {
      throw new IllegalArgumentException("Not enough ACTIVE players. Needed=" + needed + ", activeFound=" + players.size());
    }

    List<UUID> playerIds = players.stream().map(Player::getId).toList();
    List<UUID> skillIds = req.selectedSkills().stream().map(GenerateTeamsRequest.SelectedSkill::skillId).toList();

    var ratings = ratingRepository.findCurrentRatings(tenant, playerIds, skillIds);

    Map<UUID, Map<UUID, Integer>> ratingMap = new HashMap<>();
    for (var r : ratings) {
      ratingMap
          .computeIfAbsent(r.getPlayerId(), k -> new HashMap<>())
          .put(r.getSkillId(), (int) r.getRating());
    }

    Map<UUID, Double> skillAvg = computeSkillAverages(playerIds, skillIds, ratingMap);

    double multM = (req.sexMultiplier() != null && req.sexMultiplier().get("M") != null)
        ? req.sexMultiplier().get("M")
        : 1.0;

    double multF = (req.sexMultiplier() != null && req.sexMultiplier().get("F") != null)
        ? req.sexMultiplier().get("F")
        : 0.92;

    List<ScoredPlayer> scored = new ArrayList<>();
    for (Player p : players) {
      double base = 0.0;

      Map<UUID, Integer> pr = ratingMap.getOrDefault(p.getId(), Collections.emptyMap());

      for (GenerateTeamsRequest.SelectedSkill s : req.selectedSkills()) {
        int r = pr.getOrDefault(
            s.skillId(),
            (int) Math.round(skillAvg.getOrDefault(s.skillId(), 2.5))
        );
        base += r * s.weight();
      }

      double mult = (p.getSex() == 'M') ? multM : multF;
      scored.add(new ScoredPlayer(p, base, base * mult, pr));
    }

    // ✅ CORREÇÃO PRINCIPAL: evita inferência errada (Object)
    scored.sort(Comparator.comparingDouble(ScoredPlayer::getFinalScore).reversed());

    List<TeamBucket> teams = new ArrayList<>();
    for (int i = 0; i < req.teamCount(); i++) {
      teams.add(new TeamBucket(i + 1));
    }

    boolean sexBal = req.sexBalance() != null && req.sexBalance().enabled();
    int maxMaleDiff = req.sexBalance() != null ? Math.max(0, req.sexBalance().maxMaleDiff()) : 1;

    // Pega apenas os "needed" melhores
    List<ScoredPlayer> selected = scored.subList(0, needed);

    for (ScoredPlayer sp : selected) {
      TeamBucket chosen = chooseTeam(teams, sp, req.playersPerTeam(), sexBal, maxMaleDiff);
      chosen.add(sp);
    }

    // Persist session
    TeamGenerationSession session = new TeamGenerationSession();
    session.setTenantId(tenant);
    session.setMode("DB");
    session.setCreatedBy(createdBy);
    session.setTeamCount(req.teamCount());
    session.setPlayersPerTeam(req.playersPerTeam());
    session.setPlayersCount(needed);
    session.setRulesJson(mapper.valueToTree(req).toString());

    session = sessionRepository.save(session);

    for (TeamBucket tb : teams) {
      GeneratedTeam gt = new GeneratedTeam();
      gt.setSessionId(session.getId());
      gt.setTeamIndex(tb.teamIndex);
      gt.setName("Time " + tb.teamIndex);
      gt = teamRepository.save(gt);

      for (ScoredPlayer sp : tb.players) {
        GeneratedTeamPlayer gtp = new GeneratedTeamPlayer();
        gtp.setTeamId(gt.getId());
        gtp.setPlayerId(sp.player.getId());
        gtp.setSexAtGeneration(String.valueOf(sp.player.getSex()));
        gtp.setScoreAtGeneration(BigDecimal.valueOf(sp.getFinalScore()));

        var snap = mapper.createObjectNode();
        snap.put("baseScore", sp.getBaseScore());
        snap.put("finalScore", sp.getFinalScore());

        var ratingsSnap = mapper.createObjectNode();
        for (var entry : sp.currentRatings.entrySet()) {
          ratingsSnap.put(entry.getKey().toString(), entry.getValue());
        }
        snap.set("ratingsUsed", ratingsSnap);
        gtp.setSnapshotJson(snap.toString());

        teamPlayerRepository.save(gtp);
      }
    }

    var respTeams = teams.stream().map(tb ->
        new GenerateTeamsResponse.Team(
            tb.teamIndex,
            tb.sum,
            tb.players.stream().map(p ->
                new GenerateTeamsResponse.PlayerPick(
                    p.player.getId(),
                    p.player.getName(),
                    String.valueOf(p.player.getSex()),
                    p.getFinalScore()
                )
            ).toList()
        )
    ).toList();

    return new GenerateTeamsResponse(session.getId(), respTeams);
  }

  @Transactional
  public SaveGeneratedResponse generateFromPots(UUID tenantId, UUID userId, SaveGeneratedRequest request) {
    if (request.teams() == null || request.teams().isEmpty()) {
      throw new IllegalArgumentException("Nenhum time enviado.");
    }

    int teamCount = request.teams().size();
    int playersPerTeam = request.teams().get(0).players().size(); // assume que todos os times têm o mesmo número de jogadores

    // Coletar todos os IDs de jogadores
    List<UUID> allPlayerIds = request.teams().stream()
        .flatMap(t -> t.players().stream())
        .map(p -> UUID.fromString(p.id()))
        .collect(Collectors.toList());

    // Validar que todos os jogadores existem e pertencem ao tenant
    List<Player> players = playerRepository.findAllByIdInAndTenantId(allPlayerIds, tenantId);
    if (players.size() != allPlayerIds.size()) {
      throw new IllegalArgumentException("Alguns jogadores não foram encontrados ou não pertencem ao tenant.");
    }

    // Criar a sessão de geração
    TeamGenerationSession session = new TeamGenerationSession();
    session.setTenantId(tenantId);
    session.setMode("MANUAL");
    session.setCreatedBy(userId);
    session.setTeamCount(teamCount);
    session.setPlayersPerTeam(playersPerTeam);
    session.setPlayersCount(teamCount * playersPerTeam);
    try {
      session.setRulesJson(mapper.valueToTree(request).toString());
    } catch (Exception e) {
      // log
    }
    session = sessionRepository.save(session);

    // Para cada time, criar GeneratedTeam e GeneratedTeamPlayer
    List<GenerateTeamsResponse.Team> teamDtos = new ArrayList<>();
    for (SaveGeneratedRequest.TeamDTO teamDto : request.teams()) {
      GeneratedTeam gt = new GeneratedTeam();
      gt.setSessionId(session.getId());
      gt.setTeamIndex(teamDto.teamIndex());
      gt.setName("Time " + teamDto.teamIndex());
      gt = teamRepository.save(gt);

      double sumScore = 0;
      List<GenerateTeamsResponse.PlayerPick> picks = new ArrayList<>();
      for (SaveGeneratedRequest.PlayerPickDTO playerDto : teamDto.players()) {
        GeneratedTeamPlayer gtp = new GeneratedTeamPlayer();
        gtp.setTeamId(gt.getId());
        gtp.setPlayerId(UUID.fromString(playerDto.id()));
        gtp.setSexAtGeneration(playerDto.sex());
        gtp.setScoreAtGeneration(BigDecimal.valueOf(playerDto.score()));

        // Criar snapshot simples (obrigatório)
        var snap = mapper.createObjectNode();
        snap.put("method", "manual");
        snap.put("source", "pote_selection");
        gtp.setSnapshotJson(snap.toString());

        teamPlayerRepository.save(gtp);

        picks.add(new GenerateTeamsResponse.PlayerPick(
            UUID.fromString(playerDto.id()),
            playerDto.name(),
            playerDto.sex(),
            playerDto.score()
        ));
        sumScore += playerDto.score();
      }

      teamDtos.add(new GenerateTeamsResponse.Team(
          teamDto.teamIndex(),
          sumScore,
          picks
      ));
    }

    return new SaveGeneratedResponse(session.getId(), teamDtos);
  }

  public List<GenerateTeamsResponse.Team> getTeamsBySession(UUID sessionId, UUID tenantId) {
    TeamGenerationSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada"));
    if (!session.getTenantId().equals(tenantId)) {
      throw new SecurityException("Acesso negado");
    }
    List<GeneratedTeam> generatedTeams = teamRepository.findAllBySessionIdOrderByTeamIndexAsc(sessionId);
    List<GenerateTeamsResponse.Team> result = new ArrayList<>();
    for (GeneratedTeam gt : generatedTeams) {
      List<GeneratedTeamPlayer> players = teamPlayerRepository.findByTeamId(gt.getId());
      List<GenerateTeamsResponse.PlayerPick> playerPicks = players.stream()
          .map(p -> {
            Player player = playerRepository.findById(p.getPlayerId()).orElseThrow();
            return new GenerateTeamsResponse.PlayerPick(
                player.getId(),
                player.getName(),
                String.valueOf(player.getSex()),
                p.getScoreAtGeneration().doubleValue()
            );
          })
          .collect(Collectors.toList());
      double sumScore = players.stream().mapToDouble(p -> p.getScoreAtGeneration().doubleValue()).sum();
      result.add(new GenerateTeamsResponse.Team(gt.getTeamIndex(), sumScore, playerPicks));
    }
    return result;
  }

  @Transactional
  public SaveManualTeamsResponse saveManualTeams(
      UUID tenantId,
      UUID userId,
      SaveManualTeamsRequest request
  ) throws JsonProcessingException {
    List<UUID> allPlayerIds = request.teams().stream()
        .flatMap(t -> t.playerIds().stream())
        .collect(Collectors.toList());

    List<Player> players = playerRepository.findAllByIdInAndTenantId(allPlayerIds, tenantId);
    if (players.size() != allPlayerIds.size()) {
      throw new IllegalArgumentException("Alguns jogadores não foram encontrados ou não pertencem ao tenant");
    }

    // 2. Criar a sessão de geração (TeamGenerationSession)
    TeamGenerationSession session = new TeamGenerationSession();
    session.setTenantId(tenantId);
    session.setMode("MANUAL");
    session.setCreatedBy(userId);
    session.setTeamCount(request.teams().size());
    session.setPlayersPerTeam(request.teams().get(0).playerIds().size());
    session.setPlayersCount(allPlayerIds.size());

    // Armazenar metadados da montagem (nome, grupos, etc.)
    ObjectNode rules = mapper.createObjectNode();
    rules.put("name", request.name());
    rules.put("groupsCount", request.groupsCount());
    ObjectNode groupsNode = mapper.createObjectNode();
    for (SaveManualTeamsRequest.ManualTeamDTO team : request.teams()) {
      groupsNode.put(String.valueOf(team.teamIndex()), team.groupId());
    }
    rules.set("teamGroups", groupsNode);
    session.setRulesJson(mapper.writeValueAsString(rules));
    session = sessionRepository.save(session);

    // 3. Persistir os times e jogadores (GeneratedTeam, GeneratedTeamPlayer)
    //    (código idêntico ao anterior, omitido para brevidade)
    List<GeneratedTeam> generatedTeams = new ArrayList<>();
    for (SaveManualTeamsRequest.ManualTeamDTO teamDto : request.teams()) {
      GeneratedTeam gt = new GeneratedTeam();
      gt.setSessionId(session.getId());
      gt.setTeamIndex(teamDto.teamIndex());
      gt.setName("Time " + teamDto.teamIndex());
      gt = teamRepository.save(gt);
      generatedTeams.add(gt);

      for (UUID playerId : teamDto.playerIds()) {
        Player player = players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElseThrow();
        GeneratedTeamPlayer gtp = new GeneratedTeamPlayer();
        gtp.setTeamId(gt.getId());
        gtp.setPlayerId(player.getId());
        gtp.setSexAtGeneration(String.valueOf(player.getSex()));
        gtp.setScoreAtGeneration(BigDecimal.ZERO);
        ObjectNode snapshot = mapper.createObjectNode();
        snapshot.put("method", "manual");
        gtp.setSnapshotJson(mapper.writeValueAsString(snapshot));
        teamPlayerRepository.save(gtp);
      }
    }

    // 4. Construir o mapa teamIndex -> groupId
    Map<Integer, Integer> teamGroupMap = new HashMap<>();
    for (SaveManualTeamsRequest.ManualTeamDTO team : request.teams()) {
      teamGroupMap.put(team.teamIndex(), team.groupId());
    }

    // 5. Delegar a criação do campeonato para o ChampionshipService
    Championship championship = championshipService.createChampionshipFromManual(
        tenantId, request, session, generatedTeams, teamGroupMap
    );

    // 6. Retornar resposta
    SaveManualTeamsResponse response = new SaveManualTeamsResponse(session.getId(), championship.getId());
    return response;

  }

  private Map<UUID, Double> computeSkillAverages(
      List<UUID> playerIds,
      List<UUID> skillIds,
      Map<UUID, Map<UUID, Integer>> ratingMap
  ) {
    Map<UUID, List<Integer>> buckets = new HashMap<>();
    for (UUID sid : skillIds) buckets.put(sid, new ArrayList<>());

    for (UUID pid : playerIds) {
      Map<UUID, Integer> pr = ratingMap.getOrDefault(pid, Collections.emptyMap());
      for (UUID sid : skillIds) {
        Integer r = pr.get(sid);
        if (r != null) buckets.get(sid).add(r);
      }
    }

    Map<UUID, Double> avg = new HashMap<>();
    for (UUID sid : skillIds) {
      List<Integer> list = buckets.get(sid);
      avg.put(sid, list.isEmpty() ? 2.5 : list.stream().mapToInt(i -> i).average().orElse(2.5));
    }
    return avg;
  }

  private TeamBucket chooseTeam(
      List<TeamBucket> teams,
      ScoredPlayer sp,
      int playersPerTeam,
      boolean sexBalanceEnabled,
      int maxMaleDiff
  ) {
    List<TeamBucket> candidates = teams.stream()
        .filter(t -> t.players.size() < playersPerTeam)
        .sorted(Comparator.comparingDouble(t -> t.sum))
        .toList();

    if (!sexBalanceEnabled) return candidates.get(0);

    int minM = teams.stream().mapToInt(t -> t.males).min().orElse(0);
    int maxM = teams.stream().mapToInt(t -> t.males).max().orElse(0);

    for (TeamBucket t : candidates) {
      int projectedMale = t.males + (sp.player.getSex() == 'M' ? 1 : 0);
      int projectedMin = Math.min(minM, projectedMale);
      int projectedMax = Math.max(maxM, projectedMale);

      if (projectedMax - projectedMin <= maxMaleDiff) return t;
    }
    return candidates.get(0);
  }

  private static class ScoredPlayer {
    final Player player;
    final double baseScore;
    final double finalScore;
    final Map<UUID, Integer> currentRatings;

    ScoredPlayer(Player p, double base, double fin, Map<UUID, Integer> currentRatings) {
      this.player = p;
      this.baseScore = base;
      this.finalScore = fin;
      this.currentRatings = currentRatings;
    }

    double getBaseScore() { return baseScore; }
    double getFinalScore() { return finalScore; }
  }

  private static class TeamBucket {
    final int teamIndex;
    final List<ScoredPlayer> players = new ArrayList<>();
    double sum = 0.0;
    int males = 0;

    TeamBucket(int idx) { this.teamIndex = idx; }

    void add(ScoredPlayer sp) {
      players.add(sp);
      sum += sp.getFinalScore();
      if (sp.player.getSex() == 'M') males++;
    }
  }

  private static String toJson(ObjectMapper mapper, Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to serialize JSON", e);
    }
  }
}