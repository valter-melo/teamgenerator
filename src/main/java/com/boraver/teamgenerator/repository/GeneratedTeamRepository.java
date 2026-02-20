package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.model.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface GeneratedTeamRepository extends JpaRepository<GeneratedTeam, UUID> {
  List<GeneratedTeam> findAllBySessionIdOrderByTeamIndexAsc(UUID sessionId);
}