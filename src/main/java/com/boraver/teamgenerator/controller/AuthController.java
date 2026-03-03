package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.dto.auth.*;
import com.boraver.teamgenerator.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para login e registro de usuários")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register-tenant")
  @Operation(summary = "Registrar novo usuário")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resgistro bem sucedido"),
      @ApiResponse(responseCode = "4400", description = "Email já cadastrado")
  })
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
  @Operation(summary = "Login de usuário")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
      @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
  })
  public AuthResponse login(@Valid @RequestBody LoginRequest req) {
    var result = authService.login(UUID.fromString(req.tenantId()), req.email(), req.password());
    return new AuthResponse(result.token(), result.tenantId().toString(), result.userId().toString(), result.role());
  }

  @PostMapping("/login-by-slug")
  @Operation(summary = "Login de usuário com tenant")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
      @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
  })
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