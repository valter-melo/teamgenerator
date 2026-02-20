package com.boraver.teamgenerator.security;

import com.boraver.teamgenerator.common.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(7);

    try {
      var claims = jwtService.parse(token).getBody();

      UUID userId = UUID.fromString(claims.getSubject());
      String role = claims.get("role", String.class);
      String tenantId = claims.get("tenant_id", String.class);

      TenantContext.setTenantId(tenantId);

      var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

      Authentication auth = new UsernamePasswordAuthenticationToken(
          userId, // 👈 PRINCIPAL É O UUID
          null,
          authorities
      );

      SecurityContextHolder.getContext().setAuthentication(auth);

    } catch (Exception e) {
      SecurityContextHolder.clearContext();
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }
}