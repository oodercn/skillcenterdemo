package net.ooder.nexus.skillcenter.controller;

import net.ooder.nexus.skillcenter.dto.skill.*;
import net.ooder.nexus.skillcenter.model.ResultModel;
import net.ooder.skillcenter.market.SkillListing;
import net.ooder.skillcenter.market.SkillMarketManager;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能发现API控制器 - 符合v0.7.0协议规范
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
public class SkillDiscoveryController extends BaseController {

    private final SkillMarketManager marketManager;

    public SkillDiscoveryController() {
        this.marketManager = SkillMarketManager.getInstance();
    }

    @PostMapping("/discovery/skills/list")
    public ResultModel<SkillSearchResultDTO> listSkills(@RequestBody SkillSearchDTO search) {
        
        long startTime = System.currentTimeMillis();
        logRequestStart("listSkills", search);

        try {
            List<SkillListing> allSkills = marketManager.getAllSkills();
            
            String type = search.getTypes() != null && !search.getTypes().isEmpty() 
                    ? search.getTypes().get(0) : null;
            String capability = search.getCapabilities() != null && !search.getCapabilities().isEmpty() 
                    ? search.getCapabilities().get(0) : null;
            String scene = search.getScenes() != null && !search.getScenes().isEmpty() 
                    ? search.getScenes().get(0) : null;
            int page = search.getPageNum() > 0 ? search.getPageNum() : 1;
            int size = search.getPageSize() > 0 ? search.getPageSize() : 20;
            
            List<SkillListing> filtered = allSkills.stream()
                    .filter(s -> type == null || type.isEmpty() || type.equals(s.getType()))
                    .filter(s -> capability == null || capability.isEmpty() || 
                            (s.getCapabilities() != null && s.getCapabilities().contains(capability)))
                    .filter(s -> scene == null || scene.isEmpty() || 
                            (s.getScenes() != null && s.getScenes().contains(scene)))
                    .collect(Collectors.toList());

            int total = filtered.size();
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, total);
            
            List<SkillSearchResultDTO.SkillListItemDTO> items = new ArrayList<>();
            if (fromIndex < total) {
                List<SkillListing> paged = filtered.subList(fromIndex, toIndex);
                items = paged.stream()
                        .map(this::convertToListItem)
                        .collect(Collectors.toList());
            }

            SkillSearchResultDTO result = new SkillSearchResultDTO();
            result.setTotal(total);
            result.setSkills(items);

            logRequestEnd("listSkills", total + " skills", System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("listSkills", e);
            return ResultModel.error(500, "获取技能列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/discovery/skills/get")
    public ResultModel<SkillManifestDTO> getSkill(@RequestBody SkillIdDTO request) {
        long startTime = System.currentTimeMillis();
        logRequestStart("getSkill", request);

        try {
            String id = request.getSkillId();
            SkillListing listing = marketManager.getSkillListing(id);
            if (listing == null) {
                logRequestEnd("getSkill", "Not found", System.currentTimeMillis() - startTime);
                return ResultModel.notFound("技能不存在");
            }

            SkillManifestDTO manifest = convertToManifest(listing);
            logRequestEnd("getSkill", manifest.getMetadata().getName(), System.currentTimeMillis() - startTime);
            return ResultModel.success(manifest);
        } catch (Exception e) {
            logRequestError("getSkill", e);
            return ResultModel.error(500, "获取技能失败: " + e.getMessage());
        }
    }

    @PostMapping("/discovery/skills/search")
    public ResultModel<SkillSearchResultDTO> searchSkills(@RequestBody SkillSearchDTO search) {
        long startTime = System.currentTimeMillis();
        logRequestStart("searchSkills", search);

        try {
            List<SkillListing> allSkills = marketManager.getAllSkills();
            
            List<SkillListing> filtered = allSkills.stream()
                    .filter(s -> filterByCapabilities(s, search.getCapabilities()))
                    .filter(s -> filterByScenes(s, search.getScenes()))
                    .filter(s -> filterByTypes(s, search.getTypes()))
                    .filter(s -> filterByKeywords(s, search.getKeywords()))
                    .filter(s -> filterByVersion(s, search.getVersion()))
                    .collect(Collectors.toList());

            int total = filtered.size();
            int fromIndex = (search.getPageNum() - 1) * search.getPageSize();
            int toIndex = Math.min(fromIndex + search.getPageSize(), total);
            
            List<SkillSearchResultDTO.SkillListItemDTO> items = new ArrayList<>();
            if (fromIndex < total) {
                List<SkillListing> paged = filtered.subList(fromIndex, toIndex);
                items = paged.stream()
                        .map(this::convertToListItem)
                        .collect(Collectors.toList());
            }

            SkillSearchResultDTO result = new SkillSearchResultDTO();
            result.setTotal(total);
            result.setSkills(items);

            logRequestEnd("searchSkills", total + " skills found", System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("searchSkills", e);
            return ResultModel.error(500, "搜索技能失败: " + e.getMessage());
        }
    }

    @PostMapping("/discovery/skills/download")
    public ResultModel<DownloadResultDTO> downloadSkill(@RequestBody SkillIdDTO request) {
        long startTime = System.currentTimeMillis();
        logRequestStart("downloadSkill", request);

        try {
            String id = request.getSkillId();
            SkillListing listing = marketManager.getSkillListing(id);
            if (listing == null) {
                logRequestEnd("downloadSkill", "Not found", System.currentTimeMillis() - startTime);
                return ResultModel.notFound("技能不存在");
            }

            byte[] skillData = marketManager.downloadSkill(id);
            
            DownloadResultDTO result = new DownloadResultDTO();
            result.setSuccess(true);
            result.setSkillId(id);
            result.setFilename(id + "-" + listing.getVersion() + ".zip");
            result.setSize(skillData != null ? skillData.length : 0);
            
            logRequestEnd("downloadSkill", result.getSize() + " bytes", System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("downloadSkill", e);
            return ResultModel.error(500, "下载技能失败: " + e.getMessage());
        }
    }

    private boolean filterByCapabilities(SkillListing s, List<String> capabilities) {
        if (capabilities == null || capabilities.isEmpty()) {
            return true;
        }
        if (s.getCapabilities() == null) {
            return false;
        }
        return capabilities.stream().anyMatch(c -> s.getCapabilities().contains(c));
    }

    private boolean filterByScenes(SkillListing s, List<String> scenes) {
        if (scenes == null || scenes.isEmpty()) {
            return true;
        }
        if (s.getScenes() == null) {
            return false;
        }
        return scenes.stream().anyMatch(scene -> s.getScenes().contains(scene));
    }

    private boolean filterByTypes(SkillListing s, List<String> types) {
        if (types == null || types.isEmpty()) {
            return true;
        }
        return types.contains(s.getType());
    }

    private boolean filterByKeywords(SkillListing s, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }
        String name = s.getName() != null ? s.getName().toLowerCase() : "";
        String desc = s.getDescription() != null ? s.getDescription().toLowerCase() : "";
        return keywords.stream().anyMatch(k -> 
                name.contains(k.toLowerCase()) || desc.contains(k.toLowerCase()));
    }

    private boolean filterByVersion(SkillListing s, String version) {
        if (version == null || version.isEmpty()) {
            return true;
        }
        String skillVersion = s.getVersion();
        if (skillVersion == null) {
            return false;
        }
        if (version.startsWith(">=")) {
            return compareVersions(skillVersion, version.substring(2)) >= 0;
        } else if (version.startsWith(">")) {
            return compareVersions(skillVersion, version.substring(1)) > 0;
        } else if (version.startsWith("<=")) {
            return compareVersions(skillVersion, version.substring(2)) <= 0;
        } else if (version.startsWith("<")) {
            return compareVersions(skillVersion, version.substring(1)) < 0;
        } else if (version.startsWith("^")) {
            return isCompatibleVersion(skillVersion, version.substring(1));
        } else if (version.startsWith("~")) {
            return isSameMinorVersion(skillVersion, version.substring(1));
        }
        return skillVersion.equals(version);
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int len = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < len; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    private boolean isCompatibleVersion(String actual, String required) {
        String[] actualParts = actual.split("\\.");
        String[] requiredParts = required.split("\\.");
        if (actualParts.length < 1 || requiredParts.length < 1) {
            return false;
        }
        int majorActual = Integer.parseInt(actualParts[0]);
        int majorRequired = Integer.parseInt(requiredParts[0]);
        if (majorActual != majorRequired) {
            return false;
        }
        return compareVersions(actual, required) >= 0;
    }

    private boolean isSameMinorVersion(String actual, String required) {
        String[] actualParts = actual.split("\\.");
        String[] requiredParts = required.split("\\.");
        if (actualParts.length < 2 || requiredParts.length < 2) {
            return false;
        }
        int majorActual = Integer.parseInt(actualParts[0]);
        int majorRequired = Integer.parseInt(requiredParts[0]);
        int minorActual = Integer.parseInt(actualParts[1]);
        int minorRequired = Integer.parseInt(requiredParts[1]);
        return majorActual == majorRequired && minorActual == minorRequired && 
               compareVersions(actual, required) >= 0;
    }

    private SkillSearchResultDTO.SkillListItemDTO convertToListItem(SkillListing listing) {
        SkillSearchResultDTO.SkillListItemDTO item = new SkillSearchResultDTO.SkillListItemDTO();
        item.setId(listing.getSkillId());
        item.setName(listing.getName());
        item.setVersion(listing.getVersion());
        item.setType(listing.getType());
        item.setCapabilities(listing.getCapabilities());
        item.setScenes(listing.getScenes());
        item.setEndpoint(listing.getEndpoint());
        item.setDownloadUrl(listing.getDownloadUrl());
        item.setDescription(listing.getDescription());
        item.setAuthor(listing.getAuthor());
        item.setRating(listing.getRating());
        item.setDownloadCount(listing.getDownloadCount());
        return item;
    }

    private SkillManifestDTO convertToManifest(SkillListing listing) {
        SkillManifestDTO manifest = new SkillManifestDTO();
        
        SkillManifestDTO.Metadata metadata = new SkillManifestDTO.Metadata();
        metadata.setId(listing.getSkillId());
        metadata.setName(listing.getName());
        metadata.setVersion(listing.getVersion());
        metadata.setDescription(listing.getDescription());
        metadata.setAuthor(listing.getAuthor());
        metadata.setLicense(listing.getLicense());
        metadata.setHomepage(listing.getHomepage());
        metadata.setRepository(listing.getRepository());
        manifest.setMetadata(metadata);
        
        SkillManifestDTO.Spec spec = new SkillManifestDTO.Spec();
        spec.setType(listing.getType() != null ? listing.getType() : "tool-skill");
        
        if (listing.getCapabilities() != null && !listing.getCapabilities().isEmpty()) {
            List<SkillManifestDTO.Capability> capabilities = listing.getCapabilities().stream()
                    .map(capId -> {
                        SkillManifestDTO.Capability cap = new SkillManifestDTO.Capability();
                        cap.setId(capId);
                        cap.setName(capId);
                        cap.setDescription("Capability: " + capId);
                        return cap;
                    })
                    .collect(Collectors.toList());
            spec.setCapabilities(capabilities);
        }
        
        if (listing.getScenes() != null && !listing.getScenes().isEmpty()) {
            List<SkillManifestDTO.Scene> scenes = listing.getScenes().stream()
                    .map(sceneName -> {
                        SkillManifestDTO.Scene scene = new SkillManifestDTO.Scene();
                        scene.setName(sceneName);
                        scene.setDescription("Scene: " + sceneName);
                        scene.setCapabilities(listing.getCapabilities());
                        return scene;
                    })
                    .collect(Collectors.toList());
            spec.setScenes(scenes);
        }
        
        SkillManifestDTO.Runtime runtime = new SkillManifestDTO.Runtime();
        runtime.setLanguage("java");
        runtime.setJavaVersion("11");
        runtime.setFramework("spring-boot");
        spec.setRuntime(runtime);
        
        SkillManifestDTO.Deployment deployment = new SkillManifestDTO.Deployment();
        deployment.setModes(Arrays.asList("remote-hosted", "local-deployed"));
        deployment.setSingleton(false);
        deployment.setRequiresAuth(true);
        spec.setDeployment(deployment);
        
        manifest.setSpec(spec);
        
        return manifest;
    }
}
