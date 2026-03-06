package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.dto.skill.*;
import com.boraver.teamgenerator.entity.Skill;
import com.boraver.teamgenerator.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SkillService {
  private final SkillRepository repo;

  public SkillService(SkillRepository repo) { this.repo = repo; }

  private UUID tenantId() {
    return UUID.fromString(Objects.requireNonNull(TenantContext.getTenantId(), "tenant missing"));
  }

  @Transactional
  public SkillResponse create(CreateSkillRequest req) {
    Skill s = new Skill();
    s.setTenantId(tenantId());
    s.setName(req.name().trim());
    s = repo.save(s);
    return toResponse(s);
  }

  public List<SkillResponse> listActive() {
    return repo.findAllByTenantIdAndActiveTrue(tenantId()).stream().map(this::toResponse).toList();
  }

  public SkillResponse get(UUID id) {
    Skill s = repo.findByIdAndTenantId(id, tenantId()).orElseThrow(() -> new IllegalArgumentException("Skill not found"));
    return toResponse(s);
  }

  @Transactional
  public SkillResponse update(UUID id, UpdateSkillRequest req) {
    Skill s = repo.findByIdAndTenantId(id, tenantId()).orElseThrow(() -> new IllegalArgumentException("Skill not found"));
    if (req.active() != null) s.setActive(req.active());
    return toResponse(repo.save(s));
  }

  private SkillResponse toResponse(Skill s) {
    return new SkillResponse(s.getId(), s.getName(), s.isActive());
  }
}
