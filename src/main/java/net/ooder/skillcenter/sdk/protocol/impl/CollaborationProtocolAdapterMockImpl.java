package net.ooder.skillcenter.sdk.protocol.impl;

import net.ooder.nexus.skillcenter.dto.protocol.CollaborationDTO;
import net.ooder.nexus.skillcenter.dto.protocol.CollaborationDTO.InvitationDTO;
import net.ooder.nexus.skillcenter.dto.protocol.CollaborationDTO.JoinRequestDTO;
import net.ooder.nexus.skillcenter.dto.protocol.CollaborationDTO.MemberDTO;
import net.ooder.nexus.skillcenter.dto.protocol.CollaborationDTO.SceneGroupInfoDTO;
import net.ooder.nexus.skillcenter.dto.protocol.CollaborationDTO.TaskDTO;
import net.ooder.nexus.skillcenter.dto.protocol.CollaborationDTO.TaskResultDTO;
import net.ooder.skillcenter.sdk.protocol.CollaborationProtocolAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CollaborationProtocolAdapterMockImpl implements CollaborationProtocolAdapter {

    private static final Logger log = LoggerFactory.getLogger(CollaborationProtocolAdapterMockImpl.class);

    private final List<CollaborationEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, SceneGroupInfoDTO> groups = new ConcurrentHashMap<>();
    private final Map<String, List<InvitationDTO>> invitations = new ConcurrentHashMap<>();
    private final Map<String, List<TaskDTO>> pendingTasks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> groupStates = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<SceneGroupInfoDTO> joinSceneGroup(String groupId, JoinRequestDTO request) {
        log.debug("[CollaborationMock] Join scene group: {} with agent: {}", groupId, request.getAgentId());
        return CompletableFuture.supplyAsync(() -> {
            SceneGroupInfoDTO group = groups.computeIfAbsent(groupId, id -> createMockGroup(id));
            if (!group.getMemberIds().contains(request.getAgentId())) {
                group.getMemberIds().add(request.getAgentId());
                group.setMemberCount(group.getMemberIds().size());
            }
            return group;
        });
    }

    @Override
    public CompletableFuture<Void> leaveSceneGroup(String groupId) {
        log.debug("[CollaborationMock] Leave scene group: {}", groupId);
        return CompletableFuture.runAsync(() -> {
            groups.remove(groupId);
        });
    }

    @Override
    public CompletableFuture<Void> acceptInvitation(String invitationId) {
        log.debug("[CollaborationMock] Accept invitation: {}", invitationId);
        return CompletableFuture.runAsync(() -> {
            invitations.values().forEach(list -> {
                list.forEach(inv -> {
                    if (invitationId.equals(inv.getInvitationId())) {
                        inv.setStatus("ACCEPTED");
                    }
                });
            });
        });
    }

    @Override
    public CompletableFuture<Void> declineInvitation(String invitationId) {
        log.debug("[CollaborationMock] Decline invitation: {}", invitationId);
        return CompletableFuture.runAsync(() -> {
            invitations.values().forEach(list -> {
                list.forEach(inv -> {
                    if (invitationId.equals(inv.getInvitationId())) {
                        inv.setStatus("DECLINED");
                    }
                });
            });
        });
    }

    @Override
    public CompletableFuture<List<InvitationDTO>> getPendingInvitations() {
        log.debug("[CollaborationMock] Get pending invitations");
        return CompletableFuture.supplyAsync(() -> {
            List<InvitationDTO> allInvitations = new ArrayList<>();
            invitations.values().forEach(allInvitations::addAll);
            return allInvitations;
        });
    }

    @Override
    public CompletableFuture<TaskDTO> receiveTask(String groupId) {
        log.debug("[CollaborationMock] Receive task from group: {}", groupId);
        return CompletableFuture.supplyAsync(() -> {
            List<TaskDTO> tasks = pendingTasks.get(groupId);
            if (tasks != null && !tasks.isEmpty()) {
                return tasks.remove(0);
            }
            return createMockTask(groupId);
        });
    }

    @Override
    public CompletableFuture<Void> submitTaskResult(String groupId, String taskId, TaskResultDTO result) {
        log.debug("[CollaborationMock] Submit task result: {} for group: {}", taskId, groupId);
        return CompletableFuture.runAsync(() -> {
            log.info("[CollaborationMock] Task {} completed with success: {}", taskId, result.isSuccess());
        });
    }

    @Override
    public CompletableFuture<List<TaskDTO>> getPendingTasks(String groupId) {
        log.debug("[CollaborationMock] Get pending tasks for group: {}", groupId);
        return CompletableFuture.supplyAsync(() -> {
            List<TaskDTO> tasks = pendingTasks.computeIfAbsent(groupId, id -> new ArrayList<>());
            if (tasks.isEmpty()) {
                tasks.add(createMockTask(groupId));
            }
            return new ArrayList<>(tasks);
        });
    }

    @Override
    public CompletableFuture<Void> syncState(String groupId, Map<String, Object> state) {
        log.debug("[CollaborationMock] Sync state for group: {}", groupId);
        return CompletableFuture.runAsync(() -> {
            groupStates.put(groupId, new HashMap<>(state));
        });
    }

    @Override
    public CompletableFuture<Map<String, Object>> getGroupState(String groupId) {
        log.debug("[CollaborationMock] Get state for group: {}", groupId);
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> state = groupStates.get(groupId);
            if (state == null) {
                state = new HashMap<>();
                state.put("status", "active");
                state.put("lastUpdate", System.currentTimeMillis());
            }
            return state;
        });
    }

    @Override
    public CompletableFuture<List<MemberDTO>> getGroupMembers(String groupId) {
        log.debug("[CollaborationMock] Get members for group: {}", groupId);
        return CompletableFuture.supplyAsync(() -> createMockMembers());
    }

    @Override
    public void addCollaborationListener(CollaborationEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeCollaborationListener(CollaborationEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private SceneGroupInfoDTO createMockGroup(String groupId) {
        SceneGroupInfoDTO group = new SceneGroupInfoDTO();
        group.setGroupId(groupId);
        group.setGroupName("Scene-Group-" + groupId.substring(0, 4));
        group.setSceneId("scene-001");
        group.setPrimaryId("agent-primary");
        group.setMemberIds(new ArrayList<>());
        group.getMemberIds().add("agent-primary");
        group.setMemberCount(1);
        group.setStatus("ACTIVE");
        group.setCreatedAt(System.currentTimeMillis());
        group.setConfig(new HashMap<>());
        return group;
    }

    private TaskDTO createMockTask(String groupId) {
        TaskDTO task = new TaskDTO();
        task.setTaskId("task-" + UUID.randomUUID().toString().substring(0, 8));
        task.setGroupId(groupId);
        task.setTaskType("EXECUTION");
        task.setTaskName("Execute Skill");
        task.setParameters(new HashMap<>());
        task.setPriority(1);
        task.setCreatedAt(System.currentTimeMillis());
        task.setDeadline(System.currentTimeMillis() + 3600000L);
        task.setStatus("PENDING");
        return task;
    }

    private List<MemberDTO> createMockMembers() {
        List<MemberDTO> members = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            MemberDTO member = new MemberDTO();
            member.setMemberId("member-" + i);
            member.setMemberName("Agent-" + i);
            member.setRole(i == 1 ? "PRIMARY" : "MEMBER");
            member.setStatus("ONLINE");
            member.setJoinedAt(System.currentTimeMillis() - (i * 3600000L));
            member.setLastActiveAt(System.currentTimeMillis());
            members.add(member);
        }
        return members;
    }
}
