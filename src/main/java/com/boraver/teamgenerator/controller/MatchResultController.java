package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.common.TenantContext;
import com.boraver.teamgenerator.dto.match.MatchResultResponse;
import com.boraver.teamgenerator.dto.match.SaveMatchResultRequest;
import com.boraver.teamgenerator.dto.match.TeamStats;
import com.boraver.teamgenerator.service.MatchResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/match-results")
@RequiredArgsConstructor
public class MatchResultController {

  private final MatchResultService matchResultService;

  @PostMapping
  public MatchResultResponse saveResult(
      @Valid @RequestBody SaveMatchResultRequest request,
      Authentication auth
  ) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    return matchResultService.saveResult(tenantId, request);
  }

  @GetMapping
  public List<MatchResultResponse> listResults(Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    return matchResultService.listResults(tenantId);
  }

  @GetMapping("/stats")
  public List<TeamStats> getStats(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      Authentication auth
  ) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    LocalDate targetDate = date != null ? date : LocalDate.now();
    return matchResultService.getTeamStats(tenantId, targetDate);
  }

  @GetMapping("/history")
  public List<MatchResultResponse> getHistory(Authentication auth) {
    UUID tenantId = UUID.fromString(TenantContext.getTenantId());
    return matchResultService.getHistory(tenantId);
  }
}