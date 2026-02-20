package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.dto.auth.*;
import com.boraver.teamgenerator.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register-tenant")
  public AuthResponse registerTenant(@Valid @RequestBody RegisterTenantRequest req) {
    var created = authService.registerTenantWithAdmin(
        req.tenantName(),
        req.tenantSlug(),
        req.adminName(),
        req.email(),
        req.password()
    );

    var login = authService.login(created.tenantId(), req.email(), req.password());

    return new AuthResponse(
        login.token(),
        created.tenantId().toString(),
        created.userId().toString(),
        created.role()
    );
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest req) {
    var result = authService.login(UUID.fromString(req.tenantId()), req.email(), req.password());
    return new AuthResponse(result.token(), result.tenantId().toString(), result.userId().toString(), result.role());
  }

  @PostMapping("/login-by-slug")
  public AuthResponse loginBySlug(@Valid @RequestBody LoginBySlugRequest req) {
    var result = authService.loginBySlug(req.tenantSlug(), req.email(), req.password());
    return new AuthResponse(
        result.token(),
        result.tenantId().toString(),
        result.userId().toString(),
        result.role()
    );
  }
}

