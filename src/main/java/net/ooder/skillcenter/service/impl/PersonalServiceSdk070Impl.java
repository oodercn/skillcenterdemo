package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.skill.Skill;
import net.ooder.sdk.skill.SkillManager;
import net.ooder.sdk.skill.SkillResult;
import net.ooder.skillcenter.dto.*;
import net.ooder.skillcenter.sdk.AgentSDKWrapper;
import net.ooder.skillcenter.service.PersonalService;
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
 * 个人中心服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class PersonalServiceSdk070Impl implements PersonalService {

    private static final Logger log = LoggerFactory.getLogger(PersonalServiceSdk070Impl.class);

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    private final Map<String, SkillDTO> skillStore = new ConcurrentHashMap<>();
    private final Map<String, GroupDTO> groupStore = new ConcurrentHashMap<>();
    private final Map<String, GroupSkillDTO> groupSkillStore = new ConcurrentHashMap<>();
    private final Map<String, GroupMemberDTO> groupMemberStore = new ConcurrentHashMap<>();
    private final Map<String, ExecutionRecordDTO> executionStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @javax.annotation.PostConstruct
    public void init() {
        initDefaultData();
    }

    private void initDefaultData() {
        addSkill("my-skill-1", "我的文本处理器", "处理文本", "text-processing");
        addSkill("my-skill-2", "我的代码生成器", "生成代码", "development");
        addGroup("my-group-1", "我的开发团队", "个人开发团队");
        addGroup("my-group-2", "我的测试团队", "个人测试团队");
        addExecution("exec-1", "my-skill-1", "我的文本处理器", "success");
        addExecution("exec-2", "my-skill-2", "我的代码生成器", "success");
        log.info("PersonalService initialized with {} skills, {} groups", skillStore.size(), groupStore.size());
    }

    private void addSkill(String id, String name, String description, String category) {
        SkillDTO skill = new SkillDTO();
        skill.setId(id);
        skill.setName(name);
        skill.setDescription(description);
        skill.setCategory(category);
        skill.setStatus("active");
        skill.setCreatedAt(new Date());
        skill.setUpdatedAt(new Date());
        skillStore.put(id, skill);
    }

    private void addGroup(String id, String name, String description) {
        GroupDTO group = new GroupDTO();
        group.setId(id);
        group.setName(name);
        group.setDescription(description);
        group.setMemberCount(5);
        group.setCreatedAt(new Date());
        group.setUpdatedAt(new Date());
        groupStore.put(id, group);
    }

    private void addExecution(String id, String skillId, String skillName, String status) {
        ExecutionRecordDTO exec = new ExecutionRecordDTO();
        exec.setId(id);
        exec.setSkillId(skillId);
        exec.setSkillName(skillName);
        exec.setStatus(status);
        exec.setExecutedAt(new Date());
        exec.setExecutionTime((long) (Math.random() * 1000));
        executionStore.put(id, exec);
    }

    @Override
    public Map<String, Object> getPersonalDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSkills", skillStore.size());
        stats.put("totalGroups", groupStore.size());
        stats.put("totalExecutions", executionStore.size());
        stats.put("successfulExecutions", executionStore.values().stream()
            .filter(e -> "success".equals(e.getStatus())).count());
        stats.put("sdkInitialized", sdkWrapper.isInitialized());
        return stats;
    }

    @Override
    public List<SkillDTO> getMySkills() {
        List<SkillDTO> skills = new ArrayList<>(skillStore.values());
        
        if (sdkWrapper.isInitialized()) {
            SkillManager skillManager = sdkWrapper.getSkillManager();
            if (skillManager != null) {
                Map<String, Skill> sdkSkills = skillManager.getAllSkills();
                for (Map.Entry<String, Skill> entry : sdkSkills.entrySet()) {
                    SkillDTO dto = new SkillDTO();
                    dto.setId(entry.getKey());
                    dto.setName(entry.getValue().getName());
                    dto.setStatus("active");
                    skills.add(dto);
                }
            }
        }
        
        return skills;
    }

    @Override
    public SkillDTO getSkillDetail(String skillId) {
        return skillStore.get(skillId);
    }

    @Override
    public SkillDTO createSkill(SkillDTO skillDTO) {
        String id = "my-skill-" + idGenerator.getAndIncrement();
        skillDTO.setId(id);
        skillDTO.setCreatedAt(new Date());
        skillDTO.setUpdatedAt(new Date());
        skillStore.put(id, skillDTO);
        log.info("Created skill: {}", id);
        return skillDTO;
    }

    @Override
    public SkillDTO updateSkill(String skillId, SkillDTO skillDTO) {
        SkillDTO existing = skillStore.get(skillId);
        if (existing == null) return null;
        skillDTO.setId(skillId);
        skillDTO.setCreatedAt(existing.getCreatedAt());
        skillDTO.setUpdatedAt(new Date());
        skillStore.put(skillId, skillDTO);
        return skillDTO;
    }

    @Override
    public boolean deleteSkill(String skillId) {
        return skillStore.remove(skillId) != null;
    }

    @Override
    public SkillResultDTO executeSkill(String skillId, Map<String, Object> parameters) {
        SkillResultDTO result = new SkillResultDTO();
        result.setExecutionId("exec-" + idGenerator.getAndIncrement());
        result.setSkillId(skillId);
        
        if (sdkWrapper.isInitialized()) {
            try {
                SkillManager skillManager = sdkWrapper.getSkillManager();
                if (skillManager != null) {
                    SkillResult sdkResult = skillManager.executeSkill(skillId, parameters);
                    result.setStatus(sdkResult.isSuccess() ? "success" : "error");
                    result.setOutput(sdkResult.getErrorMessage() != null ? sdkResult.getErrorMessage() : "");
                    if (sdkResult.getData() != null) {
                        result.setOutput(sdkResult.getData().toString());
                    }
                } else {
                    result.setStatus("success");
                    result.setOutput("执行结果: " + parameters);
                }
            } catch (Exception e) {
                result.setStatus("error");
                result.setOutput(e.getMessage());
            }
        } else {
            result.setStatus("success");
            result.setOutput("执行结果 (SDK未初始化): " + parameters);
        }
        
        result.setExecutedAt(new Date());
        result.setExecutionTime(100L);
        return result;
    }

    @Override
    public List<GroupDTO> getMyGroups() {
        return new ArrayList<>(groupStore.values());
    }

    @Override
    public GroupDTO getGroupById(String groupId) {
        return groupStore.get(groupId);
    }

    @Override
    public GroupDTO createGroup(GroupDTO group) {
        String id = "my-group-" + idGenerator.getAndIncrement();
        group.setId(id);
        group.setCreatedAt(new Date());
        group.setUpdatedAt(new Date());
        groupStore.put(id, group);
        log.info("Created group: {}", id);
        return group;
    }

    @Override
    public GroupDTO updateGroup(String groupId, GroupDTO group) {
        GroupDTO existing = groupStore.get(groupId);
        if (existing == null) return null;
        group.setId(groupId);
        group.setCreatedAt(existing.getCreatedAt());
        group.setUpdatedAt(new Date());
        groupStore.put(groupId, group);
        return group;
    }

    @Override
    public boolean deleteGroup(String groupId) {
        return groupStore.remove(groupId) != null;
    }

    @Override
    public List<GroupSkillDTO> getAllGroupSkills() {
        return new ArrayList<>(groupSkillStore.values());
    }

    @Override
    public List<GroupSkillDTO> getGroupSkills(String groupId) {
        return groupSkillStore.values().stream()
            .filter(s -> groupId.equals(s.getGroupId()))
            .collect(Collectors.toList());
    }

    @Override
    public GroupSkillDTO addGroupSkill(GroupSkillDTO groupSkill) {
        String id = "gs-" + idGenerator.getAndIncrement();
        groupSkill.setId(id);
        groupSkill.setAddedAt(new Date());
        groupSkillStore.put(id, groupSkill);
        return groupSkill;
    }

    @Override
    public boolean deleteGroupSkill(String skillId) {
        return groupSkillStore.remove(skillId) != null;
    }

    @Override
    public List<GroupMemberDTO> getGroupMembers(String groupId) {
        return groupMemberStore.values().stream()
            .filter(m -> groupId.equals(m.getGroupId()))
            .collect(Collectors.toList());
    }

    @Override
    public GroupMemberDTO addGroupMember(String groupId, GroupMemberDTO member) {
        String id = "gm-" + idGenerator.getAndIncrement();
        member.setId(id);
        member.setGroupId(groupId);
        member.setJoinedAt(new Date());
        groupMemberStore.put(id, member);
        return member;
    }

    @Override
    public GroupMemberDTO updateGroupMember(String groupId, String memberId, GroupMemberDTO member) {
        GroupMemberDTO existing = groupMemberStore.get(memberId);
        if (existing == null) return null;
        member.setId(memberId);
        member.setGroupId(groupId);
        groupMemberStore.put(memberId, member);
        return member;
    }

    @Override
    public boolean deleteGroupMember(String groupId, String memberId) {
        return groupMemberStore.remove(memberId) != null;
    }

    @Override
    public List<ExecutionRecordDTO> getExecutionHistory() {
        return new ArrayList<>(executionStore.values());
    }

    @Override
    public ExecutionRecordDTO getExecutionById(String executionId) {
        return executionStore.get(executionId);
    }

    @Override
    public boolean deleteExecution(String executionId) {
        return executionStore.remove(executionId) != null;
    }

    @Override
    public boolean clearExecutionHistory() {
        executionStore.clear();
        return true;
    }

    @Override
    public UserDTO getPersonalIdentity() {
        UserDTO user = new UserDTO();
        user.setId("user-sdk");
        user.setUsername("sdk-user");
        user.setName("SDK用户");
        user.setEmail("sdk@example.com");
        return user;
    }

    @Override
    public UserDTO updatePersonalIdentity(UserDTO user) {
        return user;
    }

    @Override
    public List<IdentityMappingDTO> getIdentityMappings() {
        List<IdentityMappingDTO> mappings = new ArrayList<>();
        IdentityMappingDTO mapping = new IdentityMappingDTO();
        mapping.setId("1");
        mapping.setType("sdk");
        mapping.setIdentifier("sdk-identity");
        mapping.setStatus("verified");
        mapping.setLinkedAt(new Date());
        mappings.add(mapping);
        return mappings;
    }

    @Override
    public Map<String, Object> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("quickStart", Arrays.asList("发布技能", "管理技能", "技能市场"));
        help.put("faq", Arrays.asList("如何发布技能？", "技能审核需要多长时间？"));
        help.put("sdkVersion", "0.7.0");
        return help;
    }

    @Override
    public Map<String, Object> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("theme", "light");
        settings.put("language", "zh-CN");
        settings.put("sdkMode", true);
        return settings;
    }

    @Override
    public boolean updateSettings(Map<String, Object> settings) {
        return true;
    }

    @Override
    public Map<String, Object> getFeatureFlags() {
        Map<String, Object> features = new HashMap<>();
        features.put("skillPublishing", true);
        features.put("skillMarket", true);
        features.put("skillExecution", sdkWrapper.isInitialized());
        features.put("groupManagement", true);
        features.put("sdkMode", true);
        return features;
    }
}
