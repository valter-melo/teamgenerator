package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.dto.match.MatchResultResponse;
import com.boraver.teamgenerator.dto.match.SaveMatchResultRequest;
import com.boraver.teamgenerator.service.MatchResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    UUID tenantId = (UUID) auth.getPrincipal(); // ajuste conforme seu método de obter tenant
    return matchResultService.saveResult(tenantId, request);
  }

  @GetMapping
  public List<MatchResultResponse> listResults(Authentication auth) {
    UUID tenantId = (UUID) auth.getPrincipal();
    return matchResultService.listResults(tenantId);
  }
}