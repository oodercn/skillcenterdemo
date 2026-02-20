package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import net.ooder.skillcenter.sdk.SceneGroupSdkAdapter;
import net.ooder.skillcenter.service.SceneGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SceneGroupServiceMockImpl implements SceneGroupService {

    @Autowired
    private SceneGroupSdkAdapter sdkAdapter;

    @Override
    public SceneGroupDTO create(String sceneId, SceneGroupConfigDTO config) {
        return sdkAdapter.createSceneGroup(sceneId, config);
    }

    @Override
    public boolean destroy(String sceneGroupId) {
        return sdkAdapter.destroySceneGroup(sceneGroupId);
    }

    @Override
    public SceneGroupDTO get(String sceneGroupId) {
        return sdkAdapter.getSceneGroup(sceneGroupId);
    }

    @Override
    public PageResult<SceneGroupDTO> listAll(int pageNum, int pageSize) {
        return sdkAdapter.listSceneGroups(pageNum, pageSize);
    }

    @Override
    public PageResult<SceneGroupDTO> listByScene(String sceneId, int pageNum, int pageSize) {
        PageResult<SceneGroupDTO> all = sdkAdapter.listSceneGroups(pageNum, 1000);
        List<SceneGroupDTO> filtered = new ArrayList<>();
        for (SceneGroupDTO group : all.getList()) {
            if (sceneId.equals(group.getSceneId())) {
                filtered.add(group);
            }
        }
        int total = filtered.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<SceneGroupDTO> pageData = start < total ? filtered.subList(start, end) : Collections.emptyList();
        return new PageResult<>(pageData, total, pageNum, pageSize);
    }

    @Override
    public boolean join(String sceneGroupId, String agentId, String role) {
        return sdkAdapter.joinSceneGroup(sceneGroupId, agentId, role);
    }

    @Override
    public boolean leave(String sceneGroupId, String agentId) {
        return sdkAdapter.leaveSceneGroup(sceneGroupId, agentId);
    }

    @Override
    public boolean changeRole(String sceneGroupId, String agentId, String newRole) {
        return true;
    }

    @Override
    public String getRole(String sceneGroupId, String agentId) {
        PageResult<SceneMemberDTO> members = sdkAdapter.listMembers(sceneGroupId, 1, 1000);
        for (SceneMemberDTO member : members.getList()) {
            if (agentId.equals(member.getAgentId())) {
                return member.getRole();
            }
        }
        return null;
    }

    @Override
    public PageResult<SceneMemberDTO> listMembers(String sceneGroupId, int pageNum, int pageSize) {
        return sdkAdapter.listMembers(sceneGroupId, pageNum, pageSize);
    }

    @Override
    public SceneMemberDTO getPrimary(String sceneGroupId) {
        return sdkAdapter.getPrimaryMember(sceneGroupId);
    }

    @Override
    public PageResult<SceneMemberDTO> getBackups(String sceneGroupId, int pageNum, int pageSize) {
        PageResult<SceneMemberDTO> all = sdkAdapter.listMembers(sceneGroupId, pageNum, 1000);
        List<SceneMemberDTO> backups = new ArrayList<>();
        for (SceneMemberDTO member : all.getList()) {
            if ("BACKUP".equalsIgnoreCase(member.getRole())) {
                backups.add(member);
            }
        }
        int total = backups.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<SceneMemberDTO> pageData = start < total ? backups.subList(start, end) : Collections.emptyList();
        return new PageResult<>(pageData, total, pageNum, pageSize);
    }

    @Override
    public boolean handleFailover(String sceneGroupId, String failedMemberId) {
        return sdkAdapter.handleFailover(sceneGroupId, failedMemberId);
    }

    @Override
    public FailoverStatusDTO getFailoverStatus(String sceneGroupId) {
        return sdkAdapter.getFailoverStatus(sceneGroupId);
    }

    @Override
    public boolean startHeartbeat(String sceneGroupId) {
        return sdkAdapter.getSceneGroup(sceneGroupId) != null;
    }

    @Override
    public boolean stopHeartbeat(String sceneGroupId) {
        return sdkAdapter.getSceneGroup(sceneGroupId) != null;
    }

    @Override
    public SceneGroupKeyDTO generateKey(String sceneGroupId) {
        return sdkAdapter.generateKey(sceneGroupId);
    }

    @Override
    public SceneGroupKeyDTO reconstructKey(String sceneGroupId, List<KeyShareDTO> shares) {
        return sdkAdapter.reconstructKey(sceneGroupId, shares);
    }

    @Override
    public boolean distributeKeyShares(String sceneGroupId, SceneGroupKeyDTO key) {
        return sdkAdapter.distributeKeyShares(sceneGroupId, key);
    }

    @Override
    public VfsPermissionDTO getVfsPermission(String sceneGroupId, String agentId) {
        return sdkAdapter.getVfsPermission(sceneGroupId, agentId);
    }

    @Override
    public PageResult<VfsPermissionDTO> listVfsPermissions(String sceneGroupId, int pageNum, int pageSize) {
        return sdkAdapter.listVfsPermissions(sceneGroupId, pageNum, pageSize);
    }

    @Override
    public VfsPermissionDTO addVfsPermission(String sceneGroupId, String agentId, String permissionType, String path) {
        return sdkAdapter.addVfsPermission(sceneGroupId, agentId, permissionType, path);
    }
}
