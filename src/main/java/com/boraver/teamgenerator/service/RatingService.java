package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.dto.rating.*;
import com.boraver.teamgenerator.model.PlayerSkillRating;
import com.boraver.teamgenerator.repository.PlayerRepository;
import com.boraver.teamgenerator.repository.RatingRepository;
import com.boraver.teamgenerator.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RatingService {
  private final RatingRepository ratingRepo;
  private final PlayerRepository playerRepo;
  private final SkillRepository skillRepo;

  public RatingService(RatingRepository ratingRepo, PlayerRepository playerRepo, SkillRepository skillRepo) {
    this.ratingRepo = ratingRepo;
    this.playerRepo = playerRepo;
    this.skillRepo = skillRepo;
  }

  private UUID tenantId() {
    return UUID.fromString(Objects.requireNonNull(TenantContext.getTenantId(), "tenant missing"));
  }

  @Transactional
  public void upsert(UpsertRatingsRequest req) {
    UUID tenant = tenantId();

    playerRepo.findByIdAndTenantId(req.playerId(), tenant)
        .orElseThrow(() -> new IllegalArgumentException("Player not found"));

    for (var sr : req.ratings()) {
      skillRepo.findByIdAndTenantId(sr.skillId(), tenant)
          .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + sr.skillId()));

      short newRating = (short) sr.rating();

      var currentOpt = ratingRepo.findCurrent(tenant, req.playerId(), sr.skillId());
      if (currentOpt.isPresent() && currentOpt.get().getRating() == newRating) {
        continue;
      }

      ratingRepo.closeCurrent(tenant, req.playerId(), sr.skillId());

      PlayerSkillRating r = new PlayerSkillRating();
      r.setTenantId(tenant);
      r.setPlayerId(req.playerId());
      r.setSkillId(sr.skillId());
      r.setRating(newRating);
      ratingRepo.save(r);
    }
  }

  public List<PlayerSkillRating> listAllPlayerRatings(UUID playerId) {
    UUID tenant = tenantId();
    playerRepo.findByIdAndTenantId(playerId, tenant)
        .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    return ratingRepo.findAllByPlayer(tenant, playerId);
  }

  public Map<UUID, Integer> currentRatingsMap(UUID playerId) {
    UUID tenant = tenantId();
    var all = ratingRepo.findAllByPlayer(tenant, playerId);
    Map<UUID, Integer> current = new HashMap<>();
    for (var r : all) {
      if (r.getValidTo() == null) current.put(r.getSkillId(), (int) r.getRating());
    }
    return current;
  }
}
