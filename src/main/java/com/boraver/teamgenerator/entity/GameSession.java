package com.boraver.teamgenerator.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_sessions")
@Data
@Getter
@Setter
public class GameSession {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  private LocalDateTime startedAt;

  private LocalDateTime endedAt;

  private boolean active = true;
}
