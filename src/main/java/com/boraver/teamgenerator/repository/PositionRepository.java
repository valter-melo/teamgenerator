package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {
  List<Position> findAllByTenantIdAndActiveTrue(UUID tenantId);
  Optional<Position> findByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
  long countByTenantIdAndActiveTrue(UUID tenantId);
  Optional<Position> findByIdAndTenantId(UUID id, UUID tenantId);
}