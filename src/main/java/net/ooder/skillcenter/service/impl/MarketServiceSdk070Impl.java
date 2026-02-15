package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.skill.packageManager.SkillPackageManager;
import net.ooder.sdk.skill.packageManager.model.*;
import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.skillcenter.dto.SkillReviewDTO;
import net.ooder.skillcenter.service.MarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 市场服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class MarketServiceSdk070Impl implements MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketServiceSdk070Impl.class);

    private final SdkConfig sdkConfig;
    private SkillPackageManager packageManager;
    private final Map<String, List<SkillReviewDTO>> reviewStore = new ConcurrentHashMap<>();
    private final AtomicLong reviewIdGenerator = new AtomicLong(1);

    public MarketServiceSdk070Impl(SdkConfig sdkConfig) {
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
            log.info("MarketService initialized with SkillPackageManager");
        } catch (Exception e) {
            log.error("Failed to initialize SkillPackageManager: {}", e.getMessage(), e);
        }
    }

    @Override
    public PageResult<SkillDTO> getMarketSkills(int page, int size, String sortBy, String sortDirection) {
        try {
            CompletableFuture<List<SkillPackage>> future = packageManager.discoverSkills(
                DiscoveryFilter.builder().build()
            );
            List<SkillPackage> packages = future.get();
            
            List<SkillDTO> skills = packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            skills = sortSkills(skills, sortBy, sortDirection);
            return paginate(skills, page, size);
        } catch (Exception e) {
            log.error("Failed to get market skills: {}", e.getMessage(), e);
            return PageResult.empty();
        }
    }

    @Override
    public SkillDTO getSkillDetails(String skillId) {
        try {
            CompletableFuture<SkillPackage> future = packageManager.getSkillInfo(skillId);
            SkillPackage pkg = future.get();
            if (pkg != null) {
                return convertToDTO(pkg);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get skill details {}: {}", skillId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public PageResult<SkillDTO> searchSkills(String keyword, int page, int size, String sortBy, String sortDirection) {
        try {
            DiscoveryFilter filter = DiscoveryFilter.builder()
                .keyword(keyword)
                .build();
            
            CompletableFuture<List<SkillPackage>> future = packageManager.discoverSkills(filter);
            List<SkillPackage> packages = future.get();
            
            List<SkillDTO> skills = packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            skills = sortSkills(skills, sortBy, sortDirection);
            return paginate(skills, page, size);
        } catch (Exception e) {
            log.error("Failed to search skills: {}", e.getMessage(), e);
            return PageResult.empty();
        }
    }

    @Override
    public List<String> getSkillCategories() {
        try {
            List<InstalledSkill> skills = packageManager.getInstalledSkills();
            return skills.stream()
                .map(s -> getCategoryFromType(s.getType()))
                .distinct()
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get categories: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public PageResult<SkillDTO> getSkillsByCategory(String category, int page, int size, String sortBy, String sortDirection) {
        try {
            String type = inferTypeFromCategory(category);
            DiscoveryFilter filter = DiscoveryFilter.builder()
                .type(type)
                .build();
            
            CompletableFuture<List<SkillPackage>> future = packageManager.discoverSkills(filter);
            List<SkillPackage> packages = future.get();
            
            List<SkillDTO> skills = packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            skills = sortSkills(skills, sortBy, sortDirection);
            return paginate(skills, page, size);
        } catch (Exception e) {
            log.error("Failed to get skills by category: {}", e.getMessage(), e);
            return PageResult.empty();
        }
    }

    @Override
    public List<SkillDTO> getPopularSkills(int limit) {
        try {
            CompletableFuture<List<SkillPackage>> future = packageManager.discoverSkills(
                DiscoveryFilter.builder().build()
            );
            List<SkillPackage> packages = future.get();
            
            return packages.stream()
                .sorted((a, b) -> Long.compare(b.getFileSize(), a.getFileSize()))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get popular skills: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<SkillDTO> getLatestSkills(int limit) {
        try {
            CompletableFuture<List<SkillPackage>> future = packageManager.discoverSkills(
                DiscoveryFilter.builder().build()
            );
            List<SkillPackage> packages = future.get();
            
            return packages.stream()
                .sorted((a, b) -> Long.compare(b.getFileSize(), a.getFileSize()))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get latest skills: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean rateSkill(String skillId, double rating, String comment, String userId) {
        SkillReviewDTO review = new SkillReviewDTO();
        review.setId("review-" + reviewIdGenerator.getAndIncrement());
        review.setSkillId(skillId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(new Date());
        reviewStore.computeIfAbsent(skillId, k -> new ArrayList<>()).add(review);
        return true;
    }

    @Override
    public PageResult<SkillReviewDTO> getSkillReviews(String skillId, int page, int size, String sortBy, String sortDirection) {
        List<SkillReviewDTO> reviews = reviewStore.getOrDefault(skillId, new ArrayList<>());
        return paginate(reviews, page, size);
    }

    @Override
    public byte[] downloadSkill(String skillId) {
        log.warn("Download skill {} - SDK 0.7.0 download not yet implemented", skillId);
        return new byte[0];
    }

    @Override
    public boolean publishSkill(SkillDTO skill) {
        log.info("Publish skill: {} - SDK 0.7.0 publish not yet implemented", skill.getName());
        return true;
    }

    @Override
    public boolean updateSkill(String skillId, SkillDTO skill) {
        try {
            CompletableFuture<UpdateResult> future = packageManager.updateSkill(skillId, skill.getVersion());
            UpdateResult result = future.get();
            return result.isSuccess();
        } catch (Exception e) {
            log.error("Failed to update skill {}: {}", skillId, e.getMessage(), e);
            return false;
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

    private SkillDTO convertToDTO(SkillPackage pkg) {
        SkillDTO dto = new SkillDTO();
        dto.setId(pkg.getId());
        dto.setName(pkg.getName());
        dto.setVersion(pkg.getVersion());
        dto.setType(pkg.getType());
        dto.setDescription(pkg.getDescription());
        dto.setAuthor(pkg.getAuthor());
        dto.setLicense(pkg.getLicense());
        dto.setDownloadUrl(pkg.getDownloadUrl());
        dto.setDownloadCount((int) pkg.getFileSize());
        dto.setCategory(getCategoryFromType(pkg.getType()));
        dto.setStatus("active");
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

    private List<SkillDTO> sortSkills(List<SkillDTO> skills, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) return skills;
        Comparator<SkillDTO> comparator;
        switch (sortBy) {
            case "name":
                comparator = Comparator.comparing(SkillDTO::getName);
                break;
            case "rating":
                comparator = Comparator.comparing(SkillDTO::getRating);
                break;
            case "downloadCount":
                comparator = Comparator.comparing(SkillDTO::getDownloadCount);
                break;
            default:
                comparator = Comparator.comparing(SkillDTO::getId);
        }
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        return skills.stream().sorted(comparator).collect(Collectors.toList());
    }

    private <T> PageResult<T> paginate(List<T> list, int page, int size) {
        int total = list.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        if (start >= total) return PageResult.empty();
        return PageResult.of(list.subList(start, end), total, page, size);
    }
}
