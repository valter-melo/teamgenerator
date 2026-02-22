package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.dto.match.MatchResultResponse;
import com.boraver.teamgenerator.dto.match.SaveMatchResultRequest;
import com.boraver.teamgenerator.model.MatchResult;
import com.boraver.teamgenerator.model.Player;
import com.boraver.teamgenerator.repository.MatchResultRepository;
import com.boraver.teamgenerator.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    if (winningTeam.size() != request.getWinningPlayerIds().size()) {
      throw new IllegalArgumentException("Alguns jogadores não foram encontrados");
    }

    MatchResult result = new MatchResult();
    result.setTenantId(tenantId);
    result.setWinningTeam(winningTeam);
    result.setTeamScore(request.getTeamScore());
    result.setOpponentScore(request.getOpponentScore());

    MatchResult saved = matchResultRepository.save(result);

    return mapToResponse(saved);
  }

  public List<MatchResultResponse> listResults(UUID tenantId) {
    return matchResultRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
        .stream().map(this::mapToResponse).toList();
  }

  private MatchResultResponse mapToResponse(MatchResult result) {
    return MatchResultResponse.builder()
        .id(result.getId())
        .createdAt(result.getCreatedAt())
        .winningPlayerIds(result.getWinningTeam().stream().map(Player::getId).collect(Collectors.toSet()))
        .teamScore(result.getTeamScore())
        .opponentScore(result.getOpponentScore())
        .build();
  }
}