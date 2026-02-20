package net.ooder.skillcenter.sdk;

import net.ooder.sdk.api.OoderSDK;
import net.ooder.sdk.infra.config.SDKConfiguration;
import net.ooder.sdk.api.skill.*;
import net.ooder.sdk.api.scene.SceneManager;
import net.ooder.sdk.api.scene.SceneGroupManager;
import net.ooder.sdk.core.skill.impl.SkillPackageManagerImpl;
import net.ooder.sdk.core.scene.impl.SceneManagerImpl;
import net.ooder.skillcenter.config.SdkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentSDKWrapper {

    private static final Logger log = LoggerFactory.getLogger(AgentSDKWrapper.class);

    @Autowired
    private SdkConfig sdkConfig;

    private OoderSDK ooderSDK;
    private SkillPackageManager skillPackageManager;
    private SceneManager sceneManager;
    private SceneGroupManager sceneGroupManager;
    
    private boolean initialized = false;
    private final Map<String, SkillPackage> skillCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (!sdkConfig.isMockMode()) {
            initializeSDK();
        } else {
            log.info("[AgentSDKWrapper] Running in mock mode, SDK not initialized");
        }
    }

    private void initializeSDK() {
        try {
            log.info("[AgentSDKWrapper] Initializing OoderSDK v0.7.2...");
            
            SDKConfiguration config = new SDKConfiguration();
            config.setAgentId(sdkConfig.getAgentId());
            config.setAgentName(sdkConfig.getAgentName());
            config.setEndpoint(sdkConfig.getEndpoint());
            config.setUdpPort(sdkConfig.getUdpPort());
            config.setHeartbeatInterval(sdkConfig.getHeartbeatInterval());
            config.setHeartbeatTimeout(sdkConfig.getHeartbeatTimeout());
            config.setHeartbeatLossThreshold(sdkConfig.getHeartbeatLossThreshold());
            config.setSkillRootPath(sdkConfig.getBasePath());
            config.setSkillCenterUrl(sdkConfig.getSkillCenterUrl());
            config.setDiscoveryEnabled(true);
            
            log.info("[AgentSDKWrapper] Configuration:");
            log.info("[AgentSDKWrapper] - agentId: {}", config.getAgentId());
            log.info("[AgentSDKWrapper] - agentName: {}", config.getAgentName());
            log.info("[AgentSDKWrapper] - endpoint: {}", config.getEndpoint());
            log.info("[AgentSDKWrapper] - skillRootPath: {}", config.getSkillRootPath());
            log.info("[AgentSDKWrapper] - skillCenterUrl: {}", config.getSkillCenterUrl());
            
            // 创建SkillPackageManager实现
            skillPackageManager = new SkillPackageManagerImpl();
            if (skillPackageManager instanceof SkillPackageManagerImpl) {
                ((SkillPackageManagerImpl) skillPackageManager).setSkillRootPath(config.getSkillRootPath());
            }
            log.info("[AgentSDKWrapper] Created SkillPackageManagerImpl");
            
            // 创建SceneManager实现
            sceneManager = new SceneManagerImpl();
            log.info("[AgentSDKWrapper] Created SceneManagerImpl");
            
            ooderSDK = OoderSDK.builder()
                .configuration(config)
                .skillPackageManager(skillPackageManager)
                .sceneManager(sceneManager)
                .build();
            
            ooderSDK.initialize();
            ooderSDK.start();
            
            // 从SDK获取可能被覆盖的服务
            sceneGroupManager = ooderSDK.getSceneGroupManager();
            
            log.info("[AgentSDKWrapper] OoderSDK initialized successfully");
            log.info("[AgentSDKWrapper] - SkillPackageManager: {}", skillPackageManager != null ? "available" : "null");
            log.info("[AgentSDKWrapper] - SceneManager: {}", sceneManager != null ? "available" : "null");
            log.info("[AgentSDKWrapper] - SceneGroupManager: {}", sceneGroupManager != null ? "available" : "null");
            log.info("[AgentSDKWrapper] - isInitialized: {}", ooderSDK.isInitialized());
            log.info("[AgentSDKWrapper] - isStarted: {}", ooderSDK.isStarted());
            
            initialized = true;
            
            loadInstalledSkills();
            
        } catch (Exception e) {
            log.error("[AgentSDKWrapper] Failed to initialize OoderSDK: {}", e.getMessage(), e);
            initialized = false;
        }
    }
    
    private void loadInstalledSkills() {
        if (skillPackageManager == null) {
            log.info("[AgentSDKWrapper] SkillPackageManager not available, skipping skill loading");
            return;
        }
        
        try {
            List<InstalledSkill> installed = skillPackageManager.listInstalled().get();
            if (installed != null) {
                log.info("[AgentSDKWrapper] Found {} installed skills", installed.size());
                for (InstalledSkill skill : installed) {
                    SkillPackage pkg = skillPackageManager.getPackage(skill.getSkillId()).get();
                    if (pkg != null) {
                        skillCache.put(pkg.getSkillId(), pkg);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[AgentSDKWrapper] Failed to load installed skills: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        if (ooderSDK != null) {
            try {
                ooderSDK.stop();
                log.info("[AgentSDKWrapper] OoderSDK stopped successfully");
            } catch (Exception e) {
                log.error("[AgentSDKWrapper] Error during shutdown: {}", e.getMessage(), e);
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public SkillPackageManager getSkillPackageManager() {
        return skillPackageManager;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public SceneGroupManager getSceneGroupManager() {
        return sceneGroupManager;
    }

    public SkillPackage getSkill(String skillId) {
        if (skillCache.containsKey(skillId)) {
            return skillCache.get(skillId);
        }
        
        if (initialized && skillPackageManager != null) {
            try {
                SkillPackage pkg = skillPackageManager.getPackage(skillId).get();
                if (pkg != null) {
                    skillCache.put(skillId, pkg);
                }
                return pkg;
            } catch (Exception e) {
                log.error("[AgentSDKWrapper] Failed to get skill: {}", e.getMessage());
            }
        }
        return null;
    }

    public Map<String, SkillPackage> getAllSkills() {
        if (!initialized || skillPackageManager == null) {
            return Collections.emptyMap();
        }
        
        try {
            List<InstalledSkill> installed = skillPackageManager.listInstalled().get();
            if (installed != null) {
                Map<String, SkillPackage> result = new HashMap<>();
                for (InstalledSkill skill : installed) {
                    SkillPackage pkg = skillPackageManager.getPackage(skill.getSkillId()).get();
                    if (pkg != null) {
                        result.put(pkg.getSkillId(), pkg);
                        skillCache.put(pkg.getSkillId(), pkg);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("[AgentSDKWrapper] Failed to get all skills: {}", e.getMessage());
        }
        
        return new HashMap<>(skillCache);
    }
    
    public List<SkillPackage> listAllSkills() {
        if (!initialized || skillPackageManager == null) {
            return Collections.emptyList();
        }
        
        try {
            List<InstalledSkill> installed = skillPackageManager.listInstalled().get();
            if (installed != null) {
                List<SkillPackage> result = new ArrayList<>();
                for (InstalledSkill skill : installed) {
                    SkillPackage pkg = skillPackageManager.getPackage(skill.getSkillId()).get();
                    if (pkg != null) {
                        result.add(pkg);
                        skillCache.put(pkg.getSkillId(), pkg);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("[AgentSDKWrapper] Failed to list all skills: {}", e.getMessage());
        }
        
        return new ArrayList<>(skillCache.values());
    }

    public InstallResult installSkill(SkillPackage skillPackage) {
        if (initialized && skillPackageManager != null) {
            try {
                InstallRequest request = new InstallRequest();
                request.setSkillId(skillPackage.getSkillId());
                InstallResult result = skillPackageManager.install(request).get();
                if (result.isSuccess()) {
                    skillCache.put(skillPackage.getSkillId(), skillPackage);
                }
                return result;
            } catch (Exception e) {
                log.error("[AgentSDKWrapper] Failed to install skill: {}", e.getMessage());
                InstallResult result = new InstallResult();
                result.setSuccess(false);
                result.setError(e.getMessage());
                return result;
            }
        }
        InstallResult result = new InstallResult();
        result.setSuccess(false);
        result.setError("SDK not initialized");
        return result;
    }
    
    public CompletableFuture<InstallResult> installSkillAsync(String skillId) {
        if (initialized && skillPackageManager != null) {
            InstallRequest request = new InstallRequest();
            request.setSkillId(skillId);
            return skillPackageManager.install(request);
        }
        return CompletableFuture.completedFuture(createErrorResult("SDK not initialized"));
    }
    
    public CompletableFuture<UninstallResult> uninstallSkillAsync(String skillId) {
        if (initialized && skillPackageManager != null) {
            return skillPackageManager.uninstall(skillId);
        }
        return CompletableFuture.completedFuture(createUninstallErrorResult("SDK not initialized"));
    }
    
    private InstallResult createErrorResult(String error) {
        InstallResult result = new InstallResult();
        result.setSuccess(false);
        result.setError(error);
        return result;
    }
    
    private UninstallResult createUninstallErrorResult(String error) {
        UninstallResult result = new UninstallResult();
        result.setSuccess(false);
        result.setError(error);
        return result;
    }
}
