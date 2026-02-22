package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {
  List<MatchResult> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

  @Query("SELECT mr FROM MatchResult mr JOIN mr.winningTeam p WHERE p.id = :playerId AND mr.tenantId = :tenantId")
  List<MatchResult> findByPlayerIdAndTenantId(@Param("playerId") UUID playerId, @Param("tenantId") UUID tenantId);

  @Query(value = """
    WITH player_arrays AS (
        SELECT
            mr.id,
            array_agg(mrp.player_id ORDER BY mrp.player_id) AS player_ids
        FROM match_results mr
        JOIN match_result_players mrp ON mr.id = mrp.match_result_id
        WHERE mr.tenant_id = :tenantId
          AND DATE(mr.created_at) = :date
        GROUP BY mr.id
    )
    SELECT player_ids, COUNT(*) AS wins
    FROM player_arrays
    GROUP BY player_ids
    ORDER BY wins DESC
    """, nativeQuery = true)
  List<Object[]> getTeamStats(@Param("tenantId") UUID tenantId, @Param("date") LocalDate date);
}