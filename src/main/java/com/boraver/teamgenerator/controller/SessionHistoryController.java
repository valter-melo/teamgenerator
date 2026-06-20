package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.service.SessionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/session-history")
@RequiredArgsConstructor
public class SessionHistoryController {

  private final SessionHistoryService service;

  @GetMapping
  public ResponseEntity<List<SessionHistoryService.SessionSummaryDTO>> listSessions() {
    return ResponseEntity.ok(service.getSessions());
  }

  @GetMapping("/{sessionId}")
  public ResponseEntity<SessionHistoryService.SessionDetailDTO> getSessionDetail(@PathVariable UUID sessionId) {
    return ResponseEntity.ok(service.getSessionDetail(sessionId));
  }

}