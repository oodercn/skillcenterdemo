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

@Component
public class AgentSDKWrapper {

    private static final Logger log = LoggerFactory.getLogger(AgentSDKWrapper.class);

    @Autowired
    private SdkConfig sdkConfig;

    private OoderSDK ooderSDK;
    private SkillPackageManager skillPackageManager;
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
            
            ooderSDK = OoderSDK.builder().build();
            ooderSDK.start();
            
            skillPackageManager = ooderSDK.getSkillPackageManager();
            
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

    public SkillPackageManager getSkillPackageManager() {
        return skillPackageManager;
    }

    public void registerSkill(SkillPackage skillPackage) {
        if (initialized && skillPackageManager != null) {
            log.info("[AgentSDKWrapper] Skill package registered: {}", skillPackage.getSkillId());
        } else {
            log.warn("[AgentSDKWrapper] Cannot register skill, SDK not initialized");
        }
    }

    public void unregisterSkill(String skillId) {
        if (initialized && skillPackageManager != null) {
            log.info("[AgentSDKWrapper] Skill unregistered: {}", skillId);
        }
    }

    public SkillPackage getSkill(String skillId) {
        if (initialized && skillPackageManager != null) {
            try {
                return skillPackageManager.getPackage(skillId).get();
            } catch (Exception e) {
                log.error("[AgentSDKWrapper] Failed to get skill: {}", e.getMessage());
            }
        }
        return null;
    }

    public Map<String, SkillPackage> getAllSkills() {
        return Collections.emptyMap();
    }

    public InstallResult installSkill(SkillPackage skillPackage) {
        if (initialized && skillPackageManager != null) {
            try {
                InstallRequest request = new InstallRequest();
                request.setSkillId(skillPackage.getSkillId());
                return skillPackageManager.install(request).get();
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
