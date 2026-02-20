package com.boraver.teamgenerator.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="team_generation_session")
public class TeamGenerationSession {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name="tenant_id", nullable=false)
  private UUID tenantId;

  @Column(nullable=false, length=10)
  private String mode; // TXT / DB

  @Column(name="created_by")
  private UUID createdBy;

  @Column(name="created_at", nullable=false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  @Column(name="team_count", nullable=false)
  private int teamCount;

  @Column(name="players_per_team", nullable=false)
  private int playersPerTeam;

  @Column(name="players_count", nullable=false)
  private int playersCount;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name="rules_json", nullable=false, columnDefinition="jsonb")
  private JsonNode rulesJson; // <-- trocou aqui

  @Column(name="source_file_name")
  private String sourceFileName;

  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getMode() { return mode; }
  public void setMode(String mode) { this.mode = mode; }

  public UUID getCreatedBy() { return createdBy; }
  public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

  public int getTeamCount() { return teamCount; }
  public void setTeamCount(int teamCount) { this.teamCount = teamCount; }

  public int getPlayersPerTeam() { return playersPerTeam; }
  public void setPlayersPerTeam(int playersPerTeam) { this.playersPerTeam = playersPerTeam; }

  public int getPlayersCount() { return playersCount; }
  public void setPlayersCount(int playersCount) { this.playersCount = playersCount; }

  public JsonNode getRulesJson() { return rulesJson; }
  public void setRulesJson(JsonNode rulesJson) { this.rulesJson = rulesJson; }

  public String getSourceFileName() { return sourceFileName; }
  public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }
}