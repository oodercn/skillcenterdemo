package net.ooder.skillcenter.sdk;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SceneGroupSdkAdapterMockImpl implements SceneGroupSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(SceneGroupSdkAdapterMockImpl.class);

    private final Map<String, SceneGroupDTO> groupStore = new ConcurrentHashMap<>();
    private final Map<String, List<SceneMemberDTO>> memberStore = new ConcurrentHashMap<>();
    private final Map<String, FailoverStatusDTO> failoverStore = new ConcurrentHashMap<>();
    private final Map<String, SceneGroupKeyDTO> keyStore = new ConcurrentHashMap<>();
    private final Map<String, List<VfsPermissionDTO>> vfsPermissionStore = new ConcurrentHashMap<>();

    @Override
    public SceneGroupDTO createSceneGroup(String sceneId, SceneGroupConfigDTO config) {
        log.debug("[MockAdapter] Creating scene group for scene: {}", sceneId);
        
        String groupId = "sg-" + UUID.randomUUID().toString().substring(0, 8);
        SceneGroupDTO group = new SceneGroupDTO();
        group.setSceneGroupId(groupId);
        group.setSceneId(sceneId);
        group.setName("SceneGroup-" + groupId);
        group.setStatus("active");
        group.setMemberCount(0);
        group.setCreateTime(System.currentTimeMillis());
        group.setLastUpdateTime(System.currentTimeMillis());
        
        groupStore.put(groupId, group);
        memberStore.put(groupId, new ArrayList<>());
        vfsPermissionStore.put(groupId, new ArrayList<>());
        
        log.info("[MockAdapter] Scene group created: {}", groupId);
        return group;
    }

    @Override
    public boolean destroySceneGroup(String sceneGroupId) {
        log.debug("[MockAdapter] Destroying scene group: {}", sceneGroupId);
        
        groupStore.remove(sceneGroupId);
        memberStore.remove(sceneGroupId);
        failoverStore.remove(sceneGroupId);
        keyStore.remove(sceneGroupId);
        vfsPermissionStore.remove(sceneGroupId);
        
        log.info("[MockAdapter] Scene group destroyed: {}", sceneGroupId);
        return true;
    }

    @Override
    public SceneGroupDTO getSceneGroup(String sceneGroupId) {
        return groupStore.get(sceneGroupId);
    }

    @Override
    public PageResult<SceneGroupDTO> listSceneGroups(int pageNum, int pageSize) {
        List<SceneGroupDTO> all = new ArrayList<>(groupStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public boolean joinSceneGroup(String sceneGroupId, String agentId, String role) {
        log.debug("[MockAdapter] Agent {} joining scene group {} as {}", agentId, sceneGroupId, role);
        
        SceneGroupDTO group = groupStore.get(sceneGroupId);
        if (group == null) {
            log.warn("[MockAdapter] Scene group not found: {}", sceneGroupId);
            return false;
        }
        
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) {
            return false;
        }
        
        SceneMemberDTO member = new SceneMemberDTO();
        member.setAgentId(agentId);
        member.setAgentName("Agent-" + agentId);
        member.setRole(role);
        member.setSceneGroupId(sceneGroupId);
        member.setJoinTime(System.currentTimeMillis());
        member.setLastHeartbeat(System.currentTimeMillis());
        member.setStatus("active");
        
        members.add(member);
        group.setMemberCount(members.size());
        
        if ("PRIMARY".equalsIgnoreCase(role)) {
            group.setPrimaryAgentId(agentId);
        }
        
        log.info("[MockAdapter] Agent {} joined scene group {}", agentId, sceneGroupId);
        return true;
    }

    @Override
    public boolean leaveSceneGroup(String sceneGroupId, String agentId) {
        log.debug("[MockAdapter] Agent {} leaving scene group {}", agentId, sceneGroupId);
        
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) {
            return false;
        }
        
        boolean removed = members.removeIf(m -> agentId.equals(m.getAgentId()));
        if (removed) {
            SceneGroupDTO group = groupStore.get(sceneGroupId);
            if (group != null) {
                group.setMemberCount(members.size());
            }
            log.info("[MockAdapter] Agent {} left scene group {}", agentId, sceneGroupId);
        }
        return removed;
    }

    @Override
    public PageResult<SceneMemberDTO> listMembers(String sceneGroupId, int pageNum, int pageSize) {
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) {
            return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize);
        }
        return paginate(members, pageNum, pageSize);
    }

    @Override
    public SceneMemberDTO getPrimaryMember(String sceneGroupId) {
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) {
            return null;
        }
        
        for (SceneMemberDTO member : members) {
            if ("PRIMARY".equalsIgnoreCase(member.getRole())) {
                return member;
            }
        }
        return null;
    }

    @Override
    public boolean handleFailover(String sceneGroupId, String failedMemberId) {
        log.debug("[MockAdapter] Handling failover for {} in scene group {}", failedMemberId, sceneGroupId);
        
        SceneGroupDTO group = groupStore.get(sceneGroupId);
        if (group == null) {
            return false;
        }
        
        FailoverStatusDTO status = new FailoverStatusDTO();
        status.setSceneGroupId(sceneGroupId);
        status.setInProgress(true);
        status.setFailedMemberId(failedMemberId);
        status.setStartTime(System.currentTimeMillis());
        status.setPhase("detecting");
        failoverStore.put(sceneGroupId, status);
        
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members != null) {
            for (SceneMemberDTO member : members) {
                if (failedMemberId.equals(member.getAgentId())) {
                    member.setStatus("failed");
                } else if ("BACKUP".equalsIgnoreCase(member.getRole())) {
                    member.setRole("PRIMARY");
                    group.setPrimaryAgentId(member.getAgentId());
                    status.setNewPrimaryId(member.getAgentId());
                    status.setPhase("completed");
                    break;
                }
            }
        }
        
        log.info("[MockAdapter] Failover completed for scene group {}", sceneGroupId);
        return true;
    }

    @Override
    public FailoverStatusDTO getFailoverStatus(String sceneGroupId) {
        return failoverStore.get(sceneGroupId);
    }

    @Override
    public SceneGroupKeyDTO generateKey(String sceneGroupId) {
        log.debug("[MockAdapter] Generating key for scene group {}", sceneGroupId);
        
        String keyId = "key-" + UUID.randomUUID().toString().substring(0, 8);
        SceneGroupKeyDTO key = new SceneGroupKeyDTO();
        key.setKeyId(keyId);
        key.setSceneGroupId(sceneGroupId);
        key.setKeyData(Base64.getEncoder().encodeToString(keyId.getBytes()));
        key.setCreateTime(System.currentTimeMillis());
        
        keyStore.put(sceneGroupId, key);
        
        log.info("[MockAdapter] Key generated for scene group {}", sceneGroupId);
        return key;
    }

    @Override
    public SceneGroupKeyDTO reconstructKey(String sceneGroupId, List<KeyShareDTO> shares) {
        log.debug("[MockAdapter] Reconstructing key for scene group {}", sceneGroupId);
        return keyStore.get(sceneGroupId);
    }

    @Override
    public boolean distributeKeyShares(String sceneGroupId, SceneGroupKeyDTO key) {
        log.debug("[MockAdapter] Distributing key shares for scene group {}", sceneGroupId);
        return groupStore.containsKey(sceneGroupId);
    }

    @Override
    public VfsPermissionDTO getVfsPermission(String sceneGroupId, String agentId) {
        VfsPermissionDTO permission = new VfsPermissionDTO();
        permission.setAgentId(agentId);
        permission.setSceneGroupId(sceneGroupId);
        permission.setReadablePaths(Arrays.asList("/data", "/config"));
        permission.setWritablePaths(Arrays.asList("/data"));
        permission.setFullAccess(false);
        return permission;
    }

    @Override
    public PageResult<VfsPermissionDTO> listVfsPermissions(String sceneGroupId, int pageNum, int pageSize) {
        List<VfsPermissionDTO> permissions = vfsPermissionStore.get(sceneGroupId);
        if (permissions == null) {
            return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize);
        }
        return paginate(permissions, pageNum, pageSize);
    }

    @Override
    public VfsPermissionDTO addVfsPermission(String sceneGroupId, String agentId, String permissionType, String path) {
        log.debug("[MockAdapter] Adding VFS permission for {} in scene group {}", agentId, sceneGroupId);
        
        List<VfsPermissionDTO> permissions = vfsPermissionStore.get(sceneGroupId);
        if (permissions == null) {
            permissions = new ArrayList<>();
            vfsPermissionStore.put(sceneGroupId, permissions);
        }
        
        VfsPermissionDTO permission = new VfsPermissionDTO();
        permission.setAgentId(agentId);
        permission.setSceneGroupId(sceneGroupId);
        permission.setPermissionType(permissionType);
        permission.setPath(path);
        permission.setStatus("有效");
        permissions.add(permission);
        
        log.info("[MockAdapter] VFS permission added for {} in scene group {}", agentId, sceneGroupId);
        return permission;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private <T> PageResult<T> paginate(List<T> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<T> pageData = start < total ? list.subList(start, end) : Collections.emptyList();
        return new PageResult<>(pageData, total, pageNum, pageSize);
    }
}
