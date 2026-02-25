package com.boraver.teamgenerator.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "match_results")
@Data
public class MatchResult {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @ManyToMany
  @JoinTable(
      name = "match_result_players",
      joinColumns = @JoinColumn(name = "match_result_id"),
      inverseJoinColumns = @JoinColumn(name = "player_id")
  )
  private Set<Player> winningTeam = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "match_losing_players",
      joinColumns = @JoinColumn(name = "match_result_id"),
      inverseJoinColumns = @JoinColumn(name = "player_id")
  )
  private Set<Player> losingTeam = new HashSet<>();

  @ManyToOne
  @JoinColumn(name = "team_generation_id")
  private TeamGenerationSession teamGenerationSession;

  private int teamScore; // pontuação do time vencedor no set final
  private int opponentScore; // pontuação do adversário
}