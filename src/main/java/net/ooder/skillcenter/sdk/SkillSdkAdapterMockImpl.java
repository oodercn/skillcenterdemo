package net.ooder.skillcenter.sdk;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.nexus.skillcenter.dto.skill.SkillManifestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SkillSdkAdapterMockImpl implements SkillSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(SkillSdkAdapterMockImpl.class);

    private final Map<String, SkillDTO> skillStore = new ConcurrentHashMap<>();
    private final Map<String, SkillDTO> receivedSkills = new ConcurrentHashMap<>();
    private final Map<String, SkillManifestDTO> manifestStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("[SkillSdkAdapter] Initializing mock adapter");
        initMockData();
    }

    private void initMockData() {
        String[] categories = {"数据处理", "API集成", "文件操作", "网络通信", "系统工具"};
        String[] statuses = {"installed", "available", "pending"};

        for (int i = 1; i <= 20; i++) {
            SkillDTO skill = new SkillDTO();
            skill.setId("skill-" + i);
            skill.setName("技能 " + i);
            skill.setDescription("这是技能 " + i + " 的描述信息");
            skill.setVersion("1.0." + (i % 10));
            skill.setCategory(categories[i % categories.length]);
            skill.setStatus(statuses[i % 3]);
            skill.setAvailable(true);
            skill.setAuthor("开发者" + (i % 5 + 1));
            skill.setDownloadCount(100 + i * 10);
            skill.setRating(3.5 + (i % 5) * 0.3);
            skill.setCreatedAt(new Date(System.currentTimeMillis() - i * 86400000L));
            skill.setUpdatedAt(new Date(System.currentTimeMillis() - i * 3600000L));
            skillStore.put(skill.getId(), skill);
        }

        for (int i = 1; i <= 5; i++) {
            SkillDTO received = new SkillDTO();
            received.setId("received-" + i);
            received.setName("接收技能 " + i);
            received.setDescription("从其他Agent接收的技能");
            received.setVersion("1.0.0");
            received.setCategory("共享技能");
            received.setStatus("pending");
            received.setAvailable(true);
            received.setAuthor("Agent-" + i);
            received.setCreatedAt(new Date());
            receivedSkills.put(received.getId(), received);
        }

        log.info("[SkillSdkAdapter] Mock data initialized: {} skills, {} received", 
            skillStore.size(), receivedSkills.size());
    }

    @Override
    public PageResult<SkillDTO> getSkills(String category, String status, String keyword, int pageNum, int pageSize) {
        log.debug("[SkillSdkAdapter] Getting skills: category={}, status={}, keyword={}", category, status, keyword);
        
        List<SkillDTO> filtered = skillStore.values().stream()
            .filter(s -> category == null || category.isEmpty() || category.equals(s.getCategory()))
            .filter(s -> status == null || status.isEmpty() || status.equals(s.getStatus()))
            .filter(s -> keyword == null || keyword.isEmpty() || 
                s.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                s.getDescription().toLowerCase().contains(keyword.toLowerCase()))
            .sorted(Comparator.comparing(SkillDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());
        
        return paginate(filtered, pageNum, pageSize);
    }

    @Override
    public SkillDTO getSkillById(String skillId) {
        log.debug("[SkillSdkAdapter] Getting skill: {}", skillId);
        return skillStore.get(skillId);
    }

    @Override
    public SkillDTO createSkill(SkillDTO skill) {
        log.debug("[SkillSdkAdapter] Creating skill: {}", skill.getName());
        String id = skill.getId() != null ? skill.getId() : "skill-" + UUID.randomUUID().toString().substring(0, 8);
        skill.setId(id);
        skill.setCreatedAt(new Date());
        skill.setUpdatedAt(new Date());
        skill.setStatus(skill.getStatus() != null ? skill.getStatus() : "pending");
        skillStore.put(id, skill);
        log.info("[SkillSdkAdapter] Skill created: {}", id);
        return skill;
    }

    @Override
    public SkillDTO updateSkill(String skillId, SkillDTO skill) {
        log.debug("[SkillSdkAdapter] Updating skill: {}", skillId);
        SkillDTO existing = skillStore.get(skillId);
        if (existing == null) {
            return null;
        }
        skill.setId(skillId);
        skill.setCreatedAt(existing.getCreatedAt());
        skill.setUpdatedAt(new Date());
        skillStore.put(skillId, skill);
        return skill;
    }

    @Override
    public boolean deleteSkill(String skillId) {
        log.debug("[SkillSdkAdapter] Deleting skill: {}", skillId);
        SkillDTO removed = skillStore.remove(skillId);
        if (removed != null) {
            log.info("[SkillSdkAdapter] Skill deleted: {}", skillId);
            return true;
        }
        return false;
    }

    @Override
    public boolean installSkill(String skillId, String source) {
        log.debug("[SkillSdkAdapter] Installing skill: {} from {}", skillId, source);
        SkillDTO skill = skillStore.get(skillId);
        if (skill != null) {
            skill.setStatus("installed");
            skill.setUpdatedAt(new Date());
            log.info("[SkillSdkAdapter] Skill installed: {}", skillId);
            return true;
        }
        return false;
    }

    @Override
    public boolean uninstallSkill(String skillId) {
        log.debug("[SkillSdkAdapter] Uninstalling skill: {}", skillId);
        SkillDTO skill = skillStore.get(skillId);
        if (skill != null && "installed".equals(skill.getStatus())) {
            skill.setStatus("available");
            skill.setUpdatedAt(new Date());
            log.info("[SkillSdkAdapter] Skill uninstalled: {}", skillId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<SkillDTO> searchSkills(String keyword, String category, int pageNum, int pageSize) {
        return getSkills(category, null, keyword, pageNum, pageSize);
    }

    @Override
    public List<SkillDTO> getInstalledSkills() {
        return skillStore.values().stream()
            .filter(s -> "installed".equals(s.getStatus()))
            .collect(Collectors.toList());
    }

    @Override
    public List<SkillDTO> getAvailableSkills() {
        return skillStore.values().stream()
            .filter(s -> "available".equals(s.getStatus()))
            .collect(Collectors.toList());
    }

    @Override
    public SkillManifestDTO getSkillManifest(String skillId) {
        log.debug("[SkillSdkAdapter] Getting manifest: {}", skillId);
        return manifestStore.get(skillId);
    }

    @Override
    public Map<String, Object> getSkillStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", skillStore.size());
        stats.put("installed", getInstalledSkills().size());
        stats.put("available", getAvailableSkills().size());
        stats.put("shared", receivedSkills.size());
        return stats;
    }

    @Override
    public boolean shareSkill(String skillId, List<String> targetAgents) {
        log.debug("[SkillSdkAdapter] Sharing skill: {} to {}", skillId, targetAgents);
        SkillDTO skill = skillStore.get(skillId);
        if (skill != null) {
            log.info("[SkillSdkAdapter] Skill shared: {} to {} agents", skillId, targetAgents.size());
            return true;
        }
        return false;
    }

    @Override
    public boolean cancelShare(String skillId) {
        log.debug("[SkillSdkAdapter] Canceling share: {}", skillId);
        return true;
    }

    @Override
    public PageResult<SkillDTO> getReceivedSkills(int pageNum, int pageSize) {
        List<SkillDTO> all = new ArrayList<>(receivedSkills.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public boolean acceptReceivedSkill(String skillId) {
        log.debug("[SkillSdkAdapter] Accepting received skill: {}", skillId);
        SkillDTO skill = receivedSkills.remove(skillId);
        if (skill != null) {
            skill.setStatus("installed");
            skillStore.put(skillId, skill);
            log.info("[SkillSdkAdapter] Received skill accepted: {}", skillId);
            return true;
        }
        return false;
    }

    @Override
    public boolean rejectReceivedSkill(String skillId) {
        log.debug("[SkillSdkAdapter] Rejecting received skill: {}", skillId);
        SkillDTO removed = receivedSkills.remove(skillId);
        return removed != null;
    }

    @Override
    public boolean isAvailable() {
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
