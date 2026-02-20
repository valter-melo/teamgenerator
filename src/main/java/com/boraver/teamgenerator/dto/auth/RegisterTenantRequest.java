package com.boraver.teamgenerator.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record RegisterTenantRequest(
    @NotBlank String tenantName,

    @NotBlank
    @Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "Slug inválido. Use letras minúsculas, números e hífen (ex: boraver, bora-ver)."
    )
    String tenantSlug,

    @NotBlank String adminName,
    @Email @NotBlank String email,
    @Size(min = 6) String password
) {}
