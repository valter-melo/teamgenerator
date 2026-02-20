package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.dto.skill.*;
import com.boraver.teamgenerator.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/skills")
public class SkillController {

  private final SkillService service;

  public SkillController(SkillService service) { this.service = service; }

  @PostMapping
  public SkillResponse create(@Valid @RequestBody CreateSkillRequest req) {
    return service.create(req);
  }

  @GetMapping
  public List<SkillResponse> listActive() {
    return service.listActive();
  }

  @GetMapping("/{id}")
  public SkillResponse get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  public SkillResponse update(@PathVariable UUID id, @RequestBody UpdateSkillRequest req) {
    return service.update(id, req);
  }
}
