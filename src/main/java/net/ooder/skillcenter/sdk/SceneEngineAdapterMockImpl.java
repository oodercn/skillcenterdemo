package net.ooder.skillcenter.sdk;

import net.ooder.scene.provider.SecurityProvider;
import net.ooder.scene.provider.NetworkProvider;
import net.ooder.scene.provider.HostingProvider;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SceneEngineAdapterMockImpl implements SceneEngineAdapter {

    private static final Logger log = LoggerFactory.getLogger(SceneEngineAdapterMockImpl.class);

    private final Map<String, SkillInfoDTO> skills = new ConcurrentHashMap<>();
    private final Map<String, SceneInfoDTO> scenes = new ConcurrentHashMap<>();
    private final Map<String, SceneGroupInfoDTO> sceneGroups = new ConcurrentHashMap<>();
    private final Map<String, List<SceneMemberInfoDTO>> groupMembers = new ConcurrentHashMap<>();
    private final Map<String, List<CapabilityInfoDTO>> sceneCapabilities = new ConcurrentHashMap<>();

    private SecurityProvider securityProvider;
    private NetworkProvider networkProvider;
    private HostingProvider hostingProvider;

    @PostConstruct
    public void init() {
        log.info("[SceneEngineAdapterMockImpl] Initializing mock data...");
        initMockData();
        log.info("[SceneEngineAdapterMockImpl] Mock data initialized: {} skills, {} scenes, {} groups",
            skills.size(), scenes.size(), sceneGroups.size());
    }

    private void initMockData() {
        for (int i = 1; i <= 20; i++) {
            SkillInfoDTO skill = new SkillInfoDTO();
            skill.setSkillId("skill-" + String.format("%04d", i));
            skill.setName("Mock Skill " + i);
            skill.setVersion("1.0." + (i % 10));
            skill.setDescription("Mock skill for testing - " + i);
            skill.setAuthor("ooder");
            skill.setCategory(i % 2 == 0 ? "utility" : "ai");
            skill.setStatus("installed");
            skill.setInstalledAt(System.currentTimeMillis() - i * 3600000L);
            skill.setUpdatedAt(System.currentTimeMillis() - i * 1800000L);
            skills.put(skill.getSkillId(), skill);
        }

        for (int i = 1; i <= 5; i++) {
            SceneInfoDTO scene = new SceneInfoDTO();
            scene.setSceneId("scene-" + String.format("%04d", i));
            scene.setName("Mock Scene " + i);
            scene.setDescription("Mock scene for testing - " + i);
            scene.setStatus(i % 2 == 0 ? "active" : "inactive");
            scene.setCreatedAt(System.currentTimeMillis() - i * 86400000L);
            scene.setUpdatedAt(System.currentTimeMillis() - i * 43200000L);
            scenes.put(scene.getSceneId(), scene);

            List<CapabilityInfoDTO> caps = new ArrayList<>();
            for (int j = 1; j <= 3; j++) {
                CapabilityInfoDTO cap = new CapabilityInfoDTO();
                cap.setCapId("cap-" + i + "-" + j);
                cap.setSceneId(scene.getSceneId());
                cap.setName("Capability " + j);
                cap.setDescription("Mock capability " + j + " for scene " + i);
                cap.setType(j % 2 == 0 ? "input" : "output");
                cap.setStatus("active");
                caps.add(cap);
            }
            sceneCapabilities.put(scene.getSceneId(), caps);
        }

        for (int i = 1; i <= 3; i++) {
            SceneGroupInfoDTO group = new SceneGroupInfoDTO();
            group.setSceneGroupId("group-" + String.format("%04d", i));
            group.setSceneId("scene-" + String.format("%04d", i));
            group.setName("Mock Group " + i);
            group.setStatus("active");
            group.setMemberCount(i + 1);
            group.setCreatedAt(System.currentTimeMillis() - i * 172800000L);
            sceneGroups.put(group.getSceneGroupId(), group);

            List<SceneMemberInfoDTO> members = new ArrayList<>();
            for (int j = 1; j <= i + 1; j++) {
                SceneMemberInfoDTO member = new SceneMemberInfoDTO();
                member.setMemberId("member-" + i + "-" + j);
                member.setSceneGroupId(group.getSceneGroupId());
                member.setAgentId("agent-" + j);
                member.setRole(j == 1 ? "primary" : "secondary");
                member.setStatus("online");
                member.setJoinedAt(System.currentTimeMillis() - j * 3600000L);
                members.add(member);
            }
            groupMembers.put(group.getSceneGroupId(), members);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    @Override
    public NetworkProvider getNetworkProvider() {
        return networkProvider;
    }

    @Override
    public HostingProvider getHostingProvider() {
        return hostingProvider;
    }

    public void setSecurityProvider(SecurityProvider provider) {
        this.securityProvider = provider;
    }

    public void setNetworkProvider(NetworkProvider provider) {
        this.networkProvider = provider;
    }

    public void setHostingProvider(HostingProvider provider) {
        this.hostingProvider = provider;
    }

    @Override
    public List<SkillInfoDTO> listInstalledSkills() {
        return new ArrayList<>(skills.values());
    }

    @Override
    public SkillInfoDTO getSkill(String skillId) {
        return skills.get(skillId);
    }

    @Override
    public SkillInstallResultDTO installSkill(String skillId) {
        if (skills.containsKey(skillId)) {
            return SkillInstallResultDTO.failure("Skill already installed");
        }

        SkillInfoDTO skill = new SkillInfoDTO();
        skill.setSkillId(skillId);
        skill.setName("New Skill " + skillId);
        skill.setVersion("1.0.0");
        skill.setDescription("Newly installed skill");
        skill.setStatus("installed");
        skill.setInstalledAt(System.currentTimeMillis());
        skills.put(skillId, skill);

        return SkillInstallResultDTO.success(skillId);
    }

    @Override
    public SkillUninstallResultDTO uninstallSkill(String skillId) {
        if (!skills.containsKey(skillId)) {
            return SkillUninstallResultDTO.failure("Skill not found");
        }

        skills.remove(skillId);
        return SkillUninstallResultDTO.success(skillId);
    }

    @Override
    public SceneInfoDTO createScene(SceneInfoDTO scene) {
        String sceneId = "scene-" + UUID.randomUUID().toString().substring(0, 8);
        scene.setSceneId(sceneId);
        scene.setStatus("inactive");
        scene.setCreatedAt(System.currentTimeMillis());
        scene.setUpdatedAt(System.currentTimeMillis());
        scenes.put(sceneId, scene);
        sceneCapabilities.put(sceneId, new ArrayList<>());
        return scene;
    }

    @Override
    public SceneInfoDTO getScene(String sceneId) {
        return scenes.get(sceneId);
    }

    @Override
    public List<SceneInfoDTO> listScenes() {
        return new ArrayList<>(scenes.values());
    }

    @Override
    public boolean activateScene(String sceneId) {
        SceneInfoDTO scene = scenes.get(sceneId);
        if (scene != null) {
            scene.setStatus("active");
            scene.setUpdatedAt(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateScene(String sceneId) {
        SceneInfoDTO scene = scenes.get(sceneId);
        if (scene != null) {
            scene.setStatus("inactive");
            scene.setUpdatedAt(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    public boolean addCapability(String sceneId, CapabilityInfoDTO capability) {
        List<CapabilityInfoDTO> caps = sceneCapabilities.computeIfAbsent(sceneId, k -> new ArrayList<>());
        capability.setCapId("cap-" + UUID.randomUUID().toString().substring(0, 8));
        capability.setSceneId(sceneId);
        capability.setStatus("active");
        caps.add(capability);
        return true;
    }

    @Override
    public boolean removeCapability(String sceneId, String capabilityId) {
        List<CapabilityInfoDTO> caps = sceneCapabilities.get(sceneId);
        if (caps != null) {
            caps.removeIf(c -> c.getCapId().equals(capabilityId));
            return true;
        }
        return false;
    }

    @Override
    public List<CapabilityInfoDTO> listCapabilities(String sceneId) {
        return sceneCapabilities.getOrDefault(sceneId, new ArrayList<>());
    }

    @Override
    public CapabilityInfoDTO getCapability(String sceneId, String capabilityId) {
        List<CapabilityInfoDTO> caps = sceneCapabilities.get(sceneId);
        if (caps != null) {
            return caps.stream()
                .filter(c -> c.getCapId().equals(capabilityId))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    @Override
    public SceneGroupInfoDTO createSceneGroup(String sceneId, SceneGroupConfigDTO config) {
        String groupId = "group-" + UUID.randomUUID().toString().substring(0, 8);
        SceneGroupInfoDTO group = new SceneGroupInfoDTO();
        group.setSceneGroupId(groupId);
        group.setSceneId(sceneId);
        group.setName(config != null ? config.getName() : "New Group");
        group.setStatus("active");
        group.setMemberCount(0);
        group.setCreatedAt(System.currentTimeMillis());
        sceneGroups.put(groupId, group);
        groupMembers.put(groupId, new ArrayList<>());
        return group;
    }

    @Override
    public boolean destroySceneGroup(String sceneGroupId) {
        sceneGroups.remove(sceneGroupId);
        groupMembers.remove(sceneGroupId);
        return true;
    }

    @Override
    public List<SceneGroupInfoDTO> listSceneGroups() {
        return new ArrayList<>(sceneGroups.values());
    }

    @Override
    public boolean joinSceneGroup(String sceneGroupId, String agentId, String role) {
        SceneGroupInfoDTO group = sceneGroups.get(sceneGroupId);
        if (group == null) return false;

        List<SceneMemberInfoDTO> members = groupMembers.get(sceneGroupId);
        if (members == null) return false;

        SceneMemberInfoDTO member = new SceneMemberInfoDTO();
        member.setMemberId("member-" + UUID.randomUUID().toString().substring(0, 8));
        member.setSceneGroupId(sceneGroupId);
        member.setAgentId(agentId);
        member.setRole(role != null ? role : "secondary");
        member.setStatus("online");
        member.setJoinedAt(System.currentTimeMillis());
        members.add(member);

        group.setMemberCount(members.size());
        return true;
    }

    @Override
    public boolean leaveSceneGroup(String sceneGroupId, String agentId) {
        List<SceneMemberInfoDTO> members = groupMembers.get(sceneGroupId);
        if (members == null) return false;

        boolean removed = members.removeIf(m -> m.getAgentId().equals(agentId));

        SceneGroupInfoDTO group = sceneGroups.get(sceneGroupId);
        if (group != null) {
            group.setMemberCount(members.size());
        }
        return removed;
    }

    @Override
    public List<SceneMemberInfoDTO> listMembers(String sceneGroupId) {
        return groupMembers.getOrDefault(sceneGroupId, new ArrayList<>());
    }

    @Override
    public SceneMemberInfoDTO getPrimaryMember(String sceneGroupId) {
        List<SceneMemberInfoDTO> members = groupMembers.get(sceneGroupId);
        if (members != null) {
            return members.stream()
                .filter(m -> "primary".equals(m.getRole()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    @Override
    public boolean handleFailover(String sceneGroupId, String failedMemberId) {
        List<SceneMemberInfoDTO> members = groupMembers.get(sceneGroupId);
        if (members == null || members.isEmpty()) return false;

        SceneMemberInfoDTO failed = members.stream()
            .filter(m -> m.getMemberId().equals(failedMemberId))
            .findFirst()
            .orElse(null);

        if (failed == null) return false;

        if ("primary".equals(failed.getRole())) {
            members.stream()
                .filter(m -> !"primary".equals(m.getRole()))
                .findFirst()
                .ifPresent(newPrimary -> newPrimary.setRole("primary"));
        }

        failed.setStatus("offline");
        return true;
    }

    @Override
    public PageResult<SceneInfoDTO> listScenesPaged(int pageNum, int pageSize) {
        return paginate(listScenes(), pageNum, pageSize);
    }

    @Override
    public PageResult<SceneGroupInfoDTO> listSceneGroupsPaged(int pageNum, int pageSize) {
        return paginate(listSceneGroups(), pageNum, pageSize);
    }

    @Override
    public PageResult<SceneMemberInfoDTO> listMembersPaged(String sceneGroupId, int pageNum, int pageSize) {
        return paginate(listMembers(sceneGroupId), pageNum, pageSize);
    }

    @Override
    public PageResult<CapabilityInfoDTO> listCapabilitiesPaged(String sceneId, int pageNum, int pageSize) {
        return paginate(listCapabilities(sceneId), pageNum, pageSize);
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
