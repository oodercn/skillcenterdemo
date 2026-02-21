package net.ooder.skillcenter.sdk;

import net.ooder.scene.core.*;
import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class SceneEngineAdapterImpl implements SceneEngineAdapter {

    private static final Logger log = LoggerFactory.getLogger(SceneEngineAdapterImpl.class);

    @Autowired
    private SdkConfig sdkConfig;

    @Autowired
    private SceneEngineAdapterMockImpl mockAdapter;

    private SceneEngine sceneEngine;
    private SceneClient sceneClient;
    private boolean engineAvailable = false;

    @PostConstruct
    public void init() {
        if (sdkConfig.isMockMode()) {
            log.info("[SceneEngineAdapter] Running in mock mode");
            return;
        }

        log.info("[SceneEngineAdapter] Initializing SceneEngine from scene-engine-core...");
        engineAvailable = initializeEngine();

        if (engineAvailable) {
            log.info("[SceneEngineAdapter] SceneEngine initialized successfully");
        } else {
            log.warn("[SceneEngineAdapter] SceneEngine not available, falling back to mock");
        }
    }

    private boolean initializeEngine() {
        try {
            sceneEngine = new SceneEngine() {
                private EngineStatus status = EngineStatus.STOPPED;
                
                @Override
                public SceneClient login(String username, String password) {
                    return null;
                }
                
                @Override
                public SceneClient login(String token) {
                    return createMockSceneClient();
                }
                
                @Override
                public AdminClient adminLogin(String username, String password) {
                    return null;
                }
                
                @Override
                public void logout(String sessionId) {}
                
                @Override
                public SessionInfo getSession(String sessionId) {
                    return null;
                }
                
                @Override
                public boolean validateSession(String sessionId) {
                    return false;
                }
                
                @Override
                public SessionInfo refreshSession(String sessionId) {
                    return null;
                }
                
                @Override
                public EngineStatus getStatus() {
                    return status;
                }
                
                @Override
                public void start() {
                    status = EngineStatus.RUNNING;
                }
                
                @Override
                public void stop() {
                    status = EngineStatus.STOPPED;
                }
                
                @Override
                public String getName() {
                    return "SkillCenter-Engine";
                }
                
                @Override
                public String getVersion() {
                    return "0.7.3";
                }
            };
            
            sceneEngine.start();
            sceneClient = sceneEngine.login("mock-token");
            
            return sceneEngine.getStatus() == EngineStatus.RUNNING;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to initialize SceneEngine: {}", e.getMessage());
            return false;
        }
    }
    
    private SceneClient createMockSceneClient() {
        return new SceneClient() {
            @Override
            public String getSessionId() { return "mock-session"; }
            
            @Override
            public String getUserId() { return "mock-user"; }
            
            @Override
            public String getUsername() { return "MockUser"; }
            
            @Override
            public String getToken() { return "mock-token"; }
            
            @Override
            public SkillInfo findSkill(String skillId) {
                return null;
            }
            
            @Override
            public List<SkillInfo> searchSkills(SkillQuery query) {
                return new ArrayList<>();
            }
            
            @Override
            public List<InstalledSkillInfo> listMySkills() {
                return new ArrayList<>();
            }
            
            @Override
            public SkillInstallResult installSkill(String skillId) {
                SkillInstallResult result = new SkillInstallResult();
                result.setSuccess(true);
                return result;
            }
            
            @Override
            public SkillInstallResult installSkill(String skillId, java.util.Map<String, Object> config) {
                return installSkill(skillId);
            }
            
            @Override
            public SkillUninstallResult uninstallSkill(String skillId) {
                SkillUninstallResult result = new SkillUninstallResult();
                result.setSuccess(true);
                return result;
            }
            
            @Override
            public SkillInstallProgress getInstallProgress(String skillId) {
                return null;
            }
            
            @Override
            public List<SceneInfo> listAvailableScenes() {
                return new ArrayList<>();
            }
            
            @Override
            public SceneGroupInfo joinSceneGroup(String sceneId) {
                return new SceneGroupInfo();
            }
            
            @Override
            public SceneGroupInfo joinSceneGroup(String sceneId, String role) {
                return joinSceneGroup(sceneId);
            }
            
            @Override
            public void leaveSceneGroup(String groupId) {}
            
            @Override
            public List<SceneGroupInfo> listMySceneGroups() {
                return new ArrayList<>();
            }
            
            @Override
            public SceneGroupInfo getSceneGroup(String groupId) {
                return null;
            }
            
            @Override
            public Object invokeCapability(String sceneId, String capability, java.util.Map<String, Object> params) {
                return null;
            }
            
            @Override
            public List<CapabilityInfo> listCapabilities(String sceneId) {
                return new ArrayList<>();
            }
            
            @Override
            public UserSettings getSettings() {
                return new UserSettings();
            }
            
            @Override
            public void updateSettings(UserSettings settings) {}
            
            @Override
            public IdentityInfo getIdentity() {
                return new IdentityInfo();
            }
            
            @Override
            public java.util.concurrent.CompletableFuture<HeartbeatResult> startHeartbeat(String groupId) {
                return java.util.concurrent.CompletableFuture.completedFuture(new HeartbeatResult());
            }
            
            @Override
            public void stopHeartbeat(String groupId) {}
            
            @Override
            public HeartbeatStatus getHeartbeatStatus(String groupId) {
                return HeartbeatStatus.HEALTHY;
            }
        };
    }

    @Override
    public boolean isAvailable() {
        return engineAvailable || mockAdapter.isAvailable();
    }

    @Override
    public List<SkillInfoDTO> listInstalledSkills() {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.listInstalledSkills();
        }

        try {
            List<InstalledSkillInfo> skills = sceneClient.listMySkills();
            return skills.stream()
                .map(this::convertInstalledSkillInfo)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to list skills: {}", e.getMessage());
            return mockAdapter.listInstalledSkills();
        }
    }

    @Override
    public SkillInfoDTO getSkill(String skillId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.getSkill(skillId);
        }

        try {
            SkillInfo info = sceneClient.findSkill(skillId);
            return info != null ? convertSkillInfo(info) : null;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to get skill: {}", e.getMessage());
            return mockAdapter.getSkill(skillId);
        }
    }

    @Override
    public SkillInstallResultDTO installSkill(String skillId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.installSkill(skillId);
        }

        try {
            SkillInstallResult result = sceneClient.installSkill(skillId);
            SkillInstallResultDTO dto = new SkillInstallResultDTO();
            dto.setSuccess(result.isSuccess());
            dto.setError(result.getMessage());
            dto.setSkillId(skillId);
            return dto;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to install skill: {}", e.getMessage());
            return SkillInstallResultDTO.failure(e.getMessage());
        }
    }

    @Override
    public SkillUninstallResultDTO uninstallSkill(String skillId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.uninstallSkill(skillId);
        }

        try {
            SkillUninstallResult result = sceneClient.uninstallSkill(skillId);
            SkillUninstallResultDTO dto = new SkillUninstallResultDTO();
            dto.setSuccess(result.isSuccess());
            dto.setError(result.getMessage());
            dto.setSkillId(skillId);
            return dto;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to uninstall skill: {}", e.getMessage());
            return SkillUninstallResultDTO.failure(e.getMessage());
        }
    }

    @Override
    public SceneInfoDTO createScene(SceneInfoDTO scene) {
        return mockAdapter.createScene(scene);
    }

    @Override
    public SceneInfoDTO getScene(String sceneId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.getScene(sceneId);
        }

        try {
            List<SceneInfo> scenes = sceneClient.listAvailableScenes();
            for (SceneInfo info : scenes) {
                if (info.getSceneId().equals(sceneId)) {
                    return convertSceneInfo(info);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to get scene: {}", e.getMessage());
            return mockAdapter.getScene(sceneId);
        }
    }

    @Override
    public List<SceneInfoDTO> listScenes() {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.listScenes();
        }

        try {
            List<SceneInfo> scenes = sceneClient.listAvailableScenes();
            return scenes.stream()
                .map(this::convertSceneInfo)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to list scenes: {}", e.getMessage());
            return mockAdapter.listScenes();
        }
    }

    @Override
    public boolean activateScene(String sceneId) {
        return mockAdapter.activateScene(sceneId);
    }

    @Override
    public boolean deactivateScene(String sceneId) {
        return mockAdapter.deactivateScene(sceneId);
    }

    @Override
    public boolean addCapability(String sceneId, CapabilityInfoDTO capability) {
        return mockAdapter.addCapability(sceneId, capability);
    }

    @Override
    public boolean removeCapability(String sceneId, String capabilityId) {
        return mockAdapter.removeCapability(sceneId, capabilityId);
    }

    @Override
    public List<CapabilityInfoDTO> listCapabilities(String sceneId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.listCapabilities(sceneId);
        }

        try {
            List<CapabilityInfo> caps = sceneClient.listCapabilities(sceneId);
            return caps.stream()
                .map(this::convertCapabilityInfo)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to list capabilities: {}", e.getMessage());
            return mockAdapter.listCapabilities(sceneId);
        }
    }

    @Override
    public CapabilityInfoDTO getCapability(String sceneId, String capabilityId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.getCapability(sceneId, capabilityId);
        }

        try {
            List<CapabilityInfo> caps = sceneClient.listCapabilities(sceneId);
            for (CapabilityInfo cap : caps) {
                if (cap.getName().equals(capabilityId)) {
                    return convertCapabilityInfo(cap);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to get capability: {}", e.getMessage());
            return mockAdapter.getCapability(sceneId, capabilityId);
        }
    }

    @Override
    public SceneGroupInfoDTO createSceneGroup(String sceneId, SceneGroupConfigDTO config) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.createSceneGroup(sceneId, config);
        }

        try {
            SceneGroupInfo group = sceneClient.joinSceneGroup(sceneId);
            return convertSceneGroupInfo(group);
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to create scene group: {}", e.getMessage());
            return mockAdapter.createSceneGroup(sceneId, config);
        }
    }

    @Override
    public boolean destroySceneGroup(String sceneGroupId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.destroySceneGroup(sceneGroupId);
        }

        try {
            sceneClient.leaveSceneGroup(sceneGroupId);
            return true;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to destroy scene group: {}", e.getMessage());
            return mockAdapter.destroySceneGroup(sceneGroupId);
        }
    }

    @Override
    public List<SceneGroupInfoDTO> listSceneGroups() {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.listSceneGroups();
        }

        try {
            List<SceneGroupInfo> groups = sceneClient.listMySceneGroups();
            return groups.stream()
                .map(this::convertSceneGroupInfo)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to list scene groups: {}", e.getMessage());
            return mockAdapter.listSceneGroups();
        }
    }

    @Override
    public boolean joinSceneGroup(String sceneGroupId, String agentId, String role) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.joinSceneGroup(sceneGroupId, agentId, role);
        }

        try {
            sceneClient.joinSceneGroup(sceneGroupId, role);
            return true;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to join scene group: {}", e.getMessage());
            return mockAdapter.joinSceneGroup(sceneGroupId, agentId, role);
        }
    }

    @Override
    public boolean leaveSceneGroup(String sceneGroupId, String agentId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.leaveSceneGroup(sceneGroupId, agentId);
        }

        try {
            sceneClient.leaveSceneGroup(sceneGroupId);
            return true;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to leave scene group: {}", e.getMessage());
            return mockAdapter.leaveSceneGroup(sceneGroupId, agentId);
        }
    }

    @Override
    public List<SceneMemberInfoDTO> listMembers(String sceneGroupId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.listMembers(sceneGroupId);
        }

        try {
            SceneGroupInfo group = sceneClient.getSceneGroup(sceneGroupId);
            if (group != null && group.getMembers() != null) {
                return group.getMembers().stream()
                    .map(this::convertSceneMemberInfo)
                    .collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to list members: {}", e.getMessage());
            return mockAdapter.listMembers(sceneGroupId);
        }
    }

    @Override
    public SceneMemberInfoDTO getPrimaryMember(String sceneGroupId) {
        if (!engineAvailable || sceneClient == null) {
            return mockAdapter.getPrimaryMember(sceneGroupId);
        }

        try {
            SceneGroupInfo group = sceneClient.getSceneGroup(sceneGroupId);
            if (group != null && group.getMembers() != null) {
                for (SceneMemberInfo member : group.getMembers()) {
                    if (member.isPrimary()) {
                        return convertSceneMemberInfo(member);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("[SceneEngineAdapter] Failed to get primary member: {}", e.getMessage());
            return mockAdapter.getPrimaryMember(sceneGroupId);
        }
    }

    @Override
    public boolean handleFailover(String sceneGroupId, String failedMemberId) {
        return mockAdapter.handleFailover(sceneGroupId, failedMemberId);
    }

    @Override
    public PageResult<SceneInfoDTO> listScenesPaged(int pageNum, int pageSize) {
        List<SceneInfoDTO> all = listScenes();
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public PageResult<SceneGroupInfoDTO> listSceneGroupsPaged(int pageNum, int pageSize) {
        List<SceneGroupInfoDTO> all = listSceneGroups();
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public PageResult<SceneMemberInfoDTO> listMembersPaged(String sceneGroupId, int pageNum, int pageSize) {
        List<SceneMemberInfoDTO> all = listMembers(sceneGroupId);
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public PageResult<CapabilityInfoDTO> listCapabilitiesPaged(String sceneId, int pageNum, int pageSize) {
        List<CapabilityInfoDTO> all = listCapabilities(sceneId);
        return paginate(all, pageNum, pageSize);
    }

    private SkillInfoDTO convertSkillInfo(SkillInfo info) {
        SkillInfoDTO dto = new SkillInfoDTO();
        dto.setSkillId(info.getSkillId());
        dto.setName(info.getName());
        dto.setVersion(info.getVersion());
        dto.setDescription(info.getDescription());
        dto.setAuthor(info.getAuthor());
        dto.setCategory(info.getCategory());
        dto.setStatus(info.getStatus());
        dto.setInstalledAt(info.getCreatedAt());
        dto.setUpdatedAt(info.getUpdatedAt());
        return dto;
    }

    private SkillInfoDTO convertInstalledSkillInfo(InstalledSkillInfo info) {
        SkillInfoDTO dto = new SkillInfoDTO();
        dto.setSkillId(info.getSkillId());
        dto.setName(info.getName());
        dto.setVersion(info.getVersion());
        dto.setStatus(info.getStatus());
        dto.setInstalledAt(info.getInstalledAt());
        return dto;
    }

    private SceneInfoDTO convertSceneInfo(SceneInfo info) {
        SceneInfoDTO dto = new SceneInfoDTO();
        dto.setSceneId(info.getSceneId());
        dto.setName(info.getName());
        dto.setDescription(info.getDescription());
        dto.setStatus(info.getStatus());
        dto.setCreatedAt(info.getCreatedAt());
        dto.setUpdatedAt(info.getUpdatedAt());
        return dto;
    }

    private SceneGroupInfoDTO convertSceneGroupInfo(SceneGroupInfo info) {
        SceneGroupInfoDTO dto = new SceneGroupInfoDTO();
        dto.setSceneGroupId(info.getGroupId());
        dto.setSceneId(info.getSceneId());
        dto.setName(info.getName());
        dto.setStatus(info.getStatus());
        dto.setMemberCount(info.getMemberCount());
        dto.setCreatedAt(info.getCreatedAt());
        return dto;
    }

    private SceneMemberInfoDTO convertSceneMemberInfo(SceneMemberInfo info) {
        SceneMemberInfoDTO dto = new SceneMemberInfoDTO();
        dto.setMemberId(info.getMemberId());
        dto.setSceneGroupId(info.getGroupId());
        dto.setAgentId(info.getUserId());
        dto.setRole(info.getRole() != null ? info.getRole().name() : "MEMBER");
        dto.setStatus(info.getStatus());
        dto.setJoinedAt(info.getJoinedAt());
        return dto;
    }

    private CapabilityInfoDTO convertCapabilityInfo(CapabilityInfo info) {
        CapabilityInfoDTO dto = new CapabilityInfoDTO();
        dto.setCapId(info.getName());
        dto.setName(info.getName());
        dto.setDescription(info.getDescription());
        dto.setType(info.isAsync() ? "async" : "sync");
        return dto;
    }

    private <T> PageResult<T> paginate(List<T> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            return PageResult.empty();
        }

        List<T> pageList = list.subList(start, end);
        return PageResult.of(pageList, total, pageNum, pageSize);
    }
}
