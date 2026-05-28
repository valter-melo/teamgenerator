package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.dto.game.*;
import com.boraver.teamgenerator.dto.match.FriendlyMatchRequest;
import com.boraver.teamgenerator.dto.match.FriendlySessionDTO;
import com.boraver.teamgenerator.dto.match.FriendlySessionSummary;
import com.boraver.teamgenerator.entity.*;
import com.boraver.teamgenerator.repository.*;
import com.fasterxml.jackson.databind.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendlySessionService {

  private final TeamGenerationSessionRepository sessionRepository;
  private final GeneratedTeamRepository teamRepository;
  private final GeneratedTeamPlayerRepository teamPlayerRepository;
  private final PlayerRepository playerRepository;
  private final FriendlyMatchRepository friendlyMatchRepository;
  private final ObjectMapper mapper;

  public List<FriendlySessionSummary> listFriendlySessions(UUID tenantId) {
    List<TeamGenerationSession> sessions = sessionRepository
            .findByTenantIdOrderByCreatedAtDesc(tenantId);  // ← alterado aqui

    return sessions.stream()
            .filter(this::isFriendlyMode)
            .map(s -> {
              int teamCount = teamRepository.countBySessionId(s.getId());
              String date = s.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
              return new FriendlySessionSummary(s.getId(), date, teamCount);
            })
            .collect(Collectors.toList());
  }

  public FriendlySessionDTO getFriendlySessionDetails(UUID tenantId, UUID sessionId) {
    TeamGenerationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    if (!session.getTenantId().equals(tenantId)) {
      throw new SecurityException("Access denied");
    }

    JsonNode rules = parseRules(session.getRulesJson());
    String date = session.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    List<FriendlySessionDTO.CourtDTO> courts = buildCourtList(session.getId(), rules);

    return new FriendlySessionDTO(sessionId, date, courts);
  }

  // Registra o resultado de uma partida friendly
  @Transactional
  public FriendlyMatch registerMatch(UUID tenantId, UUID sessionId, FriendlyMatchRequest request) {
    TeamGenerationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    if (!session.getTenantId().equals(tenantId)) {
      throw new SecurityException("Access denied");
    }

    FriendlyMatch match = new FriendlyMatch();
    match.setSessionId(sessionId);
    match.setCourtName(request.courtName());
    match.setHomeTeamIndex(request.homeTeamIndex());
    match.setAwayTeamIndex(request.awayTeamIndex());
    match.setHomeScore(request.homeScore());
    match.setAwayScore(request.awayScore());
    match.setWalkover(Boolean.TRUE.equals(request.walkover()));
    match.setWinnerTeamIndex(request.winnerTeamIndex());
    match.setPlayed(true);
    match.setStatus("finished");
    return friendlyMatchRepository.save(match);
  }

  // --- Métodos auxiliares privados ---

  private boolean isFriendlyMode(TeamGenerationSession session) {
    try {
      JsonNode rules = mapper.readTree(session.getRulesJson());
      return "friendly".equals(rules.get("mode").asText());
    } catch (Exception e) {
      return false;
    }
  }

  private JsonNode parseRules(String rulesJson) {
    if (rulesJson == null || rulesJson.isBlank()) {
      throw new IllegalStateException("Rules not found");
    }
    try {
      return mapper.readTree(rulesJson);
    } catch (Exception e) {
      throw new IllegalStateException("Error parsing session rules", e);
    }
  }

  private List<FriendlySessionDTO.CourtDTO> buildCourtList(UUID sessionId, JsonNode rules) {
    List<FriendlySessionDTO.CourtDTO> courts = new ArrayList<>();
    JsonNode courtsArray = rules.get("courts");
    if (courtsArray != null) {
      for (JsonNode courtNode : courtsArray) {
        String name = courtNode.get("name").asText();
        List<Integer> indices = new ArrayList<>();
        courtNode.get("teamIndices").forEach(t -> indices.add(t.asInt()));

        List<GeneratedTeam> teams = teamRepository.findAllBySessionIdAndTeamIndexIn(sessionId, indices);
        List<FriendlySessionDTO.TeamInfo> teamInfos = teams.stream().map(team -> {
          List<GeneratedTeamPlayer> players = teamPlayerRepository.findByTeamId(team.getId());
          double avg = players.stream()
                  .mapToDouble(p -> p.getScoreAtGeneration().doubleValue())
                  .average().orElse(0.0);
          int women = (int) players.stream()
                  .filter(p -> "F".equals(p.getSexAtGeneration())).count();
          List<String> names = players.stream()
                  .map(p -> playerRepository.findById(p.getPlayerId()).orElseThrow().getName())
                  .collect(Collectors.toList());
          String teamName = team.getName() != null ? team.getName() : "Team " + team.getTeamIndex();
          return new FriendlySessionDTO.TeamInfo(team.getTeamIndex(), teamName, avg, women, names);
        }).collect(Collectors.toList());

        courts.add(new FriendlySessionDTO.CourtDTO(name, teamInfos));
      }
    }
    return courts;
  }
}