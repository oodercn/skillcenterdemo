package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.skill.SkillManager;
import net.ooder.skillcenter.dto.*;
import net.ooder.skillcenter.sdk.AgentSDKWrapper;
import net.ooder.skillcenter.service.AdminService;
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
 * 管理中心服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class AdminServiceSdk070Impl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceSdk070Impl.class);

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    private final Map<String, SkillDTO> skillStore = new ConcurrentHashMap<>();
    private final Map<String, SkillDTO> marketSkillStore = new ConcurrentHashMap<>();
    private final Map<String, SkillAuthenticationDTO> authStore = new ConcurrentHashMap<>();
    private final Map<String, GroupDTO> groupStore = new ConcurrentHashMap<>();
    private final Map<String, UserDTO> userStore = new ConcurrentHashMap<>();
    private final Map<String, HostingInstanceDTO> hostingStore = new ConcurrentHashMap<>();
    private final Map<String, StorageItemDTO> storageStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @javax.annotation.PostConstruct
    public void init() {
        initDefaultData();
    }

    private void initDefaultData() {
        addSkill("skill-1", "文本处理器", "处理文本内容的技能", "text-processing", "active");
        addSkill("skill-2", "代码生成器", "自动生成代码的技能", "development", "active");
        addSkill("skill-3", "图像识别", "识别图像内容的技能", "media", "pending");
        addMarketSkill("market-1", "智能客服", "AI智能客服技能", "text-processing", "active");
        addMarketSkill("market-2", "代码审查", "自动代码审查技能", "development", "active");
        addGroup("group-1", "开发团队", "核心开发团队");
        addGroup("group-2", "测试团队", "质量保证团队");
        addUser("user-1", "admin", "管理员", "admin@example.com", "admin");
        addUser("user-2", "developer", "开发者", "dev@example.com", "user");
        addHosting("hosting-1", "文本处理服务", "skill-1", "running");
        addHosting("hosting-2", "代码生成服务", "skill-2", "stopped");
        addStorage("storage-1", "JSON存储", "json", "/data/json");
        addStorage("storage-2", "VFS存储", "vfs", "/data/vfs");
        addAuth("auth-1", "skill-1", "文本处理器", "pending");
        log.info("AdminService initialized with SDK 0.7.0");
    }

    private void addSkill(String id, String name, String description, String category, String status) {
        SkillDTO skill = new SkillDTO();
        skill.setId(id);
        skill.setName(name);
        skill.setDescription(description);
        skill.setCategory(category);
        skill.setStatus(status);
        skill.setVersion("1.0.0");
        skill.setAuthor("System");
        skill.setAvailable("active".equals(status));
        skill.setCreatedAt(new Date());
        skill.setUpdatedAt(new Date());
        skillStore.put(id, skill);
    }

    private void addMarketSkill(String id, String name, String description, String category, String status) {
        SkillDTO skill = new SkillDTO();
        skill.setId(id);
        skill.setName(name);
        skill.setDescription(description);
        skill.setCategory(category);
        skill.setStatus(status);
        skill.setVersion("1.0.0");
        skill.setAuthor("Market");
        skill.setAvailable("active".equals(status));
        skill.setCreatedAt(new Date());
        skill.setUpdatedAt(new Date());
        marketSkillStore.put(id, skill);
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

    private void addUser(String id, String username, String name, String email, String role) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setUsername(username);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus("active");
        user.setCreatedAt(new Date());
        userStore.put(id, user);
    }

    private void addHosting(String id, String name, String skillId, String status) {
        HostingInstanceDTO hosting = new HostingInstanceDTO();
        hosting.setId(id);
        hosting.setName(name);
        hosting.setSkillId(skillId);
        hosting.setStatus(status);
        hosting.setCreatedAt(new Date());
        hostingStore.put(id, hosting);
    }

    private void addStorage(String id, String name, String type, String path) {
        StorageItemDTO storage = new StorageItemDTO();
        storage.setId(id);
        storage.setName(name);
        storage.setType(type);
        storage.setPath(path);
        storage.setCreatedAt(new Date());
        storageStore.put(id, storage);
    }

    private void addAuth(String id, String skillId, String skillName, String status) {
        SkillAuthenticationDTO auth = new SkillAuthenticationDTO();
        auth.setId(id);
        auth.setSkillId(skillId);
        auth.setSkillName(skillName);
        auth.setStatus(status);
        auth.setSubmittedAt(new Date());
        authStore.put(id, auth);
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSkills", skillStore.size());
        stats.put("totalMarketSkills", marketSkillStore.size());
        stats.put("totalUsers", userStore.size());
        stats.put("totalGroups", groupStore.size());
        stats.put("totalHosting", hostingStore.size());
        stats.put("activeHosting", hostingStore.values().stream().filter(h -> "running".equals(h.getStatus())).count());
        stats.put("sdkInitialized", sdkWrapper.isInitialized());
        stats.put("sdkVersion", "0.7.0");
        return stats;
    }

    @Override
    public PageResult<SkillDTO> getAllSkills(String category, String status, String keyword, int pageNum, int pageSize) {
        List<SkillDTO> filtered = skillStore.values().stream()
            .filter(skill -> category == null || category.isEmpty() || category.equals(skill.getCategory()))
            .filter(skill -> status == null || status.isEmpty() || status.equals(skill.getStatus()))
            .filter(skill -> keyword == null || keyword.isEmpty() ||
                skill.getName().toLowerCase().contains(keyword.toLowerCase()))
            .sorted(Comparator.comparing(SkillDTO::getCreatedAt).reversed())
            .collect(Collectors.toList());
        return paginate(filtered, pageNum, pageSize);
    }

    @Override
    public SkillDTO getSkillById(String skillId) {
        return skillStore.get(skillId);
    }

    @Override
    public SkillDTO addSkill(SkillDTO skillDTO) {
        String id = "skill-" + idGenerator.getAndIncrement();
        skillDTO.setId(id);
        skillDTO.setCreatedAt(new Date());
        skillDTO.setUpdatedAt(new Date());
        skillStore.put(id, skillDTO);
        log.info("Added skill: {}", id);
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
    public boolean approveSkill(String skillId) {
        SkillDTO skill = skillStore.get(skillId);
        if (skill != null) {
            skill.setStatus("active");
            skill.setAvailable(true);
            skill.setUpdatedAt(new Date());
            log.info("Approved skill: {}", skillId);
            return true;
        }
        return false;
    }

    @Override
    public boolean rejectSkill(String skillId) {
        SkillDTO skill = skillStore.get(skillId);
        if (skill != null) {
            skill.setStatus("rejected");
            skill.setAvailable(false);
            skill.setUpdatedAt(new Date());
            log.info("Rejected skill: {}", skillId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<SkillDTO> getMarketSkills(String category, String status, String keyword, int pageNum, int pageSize) {
        List<SkillDTO> filtered = marketSkillStore.values().stream()
            .filter(skill -> category == null || category.isEmpty() || category.equals(skill.getCategory()))
            .filter(skill -> status == null || status.isEmpty() || status.equals(skill.getStatus()))
            .filter(skill -> keyword == null || keyword.isEmpty() ||
                skill.getName().toLowerCase().contains(keyword.toLowerCase()))
            .sorted(Comparator.comparing(SkillDTO::getCreatedAt).reversed())
            .collect(Collectors.toList());
        return paginate(filtered, pageNum, pageSize);
    }

    @Override
    public SkillDTO getMarketSkill(String skillId) {
        return marketSkillStore.get(skillId);
    }

    @Override
    public SkillDTO addMarketSkill(SkillDTO skillDTO) {
        String id = "market-" + idGenerator.getAndIncrement();
        skillDTO.setId(id);
        skillDTO.setCreatedAt(new Date());
        skillDTO.setUpdatedAt(new Date());
        marketSkillStore.put(id, skillDTO);
        return skillDTO;
    }

    @Override
    public SkillDTO updateMarketSkill(String skillId, SkillDTO skillDTO) {
        SkillDTO existing = marketSkillStore.get(skillId);
        if (existing == null) return null;
        skillDTO.setId(skillId);
        skillDTO.setCreatedAt(existing.getCreatedAt());
        skillDTO.setUpdatedAt(new Date());
        marketSkillStore.put(skillId, skillDTO);
        return skillDTO;
    }

    @Override
    public boolean removeMarketSkill(String skillId) {
        return marketSkillStore.remove(skillId) != null;
    }

    @Override
    public List<SkillDTO> getMarketSkillsByCategory(String category) {
        return marketSkillStore.values().stream()
            .filter(skill -> category.equals(skill.getCategory()))
            .collect(Collectors.toList());
    }

    @Override
    public List<SkillDTO> getPopularMarketSkills(int limit) {
        return marketSkillStore.values().stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public List<SkillDTO> getLatestMarketSkills(int limit) {
        return marketSkillStore.values().stream()
            .sorted(Comparator.comparing(SkillDTO::getCreatedAt).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public List<SkillAuthenticationDTO> getAuthenticationRequests() {
        return new ArrayList<>(authStore.values());
    }

    @Override
    public SkillAuthenticationDTO getAuthentication(String id) {
        return authStore.get(id);
    }

    @Override
    public SkillAuthenticationDTO createAuthentication(SkillAuthenticationDTO authentication) {
        String id = "auth-" + idGenerator.getAndIncrement();
        authentication.setId(id);
        authentication.setSubmittedAt(new Date());
        authStore.put(id, authentication);
        return authentication;
    }

    @Override
    public SkillAuthenticationDTO updateAuthenticationStatus(String id, String status, String reviewer, String comments) {
        SkillAuthenticationDTO auth = authStore.get(id);
        if (auth != null) {
            auth.setStatus(status);
            auth.setReviewer(reviewer);
            auth.setComments(comments);
            auth.setReviewedAt(new Date());
        }
        return auth;
    }

    @Override
    public boolean deleteAuthentication(String id) {
        return authStore.remove(id) != null;
    }

    @Override
    public SkillAuthenticationDTO issueCertificate(SkillAuthenticationDTO request) {
        request.setStatus("approved");
        request.setCertificateId("CERT-" + idGenerator.getAndIncrement());
        request.setReviewedAt(new Date());
        return request;
    }

    @Override
    public List<GroupDTO> getAllGroups() {
        return new ArrayList<>(groupStore.values());
    }

    @Override
    public GroupDTO getGroup(String groupId) {
        return groupStore.get(groupId);
    }

    @Override
    public GroupDTO createGroup(GroupDTO group) {
        String id = "group-" + idGenerator.getAndIncrement();
        group.setId(id);
        group.setCreatedAt(new Date());
        group.setUpdatedAt(new Date());
        groupStore.put(id, group);
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
    public List<GroupDTO> searchGroups(String keyword) {
        return groupStore.values().stream()
            .filter(group -> group.getName().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return new ArrayList<>(userStore.values());
    }

    @Override
    public UserDTO getUser(String userId) {
        return userStore.get(userId);
    }

    @Override
    public UserDTO createUser(UserDTO user) {
        String id = "user-" + idGenerator.getAndIncrement();
        user.setId(id);
        user.setCreatedAt(new Date());
        userStore.put(id, user);
        return user;
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO user) {
        UserDTO existing = userStore.get(userId);
        if (existing == null) return null;
        user.setId(userId);
        user.setCreatedAt(existing.getCreatedAt());
        userStore.put(userId, user);
        return user;
    }

    @Override
    public boolean deleteUser(String userId) {
        return userStore.remove(userId) != null;
    }

    @Override
    public List<UserDTO> searchUsers(String keyword) {
        return userStore.values().stream()
            .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                user.getUsername().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getGroupMembers(String groupId) {
        return userStore.values().stream().limit(3).collect(Collectors.toList());
    }

    @Override
    public List<HostingInstanceDTO> getHostingInstances() {
        return new ArrayList<>(hostingStore.values());
    }

    @Override
    public HostingInstanceDTO getHostingInstance(String instanceId) {
        return hostingStore.get(instanceId);
    }

    @Override
    public HostingInstanceDTO createHostingInstance(HostingInstanceDTO instance) {
        String id = "hosting-" + idGenerator.getAndIncrement();
        instance.setId(id);
        instance.setCreatedAt(new Date());
        hostingStore.put(id, instance);
        return instance;
    }

    @Override
    public HostingInstanceDTO updateHostingInstance(String instanceId, HostingInstanceDTO instance) {
        HostingInstanceDTO existing = hostingStore.get(instanceId);
        if (existing == null) return null;
        instance.setId(instanceId);
        hostingStore.put(instanceId, instance);
        return instance;
    }

    @Override
    public boolean deleteHostingInstance(String instanceId) {
        return hostingStore.remove(instanceId) != null;
    }

    @Override
    public HostingInstanceDTO startHostingInstance(String instanceId) {
        HostingInstanceDTO instance = hostingStore.get(instanceId);
        if (instance != null) {
            instance.setStatus("running");
        }
        return instance;
    }

    @Override
    public HostingInstanceDTO stopHostingInstance(String instanceId) {
        HostingInstanceDTO instance = hostingStore.get(instanceId);
        if (instance != null) {
            instance.setStatus("stopped");
        }
        return instance;
    }

    @Override
    public List<HostingInstanceDTO> searchHostingInstances(String keyword) {
        return hostingStore.values().stream()
            .filter(h -> h.getName().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getHostingStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", hostingStore.size());
        stats.put("running", hostingStore.values().stream().filter(h -> "running".equals(h.getStatus())).count());
        stats.put("stopped", hostingStore.values().stream().filter(h -> "stopped".equals(h.getStatus())).count());
        return stats;
    }

    @Override
    public List<StorageItemDTO> getStorageList() {
        return new ArrayList<>(storageStore.values());
    }

    @Override
    public List<StorageItemDTO> getStorageListByType(String type) {
        return storageStore.values().stream()
            .filter(s -> type.equals(s.getType()))
            .collect(Collectors.toList());
    }

    @Override
    public StorageItemDTO getStorageItem(String storageId) {
        return storageStore.get(storageId);
    }

    @Override
    public StorageItemDTO createStorageItem(StorageItemDTO storageItem) {
        String id = "storage-" + idGenerator.getAndIncrement();
        storageItem.setId(id);
        storageItem.setCreatedAt(new Date());
        storageStore.put(id, storageItem);
        return storageItem;
    }

    @Override
    public boolean deleteStorageItem(String storageId) {
        return storageStore.remove(storageId) != null;
    }

    @Override
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", storageStore.size());
        return stats;
    }

    @Override
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", "2.1");
        info.put("sdkVersion", "0.7.0");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("sdkInitialized", sdkWrapper.isInitialized());
        return info;
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        stats.put("totalMemory", runtime.totalMemory());
        stats.put("freeMemory", runtime.freeMemory());
        stats.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        return stats;
    }

    @Override
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maintenanceMode", false);
        config.put("sdkMode", true);
        return config;
    }

    @Override
    public boolean saveSystemConfig(Map<String, Object> config) {
        return true;
    }

    @Override
    public List<SystemLogDTO> getSystemLogs(int limit) {
        return new ArrayList<>();
    }

    @Override
    public boolean restartSystem() {
        log.warn("System restart requested");
        return true;
    }

    @Override
    public boolean shutdownSystem() {
        log.warn("System shutdown requested");
        return true;
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
