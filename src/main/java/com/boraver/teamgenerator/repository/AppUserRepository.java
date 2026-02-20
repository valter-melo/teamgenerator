package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.model.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
  Optional<AppUser> findByTenantIdAndEmail(UUID tenantId, String email);
}
