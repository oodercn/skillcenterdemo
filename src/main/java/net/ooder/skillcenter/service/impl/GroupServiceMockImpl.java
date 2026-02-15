package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.GroupDTO;
import net.ooder.skillcenter.service.GroupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class GroupServiceMockImpl implements GroupService {

    private final Map<String, GroupDTO> groupStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        initMockData();
    }

    private void initMockData() {
        addMockGroup("group-001", "开发团队", "负责产品开发的团队", 5);
        addMockGroup("group-002", "测试团队", "负责质量测试的团队", 3);
        addMockGroup("group-003", "运维团队", "负责系统运维的团队", 2);
    }

    private void addMockGroup(String id, String name, String description, int memberCount) {
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
        return groupDTO;
    }

    @Override
    public boolean deleteGroup(String groupId) {
        return groupStore.remove(groupId) != null;
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
