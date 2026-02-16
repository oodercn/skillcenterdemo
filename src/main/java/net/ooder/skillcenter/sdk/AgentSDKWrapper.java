package net.ooder.skillcenter.sdk;

import net.ooder.sdk.api.OoderSDK;
import net.ooder.sdk.api.skill.*;
import net.ooder.skillcenter.config.SdkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class AgentSDKWrapper {

    private static final Logger log = LoggerFactory.getLogger(AgentSDKWrapper.class);

    @Autowired
    private SdkConfig sdkConfig;

    private OoderSDK ooderSDK;
    private SkillRegistry skillRegistry;
    private SkillDiscoverer skillDiscoverer;
    private SkillInstaller skillInstaller;
    private boolean initialized = false;

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
            log.info("[AgentSDKWrapper] Initializing OoderSDK v0.7.1...");
            
            ooderSDK = OoderSDK.builder()
                .agentId(sdkConfig.getAgentId())
                .agentName(sdkConfig.getAgentName())
                .agentType(sdkConfig.getAgentType())
                .endpoint(sdkConfig.getEndpoint())
                .build();
            
            ooderSDK.start();
            
            skillRegistry = ooderSDK.getSkillRegistry();
            skillDiscoverer = ooderSDK.getSkillDiscoverer();
            skillInstaller = ooderSDK.getSkillInstaller();
            
            log.info("[AgentSDKWrapper] OoderSDK initialized successfully");
            initialized = true;
        } catch (Exception e) {
            log.error("[AgentSDKWrapper] Failed to initialize OoderSDK: {}", e.getMessage(), e);
            initialized = false;
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

    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    public SkillDiscoverer getSkillDiscoverer() {
        return skillDiscoverer;
    }

    public SkillInstaller getSkillInstaller() {
        return skillInstaller;
    }

    public void registerSkill(SkillPackage skillPackage) {
        if (initialized && skillRegistry != null) {
            skillRegistry.register(skillPackage);
            log.info("[AgentSDKWrapper] Skill registered: {}", skillPackage.getSkillId());
        } else {
            log.warn("[AgentSDKWrapper] Cannot register skill, SDK not initialized");
        }
    }

    public void unregisterSkill(String skillId) {
        if (initialized && skillRegistry != null) {
            skillRegistry.unregister(skillId);
            log.info("[AgentSDKWrapper] Skill unregistered: {}", skillId);
        }
    }

    public SkillPackage getSkill(String skillId) {
        if (initialized && skillRegistry != null) {
            try {
                return skillRegistry.get(skillId).get();
            } catch (Exception e) {
                log.error("[AgentSDKWrapper] Failed to get skill: {}", e.getMessage());
            }
        }
        return null;
    }

    public Map<String, SkillPackage> getAllSkills() {
        if (initialized && skillRegistry != null) {
            try {
                return skillRegistry.getAll().get().stream()
                    .collect(java.util.stream.Collectors.toMap(
                        SkillPackage::getSkillId, 
                        s -> s
                    ));
            } catch (Exception e) {
                log.error("[AgentSDKWrapper] Failed to get all skills: {}", e.getMessage());
            }
        }
        return Collections.emptyMap();
    }

    public InstallResult installSkill(SkillPackage skillPackage) {
        if (initialized && skillInstaller != null) {
            try {
                return skillInstaller.install(skillPackage, SkillInstaller.InstallMode.NORMAL).get();
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
}
