package com.boraver.teamgenerator.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="generated_team_player")
public class GeneratedTeamPlayer {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name="team_id", nullable=false)
  private UUID teamId;

  @Column(name="player_id", nullable=false)
  private UUID playerId;

  @Column(name="sex_at_generation", nullable=false, length=1)
  private String sexAtGeneration;

  @Column(name="score_at_generation", nullable=false, precision=10, scale=4)
  private BigDecimal scoreAtGeneration;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name="snapshot_json", nullable=false, columnDefinition="jsonb")
  private JsonNode snapshotJson;

  public UUID getId() { return id; }

  public UUID getTeamId() { return teamId; }
  public void setTeamId(UUID teamId) { this.teamId = teamId; }

  public UUID getPlayerId() { return playerId; }
  public void setPlayerId(UUID playerId) { this.playerId = playerId; }

  public String getSexAtGeneration() { return sexAtGeneration; }
  public void setSexAtGeneration(String sexAtGeneration) { this.sexAtGeneration = sexAtGeneration; }

  public BigDecimal getScoreAtGeneration() { return scoreAtGeneration; }
  public void setScoreAtGeneration(BigDecimal scoreAtGeneration) { this.scoreAtGeneration = scoreAtGeneration; }

  public JsonNode getSnapshotJson() { return snapshotJson; }
  public void setSnapshotJson(JsonNode snapshotJson) { this.snapshotJson = snapshotJson; }
}