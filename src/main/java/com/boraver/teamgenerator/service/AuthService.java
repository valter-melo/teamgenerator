package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.entity.*;
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

  public AuthService(TenantRepository tenantRepo, AppUserRepository userRepo,
                     PasswordEncoder encoder, JwtService jwt) {
    this.tenantRepo = tenantRepo;
    this.userRepo = userRepo;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  @Transactional
  public LoginResult registerTenantWithAdmin(
    String tenantName,
    String tenantSlug,
    String adminName,
    String email,
    String password) {

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

    String token = jwt.generateToken(
      u.getId().toString(),
      u.getTenantId().toString(),
      u.getRole(),
      u.getEmail(),
      u.getName());

    return buildLoginResult(token, u, t);
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
      user.getEmail(),
      user.getName());

    Tenant tenant = tenantRepo.findById(tenantId)
      .orElseThrow(() -> new IllegalStateException("Tenant não encontrado"));

    return buildLoginResult(token, user, tenant);
  }

  public LoginResult loginBySlug(String tenantSlug, String email, String password) {
    var slug = tenantSlug.trim().toLowerCase();
    var tenant = tenantRepo.findBySlug(slug)
      .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

    return login(tenant.getId(), email, password);
  }

  private LoginResult buildLoginResult(String token, AppUser user, Tenant tenant) {
    return new LoginResult(
      token,
      user.getTenantId(),
      user.getId(),
      user.getRole(),
      user.getName(),
      tenant.getLogoUrl(),
      tenant.getPrimaryColor(),
      tenant.getSecondaryColor());
  }

  private static String normalizeSlug(String raw) {
    String s = raw == null ? "" : raw.trim().toLowerCase();
    s = s.replaceAll("[^a-z0-9-]", "-");
    s = s.replaceAll("-{2,}", "-");
    s = s.replaceAll("(^-|-$)", "");
    if (s.length() < 3) throw new IllegalArgumentException("tenantSlug too short");
    return s;
  }

  // ==================== Records ====================

  public record LoginResult(
    String token,
    UUID tenantId,
    UUID userId,
    String role,
    String userName,
    String logoUrl,
    String primaryColor,
    String secondaryColor) {}
}