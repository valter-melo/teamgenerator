package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.dto.match.MatchResultResponse;
import com.boraver.teamgenerator.dto.match.SaveMatchResultRequest;
import com.boraver.teamgenerator.dto.match.TeamStats;
import com.boraver.teamgenerator.model.MatchResult;
import com.boraver.teamgenerator.model.Player;
import com.boraver.teamgenerator.repository.MatchResultRepository;
import com.boraver.teamgenerator.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchResultService {

  private final MatchResultRepository matchResultRepository;
  private final PlayerRepository playerRepository;

  @Transactional
  public MatchResultResponse saveResult(UUID tenantId, SaveMatchResultRequest request) {
    Set<Player> winningTeam = playerRepository.findAllById(request.getWinningPlayerIds())
        .stream().collect(Collectors.toSet());
    Set<Player> losingTeam = playerRepository.findAllById(request.getLosingPlayerIds())
        .stream().collect(Collectors.toSet());

    if (winningTeam.size() != request.getWinningPlayerIds().size() ||
        losingTeam.size() != request.getLosingPlayerIds().size()) {
      throw new IllegalArgumentException("Alguns jogadores não foram encontrados");
    }

    MatchResult result = new MatchResult();
    result.setTenantId(tenantId);
    result.setWinningTeam(winningTeam);
    result.setLosingTeam(losingTeam);
    result.setTeamScore(request.getTeamScore());
    result.setOpponentScore(request.getOpponentScore());

    MatchResult saved = matchResultRepository.save(result);
    return mapToResponse(saved);
  }

  public List<MatchResultResponse> listResults(UUID tenantId) {
    return matchResultRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
        .stream().map(this::mapToResponse).toList();
  }

  public List<TeamStats> getTeamStats(UUID tenantId, LocalDate date) {
    List<Object[]> results = matchResultRepository.getTeamStats(tenantId, date);
    return results.stream()
        .map(row -> new TeamStats(
            convertSqlArrayToList(row[0]),
            ((Number) row[1]).longValue()
        ))
        .toList();
  }

  public List<MatchResultResponse> getHistory(UUID tenantId) {
    return matchResultRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  private List<UUID> convertSqlArrayToList(Object arrayObj) {
    return switch (arrayObj) {
      case null -> Collections.emptyList();

      // Se for um array Java (UUID[] ou Object[])
      case UUID[] uuids -> Arrays.asList(uuids);
      case Object[] objects -> Arrays.stream(objects)
          .map(obj -> UUID.fromString(obj.toString()))
          .collect(Collectors.toList());
      default -> throw new IllegalArgumentException("Tipo inesperado: " + arrayObj.getClass());
    };
  }

  private MatchResultResponse mapToResponse(MatchResult result) {
    return MatchResultResponse.builder()
        .id(result.getId())
        .createdAt(result.getCreatedAt())
        .winningPlayerIds(result.getWinningTeam().stream().map(Player::getId).collect(Collectors.toSet()))
        .losingPlayerIds(result.getLosingTeam().stream().map(Player::getId).collect(Collectors.toSet()))
        .teamScore(result.getTeamScore())
        .opponentScore(result.getOpponentScore())
        .build();
  }
}