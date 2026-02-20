package com.boraver.teamgenerator.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="player")
public class Player {
  @Id @GeneratedValue
  private UUID id;

  @Column(name="tenant_id", nullable=false)
  private UUID tenantId;

  @Column(nullable=false, length=120)
  private String name;

  @Column(nullable=false, length=1)
  private char sex; // M/F

  @Column(nullable=false)
  private boolean active = true;

  @Column(name="created_at", nullable=false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public char getSex() { return sex; }
  public void setSex(char sex) { this.sex = sex; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
