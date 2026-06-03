package com.boraver.teamgenerator.service;

import com.boraver.teamgenerator.dto.tenant.TenantSettingsDTO;
import com.boraver.teamgenerator.dto.tenant.UpdateTenantSettingsRequest;
import com.boraver.teamgenerator.entity.Tenant;
import com.boraver.teamgenerator.repository.TenantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {
  private final TenantRepository tenantRepository;

  public TenantSettingsDTO getSettings(UUID tenantId) {
    Tenant t = tenantRepository.findById(tenantId).orElseThrow();
    return new TenantSettingsDTO(t.getLogoUrl(), t.getPrimaryColor(), t.getSecondaryColor());
  }

  @Transactional
  public TenantSettingsDTO updateSettings(UUID tenantId, UpdateTenantSettingsRequest req) {
    Tenant t = tenantRepository.findById(tenantId).orElseThrow();
    if (req.primaryColor() != null) t.setPrimaryColor(req.primaryColor());
    if (req.secondaryColor() != null) t.setSecondaryColor(req.secondaryColor());
    tenantRepository.save(t);
    return getSettings(tenantId);
  }

  @Transactional
  public String updateLogo(UUID tenantId, MultipartFile file) throws IOException {
    String fileName = "tenant-" + tenantId + "-" + System.currentTimeMillis() + ".png";
    Path uploadPath = Paths.get("uploads/logos");
    if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
    Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    String logoUrl = "/uploads/logos/" + fileName;

    Tenant t = tenantRepository.findById(tenantId).orElseThrow();
    t.setLogoUrl(logoUrl);
    tenantRepository.save(t);
    return logoUrl;
  }
}