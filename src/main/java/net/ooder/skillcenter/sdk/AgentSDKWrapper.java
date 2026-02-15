package net.ooder.skillcenter.sdk;

import net.ooder.sdk.AgentSDK;
import net.ooder.sdk.agent.model.AgentConfig;
import net.ooder.sdk.skill.Skill;
import net.ooder.sdk.skill.SkillManager;
import net.ooder.sdk.skill.SkillResult;
import net.ooder.sdk.skill.packageManager.SkillPackageManager;
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

/**
 * AgentSDK 包装器 - 支持v0.7.0协议
 * 提供对 agent-sdk 的统一访问接口
 */
@Component
public class AgentSDKWrapper {

    private static final Logger log = LoggerFactory.getLogger(AgentSDKWrapper.class);

    @Autowired
    private SdkConfig sdkConfig;

    private AgentSDK agentSDK;
    private SkillManager skillManager;
    private SkillPackageManager packageManager;
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
            log.info("[AgentSDKWrapper] Initializing AgentSDK with v0.7.0...");
            
            AgentConfig config = AgentConfig.builder()
                .agentId(sdkConfig.getAgentId())
                .agentName(sdkConfig.getAgentName())
                .agentType(sdkConfig.getAgentType())
                .endpoint(sdkConfig.getEndpoint())
                .build();
            
            agentSDK = new AgentSDK(config);
            agentSDK.start();
            
            skillManager = SkillManager.getInstance();
            
            log.info("[AgentSDKWrapper] AgentSDK initialized successfully");
            initialized = true;
        } catch (Exception e) {
            log.error("[AgentSDKWrapper] Failed to initialize AgentSDK: {}", e.getMessage(), e);
            initialized = false;
        }
    }

    @PreDestroy
    public void destroy() {
        if (agentSDK != null) {
            try {
                agentSDK.stop();
                log.info("[AgentSDKWrapper] AgentSDK stopped successfully");
            } catch (Exception e) {
                log.error("[AgentSDKWrapper] Error during shutdown: {}", e.getMessage(), e);
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public SkillPackageManager getPackageManager() {
        return packageManager;
    }

    public void registerSkill(Skill skill) {
        if (initialized && skillManager != null) {
            skillManager.registerSkill(skill);
            log.info("[AgentSDKWrapper] Skill registered: {}", skill.getSkillId());
        } else {
            log.warn("[AgentSDKWrapper] Cannot register skill, SDK not initialized");
        }
    }

    public void unregisterSkill(String skillId) {
        if (initialized && skillManager != null) {
            skillManager.unregisterSkill(skillId);
            log.info("[AgentSDKWrapper] Skill unregistered: {}", skillId);
        }
    }

    public Skill getSkill(String skillId) {
        if (initialized && skillManager != null) {
            return skillManager.getSkill(skillId);
        }
        return null;
    }

    public Map<String, Skill> getAllSkills() {
        if (initialized && skillManager != null) {
            return skillManager.getAllSkills();
        }
        return Collections.emptyMap();
    }

    public SkillResult executeSkill(String skillId, Map<String, Object> params) {
        if (initialized && skillManager != null) {
            return skillManager.executeSkill(skillId, params);
        }
        return SkillResult.failure("SDK not initialized", null);
    }
}
