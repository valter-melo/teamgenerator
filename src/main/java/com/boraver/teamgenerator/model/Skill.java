package com.boraver.teamgenerator.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "skill",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "code"})
)
public class Skill {

  @Id @GeneratedValue
  private UUID id;

  @Column(name="tenant_id", nullable=false)
  private UUID tenantId;

  @Column(nullable=false, length=40)
  private String code;

  @Column(nullable=false, length=120)
  private String name;

  @Column(nullable=false)
  private boolean active = true;

  @Column(name="created_at", nullable=false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  // ✅ ESTES DOIS RESOLVEM O ERRO
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
