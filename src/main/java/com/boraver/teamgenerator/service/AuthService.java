package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.model.*;
import com.boraver.teamgenerator.repository.*;
import com.boraver.teamgenerator.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

  private final TenantRepository tenantRepo;
  private final AppUserRepository userRepo;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  public AuthService(TenantRepository tenantRepo, AppUserRepository userRepo, PasswordEncoder encoder, JwtService jwt) {
    this.tenantRepo = tenantRepo;
    this.userRepo = userRepo;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  @Transactional
  public AuthCreated registerTenantWithAdmin(
      String tenantName,
      String tenantSlug,
      String adminName,
      String email,
      String password
  ) {
    String slug = normalizeSlug(tenantSlug);
    String normEmail = email.trim().toLowerCase();

    if (tenantRepo.existsBySlug(slug)) {
      throw new IllegalArgumentException("Já existe um grupo com esse slug: " + slug);
    }

    Tenant t = new Tenant();
    t.setName(tenantName.trim());
    t.setSlug(slug);
    t = tenantRepo.save(t);

    AppUser u = new AppUser();
    u.setTenantId(t.getId());
    u.setName(adminName.trim());
    u.setEmail(normEmail);
    u.setPasswordHash(encoder.encode(password));
    u.setRole("ADMIN");
    u.setActive(true);
    userRepo.save(u);

    return new AuthCreated(t.getId(), u.getId(), u.getRole(), u.getEmail());
  }

  private static String normalizeSlug(String raw) {
    String s = raw == null ? "" : raw.trim().toLowerCase();
    s = s.replaceAll("[^a-z0-9-]", "-");
    s = s.replaceAll("-{2,}", "-");
    s = s.replaceAll("(^-|-$)", "");
    if (s.length() < 3) throw new IllegalArgumentException("tenantSlug too short");
    return s;
  }


  public LoginResult login(UUID tenantId, String email, String password) {
    AppUser user = userRepo.findByTenantIdAndEmail(tenantId, email)
        .orElseThrow(() -> new SecurityException("Invalid credentials"));

    if (!user.isActive() || !encoder.matches(password, user.getPasswordHash())) {
      throw new SecurityException("Invalid credentials");
    }

    String token = jwt.generateToken(
        user.getId().toString(),
        user.getTenantId().toString(),
        user.getRole(),
        user.getEmail()
    );

    return new LoginResult(token, user.getTenantId(), user.getId(), user.getRole());
  }

  public LoginResult loginBySlug(String tenantSlug, String email, String password) {
    var slug = tenantSlug.trim().toLowerCase();
    var tenant = tenantRepo.findBySlug(slug)
        .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

    return login(tenant.getId(), email, password);
  }

  public record AuthCreated(UUID tenantId, UUID userId, String role, String email) {}
  public record LoginResult(String token, UUID tenantId, UUID userId, String role) {}
}