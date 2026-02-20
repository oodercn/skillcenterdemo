package net.ooder.skillcenter.sdk;

import net.ooder.sdk.api.scene.SceneGroupManager;
import net.ooder.sdk.api.scene.SceneGroup;
import net.ooder.sdk.api.scene.SceneMember;
import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
public class SceneGroupSdkAdapterImpl implements SceneGroupSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(SceneGroupSdkAdapterImpl.class);

    @Autowired
    private SdkConfig sdkConfig;

    @Autowired
    private SceneGroupSdkAdapterMockImpl mockAdapter;

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    private SceneGroupManager sceneGroupManager;
    private boolean sdkAvailable = false;

    @PostConstruct
    public void init() {
        if (sdkConfig.isMockMode()) {
            log.info("[SceneGroupSdkAdapter] Running in mock mode");
            return;
        }
        
        log.info("[SceneGroupSdkAdapter] Checking SDK availability...");
        sdkAvailable = checkSdkAvailability();
        
        if (sdkAvailable) {
            log.info("[SceneGroupSdkAdapter] SDK is available, using real implementation");
        } else {
            log.warn("[SceneGroupSdkAdapter] SDK scene group APIs not available, falling back to mock");
        }
    }

    private boolean checkSdkAvailability() {
        if (sdkWrapper != null && sdkWrapper.isInitialized()) {
            sceneGroupManager = sdkWrapper.getSceneGroupManager();
            return sceneGroupManager != null;
        }
        return false;
    }

    @Override
    public SceneGroupDTO createSceneGroup(String sceneId, SceneGroupConfigDTO config) {
        if (!sdkAvailable || sceneGroupManager == null) {
            return mockAdapter.createSceneGroup(sceneId, config);
        }
        
        log.debug("[SceneGroupSdkAdapter] Creating scene group via SDK for scene: {}", sceneId);
        try {
            SceneGroupManager.SceneGroupConfig sdkConfig = new SceneGroupManager.SceneGroupConfig();
            SceneGroup group = sceneGroupManager.create(sceneId, sdkConfig).get();
            SceneGroupDTO dto = new SceneGroupDTO();
            dto.setSceneId(group.getSceneId());
            return dto;
        } catch (Exception e) {
            log.error("[SceneGroupSdkAdapter] Failed to create scene group: {}", e.getMessage());
            return mockAdapter.createSceneGroup(sceneId, config);
        }
    }

    @Override
    public boolean destroySceneGroup(String sceneGroupId) {
        if (!sdkAvailable || sceneGroupManager == null) {
            return mockAdapter.destroySceneGroup(sceneGroupId);
        }
        
        log.debug("[SceneGroupSdkAdapter] Destroying scene group via SDK: {}", sceneGroupId);
        try {
            sceneGroupManager.destroy(sceneGroupId).get();
            return true;
        } catch (Exception e) {
            log.error("[SceneGroupSdkAdapter] Failed to destroy scene group: {}", e.getMessage());
            return mockAdapter.destroySceneGroup(sceneGroupId);
        }
    }

    @Override
    public SceneGroupDTO getSceneGroup(String sceneGroupId) {
        return mockAdapter.getSceneGroup(sceneGroupId);
    }

    @Override
    public PageResult<SceneGroupDTO> listSceneGroups(int pageNum, int pageSize) {
        if (!sdkAvailable || sceneGroupManager == null) {
            return mockAdapter.listSceneGroups(pageNum, pageSize);
        }
        
        try {
            List<SceneGroup> groups = sceneGroupManager.listAll().get();
            List<SceneGroupDTO> dtos = groups.stream()
                .map(group -> {
                    SceneGroupDTO dto = new SceneGroupDTO();
                    dto.setSceneId(group.getSceneId());
                    return dto;
                })
                .collect(Collectors.toList());
            return paginate(dtos, pageNum, pageSize);
        } catch (Exception e) {
            log.error("[SceneGroupSdkAdapter] Failed to list scene groups: {}", e.getMessage());
            return mockAdapter.listSceneGroups(pageNum, pageSize);
        }
    }

    @Override
    public boolean joinSceneGroup(String sceneGroupId, String agentId, String role) {
        if (!sdkAvailable || sceneGroupManager == null) {
            return mockAdapter.joinSceneGroup(sceneGroupId, agentId, role);
        }
        
        log.debug("[SceneGroupSdkAdapter] Agent {} joining scene group via SDK: {}", agentId, sceneGroupId);
        try {
            sceneGroupManager.join(sceneGroupId, agentId, null).get();
            return true;
        } catch (Exception e) {
            log.error("[SceneGroupSdkAdapter] Failed to join scene group: {}", e.getMessage());
            return mockAdapter.joinSceneGroup(sceneGroupId, agentId, role);
        }
    }

    @Override
    public boolean leaveSceneGroup(String sceneGroupId, String agentId) {
        if (!sdkAvailable || sceneGroupManager == null) {
            return mockAdapter.leaveSceneGroup(sceneGroupId, agentId);
        }
        
        log.debug("[SceneGroupSdkAdapter] Agent {} leaving scene group via SDK: {}", agentId, sceneGroupId);
        try {
            sceneGroupManager.leave(sceneGroupId, agentId).get();
            return true;
        } catch (Exception e) {
            log.error("[SceneGroupSdkAdapter] Failed to leave scene group: {}", e.getMessage());
            return mockAdapter.leaveSceneGroup(sceneGroupId, agentId);
        }
    }

    @Override
    public PageResult<SceneMemberDTO> listMembers(String sceneGroupId, int pageNum, int pageSize) {
        if (!sdkAvailable || sceneGroupManager == null) {
            return mockAdapter.listMembers(sceneGroupId, pageNum, pageSize);
        }
        
        try {
            List<SceneMember> members = sceneGroupManager.listMembers(sceneGroupId).get();
            List<SceneMemberDTO> dtos = members.stream()
                .map(member -> {
                    SceneMemberDTO dto = new SceneMemberDTO();
                    dto.setAgentId(member.getAgentId());
                    return dto;
                })
                .collect(Collectors.toList());
            return paginate(dtos, pageNum, pageSize);
        } catch (Exception e) {
            log.error("[SceneGroupSdkAdapter] Failed to list members: {}", e.getMessage());
            return mockAdapter.listMembers(sceneGroupId, pageNum, pageSize);
        }
    }

    @Override
    public SceneMemberDTO getPrimaryMember(String sceneGroupId) {
        if (!sdkAvailable || sceneGroupManager == null) {
            return mockAdapter.getPrimaryMember(sceneGroupId);
        }
        
        try {
            SceneMember primary = sceneGroupManager.getPrimary(sceneGroupId).get();
            if (primary == null) return null;
            SceneMemberDTO dto = new SceneMemberDTO();
            dto.setAgentId(primary.getAgentId());
            return dto;
        } catch (Exception e) {
            log.error("[SceneGroupSdkAdapter] Failed to get primary member: {}", e.getMessage());
            return mockAdapter.getPrimaryMember(sceneGroupId);
        }
    }

    @Override
    public boolean handleFailover(String sceneGroupId, String failedMemberId) {
        if (!sdkAvailable || sceneGroupManager == null) {
            return mockAdapter.handleFailover(sceneGroupId, failedMemberId);
        }
        
        log.debug("[SceneGroupSdkAdapter] Handling failover via SDK for scene group: {}", sceneGroupId);
        try {
            sceneGroupManager.handleFailover(sceneGroupId, failedMemberId).get();
            return true;
        } catch (Exception e) {
            log.error("[SceneGroupSdkAdapter] Failed to handle failover: {}", e.getMessage());
            return mockAdapter.handleFailover(sceneGroupId, failedMemberId);
        }
    }

    @Override
    public FailoverStatusDTO getFailoverStatus(String sceneGroupId) {
        return mockAdapter.getFailoverStatus(sceneGroupId);
    }

    @Override
    public SceneGroupKeyDTO generateKey(String sceneGroupId) {
        return mockAdapter.generateKey(sceneGroupId);
    }

    @Override
    public SceneGroupKeyDTO reconstructKey(String sceneGroupId, List<KeyShareDTO> shares) {
        return mockAdapter.reconstructKey(sceneGroupId, shares);
    }

    @Override
    public boolean distributeKeyShares(String sceneGroupId, SceneGroupKeyDTO key) {
        return mockAdapter.distributeKeyShares(sceneGroupId, key);
    }

    @Override
    public VfsPermissionDTO getVfsPermission(String sceneGroupId, String agentId) {
        return mockAdapter.getVfsPermission(sceneGroupId, agentId);
    }

    @Override
    public PageResult<VfsPermissionDTO> listVfsPermissions(String sceneGroupId, int pageNum, int pageSize) {
        return mockAdapter.listVfsPermissions(sceneGroupId, pageNum, pageSize);
    }

    @Override
    public VfsPermissionDTO addVfsPermission(String sceneGroupId, String agentId, String permissionType, String path) {
        return mockAdapter.addVfsPermission(sceneGroupId, agentId, permissionType, path);
    }

    @Override
    public boolean isAvailable() {
        return sdkAvailable || mockAdapter.isAvailable();
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
