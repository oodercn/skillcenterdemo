package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.skillcenter.service.SkillService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 技能服务Mock实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class SkillServiceMockImpl implements SkillService {

    private final Map<String, SkillDTO> skillStore = new ConcurrentHashMap<>();
    private final Map<String, SkillDTO> marketSkillStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        // 初始化Mock数据
        initMockData();
    }

    private void initMockData() {
        // 添加一些初始技能数据
        addMockSkill("text-processor", "文本处理器", "处理文本内容的技能", "text-processing", "active");
        addMockSkill("code-generator", "代码生成器", "自动生成代码的技能", "development", "active");
        addMockSkill("image-recognition", "图像识别", "识别图像内容的技能", "media", "pending");
        addMockSkill("data-analyzer", "数据分析器", "分析数据的技能", "storage", "active");
        addMockSkill("deploy-tool", "部署工具", "自动化部署技能", "deployment", "inactive");

        // 添加市场技能
        addMockMarketSkill("market-1", "智能客服", "AI智能客服技能", "text-processing", "active");
        addMockMarketSkill("market-2", "代码审查", "自动代码审查技能", "development", "active");
        addMockMarketSkill("market-3", "视频转码", "视频格式转换技能", "media", "pending");
    }

    private void addMockSkill(String id, String name, String description, String category, String status) {
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
        skill.setDownloadCount((int) (Math.random() * 1000));
        skill.setRating(Math.round((3.5 + Math.random() * 1.5) * 10) / 10.0);
        skillStore.put(id, skill);
    }

    private void addMockMarketSkill(String id, String name, String description, String category, String status) {
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
        skill.setDownloadCount((int) (Math.random() * 5000));
        skill.setRating(Math.round((3.5 + Math.random() * 1.5) * 10) / 10.0);
        marketSkillStore.put(id, skill);
    }

    @Override
    public PageResult<SkillDTO> getAllSkills(String category, String status, String keyword, int pageNum, int pageSize) {
        List<SkillDTO> filtered = skillStore.values().stream()
            .filter(skill -> category == null || category.isEmpty() || category.equals(skill.getCategory()))
            .filter(skill -> status == null || status.isEmpty() || status.equals(skill.getStatus()))
            .filter(skill -> keyword == null || keyword.isEmpty() ||
                skill.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                skill.getId().toLowerCase().contains(keyword.toLowerCase()))
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
        return skillDTO;
    }

    @Override
    public SkillDTO updateSkill(String skillId, SkillDTO skillDTO) {
        SkillDTO existing = skillStore.get(skillId);
        if (existing == null) {
            return null;
        }
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
                skill.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                skill.getId().toLowerCase().contains(keyword.toLowerCase()))
            .sorted(Comparator.comparing(SkillDTO::getDownloadCount).reversed())
            .collect(Collectors.toList());

        return paginate(filtered, pageNum, pageSize);
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
        if (existing == null) {
            return null;
        }
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
    public int getSkillCount() {
        return skillStore.size();
    }

    @Override
    public int getExecutionCount() {
        return 150;
    }

    @Override
    public int getSuccessfulExecutionCount() {
        return 120;
    }

    @Override
    public int getFailedExecutionCount() {
        return 30;
    }

    @Override
    public int getSharedSkillCount() {
        return (int) skillStore.values().stream()
            .filter(skill -> Boolean.TRUE.equals(skill.isAvailable()))
            .count();
    }

    @Override
    public java.util.concurrent.CompletableFuture<net.ooder.skillcenter.model.SpecValidationModels.SpecValidationResult> validateSpec(String skillId) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            net.ooder.skillcenter.model.SpecValidationModels.SpecValidationResult result = new net.ooder.skillcenter.model.SpecValidationModels.SpecValidationResult();
            SkillDTO skill = skillStore.get(skillId);
            if (skill == null) {
                result.setValid(false);
                result.setErrors(java.util.Collections.singletonList("Skill not found: " + skillId));
            } else {
                result.setValid(true);
                result.setErrors(new java.util.ArrayList<>());
                result.setWarnings(new java.util.ArrayList<>());
                java.util.Map<String, Object> details = new java.util.HashMap<>();
                details.put("skillId", skillId);
                details.put("name", skill.getName());
                details.put("version", skill.getVersion());
                result.setDetails(details);
            }
            return result;
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<net.ooder.skillcenter.model.SpecValidationModels.SpecValidationResult> validateDefinition(java.util.Map<String, Object> definition) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            net.ooder.skillcenter.model.SpecValidationModels.SpecValidationResult result = new net.ooder.skillcenter.model.SpecValidationModels.SpecValidationResult();
            java.util.List<String> errors = new java.util.ArrayList<>();
            java.util.List<String> warnings = new java.util.ArrayList<>();
            
            if (definition == null) {
                errors.add("Definition cannot be null");
            } else {
                if (!definition.containsKey("name") || definition.get("name") == null) {
                    errors.add("Skill name is required");
                }
                if (!definition.containsKey("version") || definition.get("version") == null) {
                    warnings.add("Version is recommended");
                }
            }
            
            result.setValid(errors.isEmpty());
            result.setErrors(errors);
            result.setWarnings(warnings);
            result.setDetails(definition);
            return result;
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<java.util.List<net.ooder.skillcenter.model.SpecValidationModels.VersionHistory>> getVersionHistory(String skillId, int limit) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            java.util.List<net.ooder.skillcenter.model.SpecValidationModels.VersionHistory> history = new java.util.ArrayList<>();
            SkillDTO skill = skillStore.get(skillId);
            if (skill != null) {
                net.ooder.skillcenter.model.SpecValidationModels.VersionHistory vh = new net.ooder.skillcenter.model.SpecValidationModels.VersionHistory();
                vh.setVersionId(skillId + "-v1");
                vh.setSkillId(skillId);
                vh.setVersion(skill.getVersion() != null ? skill.getVersion() : "1.0.0");
                vh.setAuthor(skill.getAuthor() != null ? skill.getAuthor() : "unknown");
                vh.setCreateTime(skill.getCreatedAt() != null ? skill.getCreatedAt().getTime() : System.currentTimeMillis());
                vh.setChangeDescription("Initial version");
                history.add(vh);
            }
            return history;
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<net.ooder.skillcenter.model.SpecValidationModels.SpecValidationReport> getValidationReport(String skillId) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            net.ooder.skillcenter.model.SpecValidationModels.SpecValidationReport report = new net.ooder.skillcenter.model.SpecValidationModels.SpecValidationReport();
            report.setSkillId(skillId);
            report.setValidationTime(System.currentTimeMillis());
            report.setPassed(true);
            report.setTotalChecks(3);
            report.setPassedChecks(3);
            report.setFailedChecks(0);
            report.setWarningCount(0);
            report.setChecks(new java.util.ArrayList<>());
            report.setSummary(new java.util.HashMap<>());
            return report;
        });
    }

    private PageResult<SkillDTO> paginate(List<SkillDTO> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            return PageResult.empty();
        }

        List<SkillDTO> pageList = list.subList(start, end);
        return PageResult.of(pageList, total, pageNum, pageSize);
    }
}
