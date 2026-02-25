package com.boraver.teamgenerator.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="generated_team")
public class GeneratedTeam {
  @Id @GeneratedValue
  private UUID id;

  @Column(name="session_id", nullable=false)
  private UUID sessionId;

  @Column(name="team_index", nullable=false)
  private int teamIndex;

  @Column(nullable=false, length=120)
  private String name;

  public UUID getId() { return id; }
  public UUID getSessionId() { return sessionId; }
  public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
  public int getTeamIndex() { return teamIndex; }
  public void setTeamIndex(int teamIndex) { this.teamIndex = teamIndex; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}

