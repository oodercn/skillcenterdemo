package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import net.ooder.skillcenter.service.SceneGroupService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SceneGroupServiceMockImpl implements SceneGroupService {

    private final Map<String, SceneGroupDTO> groupStore = new ConcurrentHashMap<>();
    private final Map<String, List<SceneMemberDTO>> memberStore = new ConcurrentHashMap<>();
    private final Map<String, FailoverStatusDTO> failoverStore = new ConcurrentHashMap<>();
    private final Map<String, SceneGroupKeyDTO> keyStore = new ConcurrentHashMap<>();

    @Override
    public SceneGroupDTO create(String sceneId, SceneGroupConfigDTO config) {
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
        return group;
    }

    @Override
    public boolean destroy(String sceneGroupId) {
        groupStore.remove(sceneGroupId);
        memberStore.remove(sceneGroupId);
        failoverStore.remove(sceneGroupId);
        keyStore.remove(sceneGroupId);
        return true;
    }

    @Override
    public SceneGroupDTO get(String sceneGroupId) {
        return groupStore.get(sceneGroupId);
    }

    @Override
    public PageResult<SceneGroupDTO> listAll(int pageNum, int pageSize) {
        List<SceneGroupDTO> all = new ArrayList<>(groupStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public PageResult<SceneGroupDTO> listByScene(String sceneId, int pageNum, int pageSize) {
        List<SceneGroupDTO> filtered = groupStore.values().stream()
            .filter(g -> sceneId.equals(g.getSceneId()))
            .collect(Collectors.toList());
        return paginate(filtered, pageNum, pageSize);
    }

    @Override
    public boolean join(String sceneGroupId, String agentId, String role) {
        SceneGroupDTO group = groupStore.get(sceneGroupId);
        if (group == null) return false;
        
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) return false;
        
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
        
        if ("primary".equals(role)) {
            group.setPrimaryAgentId(agentId);
        }
        
        return true;
    }

    @Override
    public boolean leave(String sceneGroupId, String agentId) {
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) return false;
        
        boolean removed = members.removeIf(m -> agentId.equals(m.getAgentId()));
        if (removed) {
            SceneGroupDTO group = groupStore.get(sceneGroupId);
            if (group != null) {
                group.setMemberCount(members.size());
            }
        }
        return removed;
    }

    @Override
    public boolean changeRole(String sceneGroupId, String agentId, String newRole) {
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) return false;
        
        for (SceneMemberDTO member : members) {
            if (agentId.equals(member.getAgentId())) {
                member.setRole(newRole);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getRole(String sceneGroupId, String agentId) {
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) return null;
        
        for (SceneMemberDTO member : members) {
            if (agentId.equals(member.getAgentId())) {
                return member.getRole();
            }
        }
        return null;
    }

    @Override
    public PageResult<SceneMemberDTO> listMembers(String sceneGroupId, int pageNum, int pageSize) {
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize);
        return paginate(members, pageNum, pageSize);
    }

    @Override
    public SceneMemberDTO getPrimary(String sceneGroupId) {
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) return null;
        
        for (SceneMemberDTO member : members) {
            if ("primary".equals(member.getRole())) {
                return member;
            }
        }
        return null;
    }

    @Override
    public PageResult<SceneMemberDTO> getBackups(String sceneGroupId, int pageNum, int pageSize) {
        List<SceneMemberDTO> members = memberStore.get(sceneGroupId);
        if (members == null) return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize);
        
        List<SceneMemberDTO> backups = members.stream()
            .filter(m -> "backup".equals(m.getRole()))
            .collect(Collectors.toList());
        return paginate(backups, pageNum, pageSize);
    }

    @Override
    public boolean handleFailover(String sceneGroupId, String failedMemberId) {
        SceneGroupDTO group = groupStore.get(sceneGroupId);
        if (group == null) return false;
        
        FailoverStatusDTO status = new FailoverStatusDTO();
        status.setSceneGroupId(sceneGroupId);
        status.setInProgress(true);
        status.setFailedMemberId(failedMemberId);
        status.setStartTime(System.currentTimeMillis());
        status.setPhase("detecting");
        failoverStore.put(sceneGroupId, status);
        
        return true;
    }

    @Override
    public FailoverStatusDTO getFailoverStatus(String sceneGroupId) {
        return failoverStore.get(sceneGroupId);
    }

    @Override
    public boolean startHeartbeat(String sceneGroupId) {
        SceneGroupDTO group = groupStore.get(sceneGroupId);
        return group != null;
    }

    @Override
    public boolean stopHeartbeat(String sceneGroupId) {
        SceneGroupDTO group = groupStore.get(sceneGroupId);
        return group != null;
    }

    @Override
    public SceneGroupKeyDTO generateKey(String sceneGroupId) {
        String keyId = "key-" + UUID.randomUUID().toString().substring(0, 8);
        SceneGroupKeyDTO key = new SceneGroupKeyDTO();
        key.setKeyId(keyId);
        key.setSceneGroupId(sceneGroupId);
        key.setKeyData(Base64.getEncoder().encodeToString(keyId.getBytes()));
        key.setCreateTime(System.currentTimeMillis());
        keyStore.put(sceneGroupId, key);
        return key;
    }

    @Override
    public SceneGroupKeyDTO reconstructKey(String sceneGroupId, List<KeyShareDTO> shares) {
        return keyStore.get(sceneGroupId);
    }

    @Override
    public boolean distributeKeyShares(String sceneGroupId, SceneGroupKeyDTO key) {
        SceneGroupDTO group = groupStore.get(sceneGroupId);
        return group != null;
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

    private <T> PageResult<T> paginate(List<T> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<T> pageData = start < total ? list.subList(start, end) : Collections.emptyList();
        return new PageResult<>(pageData, total, pageNum, pageSize);
    }
}
