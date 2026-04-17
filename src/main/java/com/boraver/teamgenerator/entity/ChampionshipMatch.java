package com.boraver.teamgenerator.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "championship_matches")
@Data
public class ChampionshipMatch {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(length = 36)
  private UUID id;

  @Column(name = "championship_id", nullable = false)
  @JdbcTypeCode(SqlTypes.CHAR)
  private UUID championshipId;

  @Column(name = "group_index")
  private Integer groupIndex; // grupo da partida (null se fase eliminatória)

  @Column(name = "round", nullable = false)
  private int round; // rodada (1,2,3...)

  @Column(name = "home_team_index", nullable = false)
  private int homeTeamIndex; // índice do time mandante

  @Column(name = "away_team_index", nullable = false)
  private int awayTeamIndex; // índice do time visitante

  @Column(name = "home_score")
  private Integer homeScore; // gols/placar

  @Column(name = "away_score")
  private Integer awayScore;

  @Column(name = "played")
  private boolean played = false;

  @Column(name = "match_date")
  private LocalDateTime matchDate; // opcional

  @Column(name = "winner_team_index")
  private Integer winnerTeamIndex; // resultado

  @Column(name = "match_result_id")
  @JdbcTypeCode(SqlTypes.CHAR)
  private UUID matchResultId;

  @Column(nullable = false)
  private String status = "pending";

  @Column(name = "stage")
  private String stage;
}