package com.boraver.teamgenerator.repository;

import com.boraver.teamgenerator.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface GeneratedTeamPlayerRepository extends JpaRepository<GeneratedTeamPlayer, UUID> {

  @Query("""
    select gtp from GeneratedTeamPlayer gtp
    where gtp.teamId in :teamIds
  """)
  List<GeneratedTeamPlayer> findAllByTeamIds(@Param("teamIds") List<UUID> teamIds);
}
