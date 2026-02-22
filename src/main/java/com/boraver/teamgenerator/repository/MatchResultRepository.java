package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {
  List<MatchResult> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

  @Query("SELECT mr FROM MatchResult mr JOIN mr.winningTeam p WHERE p.id = :playerId AND mr.tenantId = :tenantId")
  List<MatchResult> findByPlayerIdAndTenantId(@Param("playerId") UUID playerId, @Param("tenantId") UUID tenantId);
}