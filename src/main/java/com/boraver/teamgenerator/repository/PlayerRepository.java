package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.entity.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
  List<Player> findAllByTenantIdAndActiveTrue(UUID tenantId, Sort sort);
  List<Player> findAllByTenantIdAndIdIn(UUID tenantId, List<UUID> ids);
  Optional<Player> findByIdAndTenantId(UUID id, UUID tenantId);
  List<Player> findAllByIdInAndTenantId(List<UUID> ids, UUID tenantId);
}
