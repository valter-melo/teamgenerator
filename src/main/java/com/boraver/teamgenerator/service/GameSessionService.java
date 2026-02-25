package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.entity.GameSession;
import com.boraver.teamgenerator.entity.TeamGenerationSession;
import com.boraver.teamgenerator.repository.GameSessionRepository;
import com.boraver.teamgenerator.repository.TeamGenerationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameSessionService {
  private final GameSessionRepository gameSessionRepository;
  private final TeamGenerationSessionRepository teamGenerationSessionRepository;

  @Transactional
  public GameSession startSession(UUID tenantId, UUID teamGenerationId) {
    if (gameSessionRepository.findByTenantIdAndActiveTrue(tenantId).isPresent()) {
      throw new RuntimeException("Já existe uma sessão ativa para este tenant");
    }

    TeamGenerationSession teamGen = teamGenerationSessionRepository.findById(teamGenerationId)
        .orElseThrow(() -> new RuntimeException("Team generation session não encontrada"));

    GameSession session = new GameSession();
    session.setTenantId(tenantId);
    session.setStartedAt(LocalDateTime.now());
    session.setActive(true);
    gameSessionRepository.save(session);

    teamGen.setGameSession(session);
    teamGenerationSessionRepository.save(teamGen);

    return session;
  }

  @Transactional
  public void endSession(UUID tenantId) {
    GameSession gameSession = gameSessionRepository.findByTenantIdAndActiveTrue(tenantId)
        .orElseThrow(() -> new RuntimeException("Nenhuma sessão ativa encontrada"));
    gameSession.setActive(false);
    gameSession.setEndedAt(LocalDateTime.now());
    gameSessionRepository.save(gameSession);
  }

  public boolean hasActiveSession(UUID tenantId) {
    return gameSessionRepository.findByTenantIdAndActiveTrue(tenantId).isPresent();
  }
}
