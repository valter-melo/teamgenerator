package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.entity.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
  List<Skill> findAllByTenantIdAndActiveTrue(UUID tenantId);
  Optional<Skill> findByIdAndTenantId(UUID id, UUID tenantId);
}
