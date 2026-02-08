package net.ooder.skillcenter.sdk;

import net.ooder.sdk.AgentSDK;
import net.ooder.sdk.agent.model.AgentConfig;
import net.ooder.sdk.skill.Skill;
import net.ooder.sdk.skill.SkillManager;
import net.ooder.sdk.skill.SkillResult;
import net.ooder.sdk.command.model.CommandResult;
import net.ooder.sdk.command.model.CommandType;
import net.ooder.skillcenter.config.SdkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AgentSDK 0.6.6 包装器
 * 提供对 agent-sdk 的统一访问接口
 */
@Component
public class AgentSDKWrapper {

    private static final Logger log = LoggerFactory.getLogger(AgentSDKWrapper.class);

    @Autowired
    private SdkConfig sdkConfig;

    private AgentSDK agentSDK;
    private SkillManager skillManager;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        if (!sdkConfig.isMockMode()) {
            initializeSDK();
        } else {
            log.info("[AgentSDKWrapper] Running in mock mode, SDK not initialized");
        }
    }

    /**
     * 初始化AgentSDK
     */
    private void initializeSDK() {
        try {
            log.info("[AgentSDKWrapper] Initializing AgentSDK...");

            AgentConfig config = new AgentConfig();
            config.setAgentId(sdkConfig.getAgentId());
            config.setAgentName(sdkConfig.getAgentName());
            config.setAgentType(sdkConfig.getAgentType());
            config.setEndpoint(sdkConfig.getEndpoint());
            config.setUdpPort(sdkConfig.getUdpPort());
            config.setUdpBufferSize(sdkConfig.getUdpBufferSize());
            config.setUdpTimeout(sdkConfig.getUdpTimeout());
            config.setUdpMaxPacketSize(sdkConfig.getUdpMaxPacketSize());
            config.setHeartbeatInterval(sdkConfig.getHeartbeatInterval());
            config.setHeartbeatTimeout(sdkConfig.getHeartbeatTimeout());
            config.setHeartbeatLossThreshold(sdkConfig.getHeartbeatLossThreshold());

            agentSDK = new AgentSDK(config);
            agentSDK.start();
            skillManager = SkillManager.getInstance();
            initialized = true;

            log.info("[AgentSDKWrapper] AgentSDK initialized successfully, agentId: {}", config.getAgentId());
        } catch (Exception e) {
            log.error("[AgentSDKWrapper] Failed to initialize AgentSDK: {}", e.getMessage(), e);
            initialized = false;
        }
    }

    @PreDestroy
    public void destroy() {
        if (agentSDK != null) {
            log.info("[AgentSDKWrapper] Stopping AgentSDK...");
            agentSDK.stop();
            log.info("[AgentSDKWrapper] AgentSDK stopped");
        }
    }

    /**
     * 检查SDK是否已初始化
     */
    public boolean isInitialized() {
        return initialized && agentSDK != null;
    }

    // ==================== 技能管理 ====================

    /**
     * 获取所有技能
     */
    public Map<String, Skill> getAllSkills() {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, returning empty skills");
            return Collections.emptyMap();
        }
        // SkillManager 0.6.6 使用 getAllSkills()
        return skillManager.getAllSkills();
    }

    /**
     * 根据ID获取技能
     */
    public Skill getSkill(String skillId) {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, cannot get skill: {}", skillId);
            return null;
        }
        return skillManager.getSkill(skillId);
    }

    /**
     * 注册技能
     */
    public void registerSkill(Skill skill) {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, cannot register skill");
            return;
        }
        skillManager.registerSkill(skill);
    }

    /**
     * 注销技能
     */
    public void unregisterSkill(String skillId) {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, cannot unregister skill");
            return;
        }
        skillManager.unregisterSkill(skillId);
    }

    /**
     * 执行技能
     */
    public SkillResult executeSkill(String skillId, Map<String, Object> params) {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, cannot execute skill: {}", skillId);
            return SkillResult.failure("SDK not initialized", null);
        }

        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) {
            log.error("[AgentSDKWrapper] Skill not found: {}", skillId);
            return SkillResult.failure("Skill not found: " + skillId, null);
        }

        try {
            return skill.execute(params);
        } catch (Exception e) {
            log.error("[AgentSDKWrapper] Failed to execute skill: {}", skillId, e);
            return SkillResult.failure("Execution failed: " + e.getMessage(), null);
        }
    }

    // ==================== Agent 状态 ====================

    /**
     * 获取Agent状态
     */
    public AgentStatus getAgentStatus() {
        if (!isInitialized()) {
            return AgentStatus.builder()
                    .agentId(sdkConfig.getAgentId())
                    .healthy(false)
                    .message("SDK not initialized")
                    .build();
        }

        return AgentStatus.builder()
                .agentId(agentSDK.getAgentId())
                .agentName(agentSDK.getAgentName())
                .agentType(agentSDK.getAgentType())
                .healthy(agentSDK.isHealthy())
                .sleepMode(agentSDK.getSleepMode() != null ? agentSDK.getSleepMode().name() : "UNKNOWN")
                .endpoint(agentSDK.getEndpoint())
                .message("OK")
                .build();
    }

    /**
     * 检查Agent健康状态
     */
    public boolean isHealthy() {
        return isInitialized() && agentSDK.isHealthy();
    }

    // ==================== 命令发送 ====================

    /**
     * 发送命令
     */
    public CompletableFuture<CommandResult> sendCommand(CommandType commandType, Map<String, Object> params) {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, cannot send command");
            return CompletableFuture.completedFuture(
                    CommandResult.failed("SDK not initialized")
            );
        }

        return agentSDK.sendCommand(commandType, params)
                .thenApply(sendResult -> {
                    if (sendResult.isSuccess()) {
                        return CommandResult.success();
                    } else {
                        return CommandResult.failed(sendResult.getMessage());
                    }
                })
                .exceptionally(e -> {
                    log.error("[AgentSDKWrapper] Failed to send command: {}", commandType, e);
                    return CommandResult.failed("Send failed: " + e.getMessage());
                });
    }

    // ==================== 路由管理 ====================

    /**
     * 获取所有路由
     */
    public Map<String, AgentSDK.RouteInfo> getRoutes() {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, cannot get routes");
            return Collections.emptyMap();
        }
        return agentSDK.getRoutes();
    }

    /**
     * 添加路由
     */
    public void addRoute(String routeId, String source, String destination, Map<String, Object> routeInfo) {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, cannot add route");
            return;
        }
        agentSDK.addRoute(routeId, source, destination, routeInfo);
    }

    /**
     * 删除路由
     */
    public void removeRoute(String routeId) {
        if (!isInitialized()) {
            log.warn("[AgentSDKWrapper] SDK not initialized, cannot remove route");
            return;
        }
        agentSDK.removeRoute(routeId);
    }

    // ==================== 内部类 ====================

    /**
     * Agent状态信息
     */
    public static class AgentStatus {
        private String agentId;
        private String agentName;
        private String agentType;
        private boolean healthy;
        private String sleepMode;
        private String endpoint;
        private String message;

        public static AgentStatusBuilder builder() {
            return new AgentStatusBuilder();
        }

        // Getters
        public String getAgentId() { return agentId; }
        public String getAgentName() { return agentName; }
        public String getAgentType() { return agentType; }
        public boolean isHealthy() { return healthy; }
        public String getSleepMode() { return sleepMode; }
        public String getEndpoint() { return endpoint; }
        public String getMessage() { return message; }

        public static class AgentStatusBuilder {
            private AgentStatus status = new AgentStatus();

            public AgentStatusBuilder agentId(String agentId) {
                status.agentId = agentId;
                return this;
            }

            public AgentStatusBuilder agentName(String agentName) {
                status.agentName = agentName;
                return this;
            }

            public AgentStatusBuilder agentType(String agentType) {
                status.agentType = agentType;
                return this;
            }

            public AgentStatusBuilder healthy(boolean healthy) {
                status.healthy = healthy;
                return this;
            }

            public AgentStatusBuilder sleepMode(String sleepMode) {
                status.sleepMode = sleepMode;
                return this;
            }

            public AgentStatusBuilder endpoint(String endpoint) {
                status.endpoint = endpoint;
                return this;
            }

            public AgentStatusBuilder message(String message) {
                status.message = message;
                return this;
            }

            public AgentStatus build() {
                return status;
            }
        }
    }
}
