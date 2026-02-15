package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.skill.packageManager.SkillPackageManager;
import net.ooder.sdk.skill.packageManager.model.*;
import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.skillcenter.service.SkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 技能服务SDK 0.7.0实现
 * 使用SkillPackageManager管理技能
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class SkillServiceSdk070Impl implements SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillServiceSdk070Impl.class);

    private final SdkConfig sdkConfig;
    private SkillPackageManager packageManager;

    public SkillServiceSdk070Impl(SdkConfig sdkConfig) {
        this.sdkConfig = sdkConfig;
    }

    @PostConstruct
    public void init() {
        try {
            String basePath = sdkConfig.getBasePath();
            String skillCenterUrl = sdkConfig.getSkillCenterUrl();
            
            packageManager = new net.ooder.sdk.skill.packageManager.impl.SkillPackageManagerImpl(
                basePath, skillCenterUrl
            );
            log.info("SkillPackageManager initialized with basePath: {}, skillCenterUrl: {}", 
                    basePath, skillCenterUrl);
        } catch (Exception e) {
            log.error("Failed to initialize SkillPackageManager: {}", e.getMessage(), e);
        }
    }

    @Override
    public PageResult<SkillDTO> getAllSkills(String category, String status, String keyword, 
                                              int pageNum, int pageSize) {
        try {
            List<InstalledSkill> installedSkills = packageManager.getInstalledSkills();
            
            List<SkillDTO> filtered = installedSkills.stream()
                .filter(skill -> category == null || category.isEmpty() || 
                        category.equals(getCategoryFromType(skill.getType())))
                .filter(skill -> status == null || status.isEmpty() || 
                        status.equals(skill.getStatus().name().toLowerCase()))
                .filter(skill -> keyword == null || keyword.isEmpty() ||
                        skill.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        skill.getSkillId().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            return paginate(filtered, pageNum, pageSize);
        } catch (Exception e) {
            log.error("Failed to get all skills: {}", e.getMessage(), e);
            return PageResult.empty();
        }
    }

    @Override
    public SkillDTO getSkillById(String skillId) {
        try {
            InstalledSkill skill = packageManager.getInstalledSkill(skillId);
            if (skill != null) {
                return convertToDTO(skill);
            }
            
            CompletableFuture<SkillPackage> future = packageManager.getSkillInfo(skillId);
            SkillPackage pkg = future.get();
            if (pkg != null) {
                return convertPackageToDTO(pkg);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get skill {}: {}", skillId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SkillDTO addSkill(SkillDTO skillDTO) {
        try {
            InstallRequest request = InstallRequest.builder()
                .skillId(skillDTO.getId())
                .mode(InstallMode.LOCAL_DEPLOYED)
                .build();
            
            CompletableFuture<InstallResult> future = packageManager.installSkill(request);
            InstallResult result = future.get();
            
            if (result.isSuccess()) {
                return getSkillById(skillDTO.getId());
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to add skill: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SkillDTO updateSkill(String skillId, SkillDTO skillDTO) {
        try {
            CompletableFuture<UpdateResult> future = packageManager.updateSkill(skillId, skillDTO.getVersion());
            UpdateResult result = future.get();
            
            if (result.isSuccess()) {
                return getSkillById(skillId);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to update skill {}: {}", skillId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteSkill(String skillId) {
        try {
            CompletableFuture<UninstallResult> future = packageManager.uninstallSkill(skillId);
            UninstallResult result = future.get();
            return result.isSuccess();
        } catch (Exception e) {
            log.error("Failed to delete skill {}: {}", skillId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean approveSkill(String skillId) {
        InstalledSkill skill = packageManager.getInstalledSkill(skillId);
        if (skill != null) {
            try {
                packageManager.startSkill(skillId).get();
                return true;
            } catch (Exception e) {
                log.error("Failed to approve skill {}: {}", skillId, e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean rejectSkill(String skillId) {
        InstalledSkill skill = packageManager.getInstalledSkill(skillId);
        if (skill != null) {
            try {
                packageManager.stopSkill(skillId).get();
                return true;
            } catch (Exception e) {
                log.error("Failed to reject skill {}: {}", skillId, e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    @Override
    public PageResult<SkillDTO> getMarketSkills(String category, String status, String keyword, 
                                                 int pageNum, int pageSize) {
        try {
            DiscoveryFilter.Builder builder = DiscoveryFilter.builder();
            
            if (category != null && !category.isEmpty()) {
                builder.type(inferTypeFromCategory(category));
            }
            if (keyword != null && !keyword.isEmpty()) {
                builder.keyword(keyword);
            }
            
            CompletableFuture<List<SkillPackage>> future = packageManager.discoverSkills(builder.build());
            List<SkillPackage> packages = future.get();
            
            List<SkillDTO> filtered = packages.stream()
                .filter(pkg -> status == null || status.isEmpty() || 
                        status.equals("available"))
                .map(this::convertPackageToDTO)
                .collect(Collectors.toList());

            return paginate(filtered, pageNum, pageSize);
        } catch (Exception e) {
            log.error("Failed to get market skills: {}", e.getMessage(), e);
            return PageResult.empty();
        }
    }

    @Override
    public SkillDTO addMarketSkill(SkillDTO skillDTO) {
        return addSkill(skillDTO);
    }

    @Override
    public SkillDTO updateMarketSkill(String skillId, SkillDTO skillDTO) {
        return updateSkill(skillId, skillDTO);
    }

    @Override
    public boolean removeMarketSkill(String skillId) {
        return deleteSkill(skillId);
    }

    @Override
    public int getSkillCount() {
        return packageManager.getInstalledSkills().size();
    }

    @Override
    public int getExecutionCount() {
        return 150;
    }

    @Override
    public int getSuccessfulExecutionCount() {
        return 120;
    }

    @Override
    public int getFailedExecutionCount() {
        return 30;
    }

    @Override
    public int getSharedSkillCount() {
        return (int) packageManager.getInstalledSkills().stream()
            .filter(skill -> skill.getStatus() == SkillStatus.RUNNING)
            .count();
    }

    private SkillDTO convertToDTO(InstalledSkill skill) {
        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getSkillId());
        dto.setName(skill.getName());
        dto.setVersion(skill.getVersion());
        dto.setType(skill.getType());
        dto.setStatus(skill.getStatus().name().toLowerCase());
        dto.setAvailable(skill.getStatus() == SkillStatus.RUNNING);
        dto.setCategory(getCategoryFromType(skill.getType()));
        
        if (skill.getCapabilities() != null) {
            dto.setCapabilities(skill.getCapabilities().stream()
                .map(Capability::getId)
                .collect(Collectors.toList()));
        }
        
        if (skill.getScenes() != null) {
            dto.setScenes(skill.getScenes().stream()
                .map(SceneInfo::getName)
                .collect(Collectors.toList()));
        }
        
        if (skill.getConnectionInfo() != null) {
            dto.setEndpoint(skill.getConnectionInfo().getEndpoint());
        }
        
        if (skill.getInstallTime() != null) {
            dto.setCreatedAt(Date.from(skill.getInstallTime()));
        }
        if (skill.getLastStartTime() != null) {
            dto.setUpdatedAt(Date.from(skill.getLastStartTime()));
        }
        
        return dto;
    }

    private SkillDTO convertPackageToDTO(SkillPackage pkg) {
        SkillDTO dto = new SkillDTO();
        dto.setId(pkg.getId());
        dto.setName(pkg.getName());
        dto.setVersion(pkg.getVersion());
        dto.setType(pkg.getType());
        dto.setDescription(pkg.getDescription());
        dto.setAuthor(pkg.getAuthor());
        dto.setLicense(pkg.getLicense());
        dto.setCategory(getCategoryFromType(pkg.getType()));
        dto.setDownloadUrl(pkg.getDownloadUrl());
        dto.setStatus("available");
        dto.setAvailable(true);
        
        if (pkg.getCapabilities() != null) {
            dto.setCapabilities(pkg.getCapabilities().stream()
                .map(Capability::getId)
                .collect(Collectors.toList()));
        }
        
        if (pkg.getScenes() != null) {
            dto.setScenes(pkg.getScenes().stream()
                .map(SceneInfo::getName)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private String getCategoryFromType(String type) {
        if (type == null) return "utilities";
        switch (type) {
            case "enterprise-skill": return "enterprise";
            case "tool-skill": return "development";
            case "integration-skill": return "integration";
            case "infrastructure-skill": return "infrastructure";
            default: return "utilities";
        }
    }

    private String inferTypeFromCategory(String category) {
        if (category == null) return "tool-skill";
        switch (category) {
            case "enterprise": return "enterprise-skill";
            case "development": return "tool-skill";
            case "integration": return "integration-skill";
            case "infrastructure": return "infrastructure-skill";
            default: return "tool-skill";
        }
    }

    private PageResult<SkillDTO> paginate(List<SkillDTO> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            return PageResult.empty();
        }

        List<SkillDTO> pageList = list.subList(start, end);
        return PageResult.of(pageList, total, pageNum, pageSize);
    }
}
