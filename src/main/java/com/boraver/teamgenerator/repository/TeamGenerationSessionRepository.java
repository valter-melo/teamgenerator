package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.entity.GameSession;
import com.boraver.teamgenerator.entity.TeamGenerationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TeamGenerationSessionRepository extends JpaRepository<TeamGenerationSession, UUID> {

  @Query("SELECT s FROM GameSession s WHERE s.tenantId = :tenantId AND s.active = true")
  Optional<GameSession> findActiveSessionByTenantId(@Param("tenantId") UUID tenantId);

  @Query("SELECT s FROM TeamGenerationSession s WHERE s.gameSession.id = :gameSessionId")
  Optional<TeamGenerationSession> findByGameSessionId(@Param("gameSessionId") UUID gameSessionId);
}