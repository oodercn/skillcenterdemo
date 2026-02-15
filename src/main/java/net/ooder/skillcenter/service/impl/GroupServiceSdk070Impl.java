package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.AgentSDK;
import net.ooder.sdk.command.commands.GroupCreateCommand;
import net.ooder.sdk.command.commands.GroupDeleteCommand;
import net.ooder.sdk.command.model.CommandResult;
import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.GroupDTO;
import net.ooder.skillcenter.sdk.AgentSDKWrapper;
import net.ooder.skillcenter.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 团队服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class GroupServiceSdk070Impl implements GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupServiceSdk070Impl.class);

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    private final Map<String, GroupDTO> groupStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @javax.annotation.PostConstruct
    public void init() {
        initDefaultGroups();
    }

    private void initDefaultGroups() {
        addGroup("group-001", "开发团队", "负责产品开发的团队", 5);
        addGroup("group-002", "测试团队", "负责质量测试的团队", 3);
        addGroup("group-003", "运维团队", "负责系统运维的团队", 2);
        log.info("Initialized {} default groups", groupStore.size());
    }

    private void addGroup(String id, String name, String description, int memberCount) {
        GroupDTO group = new GroupDTO();
        group.setId(id);
        group.setName(name);
        group.setDescription(description);
        group.setMemberCount(memberCount);
        group.setCreatedAt(new Date());
        group.setUpdatedAt(new Date());
        groupStore.put(id, group);
    }

    @Override
    public PageResult<GroupDTO> getAllGroups(int pageNum, int pageSize) {
        List<GroupDTO> list = new ArrayList<>(groupStore.values());
        list.sort(Comparator.comparing(GroupDTO::getCreatedAt).reversed());
        return paginate(list, pageNum, pageSize);
    }

    @Override
    public PageResult<GroupDTO> searchGroups(String keyword, int pageNum, int pageSize) {
        List<GroupDTO> filtered = groupStore.values().stream()
            .filter(group -> keyword == null || keyword.isEmpty() ||
                group.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                (group.getDescription() != null && group.getDescription().toLowerCase().contains(keyword.toLowerCase())))
            .sorted(Comparator.comparing(GroupDTO::getCreatedAt).reversed())
            .collect(Collectors.toList());
        return paginate(filtered, pageNum, pageSize);
    }

    @Override
    public GroupDTO getGroupById(String groupId) {
        return groupStore.get(groupId);
    }

    @Override
    public GroupDTO createGroup(GroupDTO groupDTO) {
        String id = "group-" + idGenerator.getAndIncrement();
        groupDTO.setId(id);
        groupDTO.setCreatedAt(new Date());
        groupDTO.setUpdatedAt(new Date());
        groupStore.put(id, groupDTO);
        
        log.info("Created group: {} ({})", groupDTO.getName(), id);
        return groupDTO;
    }

    @Override
    public GroupDTO updateGroup(String groupId, GroupDTO groupDTO) {
        GroupDTO existing = groupStore.get(groupId);
        if (existing == null) {
            return null;
        }
        groupDTO.setId(groupId);
        groupDTO.setCreatedAt(existing.getCreatedAt());
        groupDTO.setUpdatedAt(new Date());
        groupStore.put(groupId, groupDTO);
        
        log.info("Updated group: {}", groupId);
        return groupDTO;
    }

    @Override
    public boolean deleteGroup(String groupId) {
        boolean removed = groupStore.remove(groupId) != null;
        if (removed) {
            log.info("Deleted group: {}", groupId);
        }
        return removed;
    }

    private PageResult<GroupDTO> paginate(List<GroupDTO> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            return PageResult.empty();
        }

        List<GroupDTO> pageList = list.subList(start, end);
        return PageResult.of(pageList, total, pageNum, pageSize);
    }
}
