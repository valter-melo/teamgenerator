package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.model.GameSession;
import com.boraver.teamgenerator.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameSessionService {
  private final GameSessionRepository repository;

  @Transactional
  public GameSession startSession(UUID tenantId) {
    // Se já existir uma sessão ativa, retorna ela ou lança exceção? Melhor lançar erro.
    Optional<GameSession> existing = repository.findByTenantIdAndActiveTrue(tenantId);
    if (existing.isPresent()) {
      throw new IllegalStateException("Já existe uma sessão de jogos em andamento.");
    }
    GameSession session = new GameSession();
    session.setTenantId(tenantId);
    session.setStartedAt(LocalDateTime.now());
    session.setActive(true);
    return repository.save(session);
  }

  @Transactional
  public void endSession(UUID tenantId) {
    Optional<GameSession> session = repository.findByTenantIdAndActiveTrue(tenantId);
    session.ifPresent(s -> {
      s.setActive(false);
      s.setEndedAt(LocalDateTime.now());
    });
  }

  public boolean hasActiveSession(UUID tenantId) {
    return repository.findByTenantIdAndActiveTrue(tenantId).isPresent();
  }
}
