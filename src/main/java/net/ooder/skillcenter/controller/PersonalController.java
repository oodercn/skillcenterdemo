package net.ooder.skillcenter.controller;

import net.ooder.skillcenter.manager.SkillManager;
import net.ooder.skillcenter.model.ApiResponse;
import net.ooder.skillcenter.model.ExecutionRecord;
import net.ooder.skillcenter.model.Group;
import net.ooder.skillcenter.model.GroupMember;
import net.ooder.skillcenter.model.GroupSkill;
import net.ooder.skillcenter.model.Skill;
import net.ooder.skillcenter.model.SkillContext;
import net.ooder.skillcenter.model.SkillException;
import net.ooder.skillcenter.model.SkillResult;
import net.ooder.skillcenter.model.User;
import net.ooder.skillcenter.storage.ExecutionStorageService;
import net.ooder.skillcenter.storage.GroupStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 个人中心REST API控制器
 */
@RestController
@RequestMapping("/api/personal")
public class PersonalController {

    private final SkillManager skillManager;
    private final Map<String, SkillResult> executionResults;
    private final GroupStorageService groupStorageService;
    private final ExecutionStorageService executionStorageService;

    // 当前用户数据（模拟登录用户）
    private User currentUser = new User("user-001", "user123", "用户", "user@example.com", "13800138000", "2026-01-01 00:00:00");

    /**
     * 构造方法，初始化管理器
     */
    @Autowired
    public PersonalController(GroupStorageService groupStorageService, ExecutionStorageService executionStorageService) {
        this.skillManager = SkillManager.getInstance();
        this.executionResults = new ConcurrentHashMap<>();
        this.groupStorageService = groupStorageService;
        this.executionStorageService = executionStorageService;
    }

    // ==================== 个人仪表盘 ====================

    /**
     * 获取个人仪表盘统计数据
     * @return 个人仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPersonalDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 个人技能统计
            int totalSkills = skillManager.getAllSkills().size();
            stats.put("totalSkills", totalSkills);

            // 执行统计
            int totalExecutions = executionResults.size();
            int successfulExecutions = (int) executionResults.values().stream()
                    .filter(result -> result.getStatus() == SkillResult.Status.SUCCESS)
                    .count();
            int failedExecutions = totalExecutions - successfulExecutions;

            stats.put("totalExecutions", totalExecutions);
            stats.put("successfulExecutions", successfulExecutions);
            stats.put("failedExecutions", failedExecutions);

            // 最近活动
            List<Map<String, Object>> recentActivities = new ArrayList<>();
            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("id", "1");
            activity1.put("type", "execution");
            activity1.put("description", "执行了文本转大写技能");
            activity1.put("timestamp", "2026-01-20 10:30:00");
            recentActivities.add(activity1);

            Map<String, Object> activity2 = new HashMap<>();
            activity2.put("id", "2");
            activity2.put("type", "skill");
            activity2.put("description", "发布了新技能：JSON格式化");
            activity2.put("timestamp", "2026-01-19 15:45:00");
            recentActivities.add(activity2);

            stats.put("recentActivities", recentActivities);

            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取个人仪表盘统计数据失败: " + e.getMessage()));
        }
    }

    // ==================== 我的技能 ====================

    /**
     * 获取个人技能列表
     * @return 个人技能列表
     */
    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getMySkills() {
        try {
            List<Skill> skills = skillManager.getAllSkills();
            return ResponseEntity.ok(ApiResponse.success(skills));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取个人技能列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取技能详情
     * @param skillId 技能ID
     * @return 技能详情
     */
    @GetMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Skill>> getSkillDetail(@PathVariable String skillId) {
        try {
            Skill skill = skillManager.getSkill(skillId);
            if (skill == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "技能不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(skill));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取技能详情失败: " + e.getMessage()));
        }
    }

    /**
     * 创建个人技能
     * @param skillInfo 技能信息
     * @return 创建结果
     */
    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<Skill>> createSkill(@RequestBody SkillManager.SkillInfo skillInfo) {
        try {
            skillManager.registerSkill(skillInfo);
            return ResponseEntity.ok(ApiResponse.success(skillInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "创建技能失败: " + e.getMessage()));
        }
    }

    /**
     * 更新个人技能
     * @param skillId 技能ID
     * @param skillInfo 技能信息
     * @return 更新结果
     */
    @PutMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Skill>> updateSkill(@PathVariable String skillId, @RequestBody SkillManager.SkillInfo skillInfo) {
        try {
            skillInfo.setId(skillId);
            skillManager.updateSkill(skillInfo);
            return ResponseEntity.ok(ApiResponse.success(skillInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新技能失败: " + e.getMessage()));
        }
    }

    /**
     * 删除个人技能
     * @param skillId 技能ID
     * @return 删除结果
     */
    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteSkill(@PathVariable String skillId) {
        try {
            skillManager.unregisterSkill(skillId);
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除技能失败: " + e.getMessage()));
        }
    }

    /**
     * 执行技能
     * @param skillId 技能ID
     * @param context 执行上下文
     * @return 执行结果
     */
    @PostMapping("/skills/{skillId}/execute")
    public ResponseEntity<ApiResponse<SkillResult>> executeSkill(
            @PathVariable String skillId,
            @RequestBody SkillContext context) {
        try {
            Skill skill = skillManager.getSkill(skillId);
            if (skill == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "技能不存在"));
            }

            SkillResult result = skill.execute(context);
            String executionId = UUID.randomUUID().toString();
            executionResults.put(executionId, result);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (SkillException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "技能执行失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "技能执行失败: " + e.getMessage()));
        }
    }

    // ==================== 我的群组 ====================

    /**
     * 获取我的群组列表
     * @return 我的群组列表
     */
    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<Group>>> getMyGroups() {
        try {
            List<Group> groups = groupStorageService.getAllGroups();
            return ResponseEntity.ok(ApiResponse.success(groups));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取群组列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取群组详情
     * @param groupId 群组ID
     * @return 群组详情
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<Group>> getGroupById(@PathVariable String groupId) {
        try {
            Group group = groupStorageService.getGroupById(groupId);
            if (group == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "群组不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(group));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取群组详情失败: " + e.getMessage()));
        }
    }

    /**
     * 创建群组
     * @param group 群组信息
     * @return 创建结果
     */
    @PostMapping("/groups")
    public ResponseEntity<ApiResponse<Group>> createGroup(@RequestBody Group group) {
        try {
            Group createdGroup = groupStorageService.addGroup(group);
            return ResponseEntity.ok(ApiResponse.success(createdGroup));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "创建群组失败: " + e.getMessage()));
        }
    }

    /**
     * 更新群组
     * @param groupId 群组ID
     * @param group 群组信息
     * @return 更新结果
     */
    @PutMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<Group>> updateGroup(@PathVariable String groupId, @RequestBody Group group) {
        try {
            group.setId(groupId);
            Group updatedGroup = groupStorageService.updateGroup(group);
            if (updatedGroup == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "群组不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(updatedGroup));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新群组失败: " + e.getMessage()));
        }
    }

    /**
     * 删除群组
     * @param groupId 群组ID
     * @return 删除结果
     */
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteGroup(@PathVariable String groupId) {
        try {
            boolean deleted = groupStorageService.deleteGroup(groupId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "群组不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除群组失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有群组技能列表
     * @return 群组技能列表
     */
    @GetMapping("/groups/skills")
    public ResponseEntity<ApiResponse<List<GroupSkill>>> getAllGroupSkills() {
        try {
            List<GroupSkill> skills = groupStorageService.getAllGroupSkills();
            return ResponseEntity.ok(ApiResponse.success(skills));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取群组技能列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取群组技能列表
     * @param groupId 群组ID
     * @return 群组技能列表
     */
    @GetMapping("/groups/{groupId}/skills")
    public ResponseEntity<ApiResponse<List<GroupSkill>>> getGroupSkills(@PathVariable String groupId) {
        try {
            List<GroupSkill> skills = groupStorageService.getGroupSkillsByGroupId(groupId);
            return ResponseEntity.ok(ApiResponse.success(skills));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取群组技能列表失败: " + e.getMessage()));
        }
    }

    /**
     * 添加群组技能
     * @param groupSkill 群组技能信息
     * @return 添加结果
     */
    @PostMapping("/groups/skills")
    public ResponseEntity<ApiResponse<GroupSkill>> addGroupSkill(@RequestBody GroupSkill groupSkill) {
        try {
            GroupSkill createdSkill = groupStorageService.addGroupSkill(groupSkill);
            return ResponseEntity.ok(ApiResponse.success(createdSkill));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "添加群组技能失败: " + e.getMessage()));
        }
    }

    /**
     * 删除群组技能
     * @param skillId 技能ID
     * @return 删除结果
     */
    @DeleteMapping("/groups/skills/{skillId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteGroupSkill(@PathVariable String skillId) {
        try {
            boolean deleted = groupStorageService.deleteGroupSkill(skillId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "群组技能不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除群组技能失败: " + e.getMessage()));
        }
    }

    // ==================== 群组成员管理 ====================

    /**
     * 获取群组成员列表
     * @param groupId 群组ID
     * @return 成员列表
     */
    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMember>>> getGroupMembers(@PathVariable String groupId) {
        try {
            List<GroupMember> members = groupStorageService.getGroupMembersByGroupId(groupId);
            return ResponseEntity.ok(ApiResponse.success(members));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取群组成员列表失败: " + e.getMessage()));
        }
    }

    /**
     * 添加群组成员
     * @param groupId 群组ID
     * @param member 成员信息
     * @return 添加结果
     */
    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupMember>> addGroupMember(@PathVariable String groupId, @RequestBody GroupMember member) {
        try {
            member.setGroupId(groupId);
            GroupMember createdMember = groupStorageService.addGroupMember(member);
            return ResponseEntity.ok(ApiResponse.success(createdMember));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "添加群组成员失败: " + e.getMessage()));
        }
    }

    /**
     * 更新群组成员
     * @param groupId 群组ID
     * @param memberId 成员ID
     * @param member 成员信息
     * @return 更新结果
     */
    @PutMapping("/groups/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse<GroupMember>> updateGroupMember(@PathVariable String groupId, @PathVariable String memberId, @RequestBody GroupMember member) {
        try {
            member.setId(memberId);
            member.setGroupId(groupId);
            GroupMember updatedMember = groupStorageService.updateGroupMember(member);
            if (updatedMember == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "成员不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(updatedMember));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新群组成员失败: " + e.getMessage()));
        }
    }

    /**
     * 删除群组成员
     * @param groupId 群组ID
     * @param memberId 成员ID
     * @return 删除结果
     */
    @DeleteMapping("/groups/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteGroupMember(@PathVariable String groupId, @PathVariable String memberId) {
        try {
            boolean deleted = groupStorageService.deleteGroupMember(memberId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "成员不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除群组成员失败: " + e.getMessage()));
        }
    }

    // ==================== 执行历史 ====================

    /**
     * 获取执行历史列表
     * @return 执行历史列表
     */
    @GetMapping("/executions")
    public ResponseEntity<ApiResponse<List<ExecutionRecord>>> getExecutionHistory() {
        try {
            List<ExecutionRecord> executions = executionStorageService.getAllExecutions();
            return ResponseEntity.ok(ApiResponse.success(executions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取执行历史失败: " + e.getMessage()));
        }
    }

    /**
     * 获取执行记录详情
     * @param executionId 执行ID
     * @return 执行记录详情
     */
    @GetMapping("/executions/{executionId}")
    public ResponseEntity<ApiResponse<ExecutionRecord>> getExecutionById(@PathVariable String executionId) {
        try {
            ExecutionRecord execution = executionStorageService.getExecutionById(executionId);
            if (execution == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "执行记录不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(execution));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取执行记录失败: " + e.getMessage()));
        }
    }

    /**
     * 删除执行记录
     * @param executionId 执行ID
     * @return 删除结果
     */
    @DeleteMapping("/executions/{executionId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteExecution(@PathVariable String executionId) {
        try {
            boolean deleted = executionStorageService.deleteExecution(executionId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "执行记录不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除执行记录失败: " + e.getMessage()));
        }
    }

    /**
     * 清空执行历史
     * @return 清空结果
     */
    @DeleteMapping("/executions")
    public ResponseEntity<ApiResponse<Boolean>> clearExecutionHistory() {
        try {
            executionStorageService.clearAllExecutions();
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "清空执行历史失败: " + e.getMessage()));
        }
    }

    // ==================== 个人身份 ====================

    /**
     * 获取个人身份信息
     * @return 个人身份信息
     */
    @GetMapping("/identity")
    public ResponseEntity<ApiResponse<User>> getPersonalIdentity() {
        try {
            return ResponseEntity.ok(ApiResponse.success(currentUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取个人身份失败: " + e.getMessage()));
        }
    }

    /**
     * 更新个人身份信息
     * @param user 个人身份信息
     * @return 更新结果
     */
    @PutMapping("/identity")
    public ResponseEntity<ApiResponse<User>> updatePersonalIdentity(@RequestBody User user) {
        try {
            // 更新当前用户信息
            if (user.getName() != null) currentUser.setName(user.getName());
            if (user.getEmail() != null) currentUser.setEmail(user.getEmail());
            if (user.getPhone() != null) currentUser.setPhone(user.getPhone());
            if (user.getAvatar() != null) currentUser.setAvatar(user.getAvatar());
            if (user.getBio() != null) currentUser.setBio(user.getBio());

            return ResponseEntity.ok(ApiResponse.success(currentUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新个人身份失败: " + e.getMessage()));
        }
    }

    /**
     * 获取个人身份映射
     * @return 身份映射列表
     */
    @GetMapping("/identity/mappings")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getIdentityMappings() {
        try {
            List<Map<String, Object>> identityMappings = new ArrayList<>();
            Map<String, Object> mapping1 = new HashMap<>();
            mapping1.put("id", "1");
            mapping1.put("type", "github");
            mapping1.put("identifier", "github.com/user123");
            mapping1.put("status", "verified");
            mapping1.put("linkedAt", "2026-01-10 10:00:00");
            identityMappings.add(mapping1);

            Map<String, Object> mapping2 = new HashMap<>();
            mapping2.put("id", "2");
            mapping2.put("type", "wechat");
            mapping2.put("identifier", "wx_user123");
            mapping2.put("status", "pending");
            mapping2.put("linkedAt", "2026-01-15 14:30:00");
            identityMappings.add(mapping2);

            return ResponseEntity.ok(ApiResponse.success(identityMappings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取身份映射失败: " + e.getMessage()));
        }
    }

    // ==================== 帮助与支持 ====================

    /**
     * 获取帮助文档
     * @return 帮助文档内容
     */
    @GetMapping("/help")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHelp() {
        Map<String, Object> helpContent = new HashMap<>();

        // 快速开始指南
        List<Map<String, Object>> quickStart = new ArrayList<>();
        Map<String, Object> step1 = new HashMap<>();
        step1.put("id", "1");
        step1.put("title", "发布技能");
        step1.put("description", "学习如何发布你的第一个技能");
        step1.put("icon", "upload");
        quickStart.add(step1);

        Map<String, Object> step2 = new HashMap<>();
        step2.put("id", "2");
        step2.put("title", "管理技能");
        step2.put("description", "了解如何管理已发布的技能");
        step2.put("icon", "settings");
        quickStart.add(step2);

        Map<String, Object> step3 = new HashMap<>();
        step3.put("id", "3");
        step3.put("title", "技能市场");
        step3.put("description", "探索技能市场，发现更多技能");
        step3.put("icon", "market");
        quickStart.add(step3);

        helpContent.put("quickStart", quickStart);

        // 常见问题
        List<Map<String, Object>> faq = new ArrayList<>();
        Map<String, Object> faq1 = new HashMap<>();
        faq1.put("id", "1");
        faq1.put("question", "如何发布技能？");
        faq1.put("answer", "在'我的技能'页面点击'发布技能'按钮，填写技能信息并提交即可。");
        faq.add(faq1);

        Map<String, Object> faq2 = new HashMap<>();
        faq2.put("id", "2");
        faq2.put("question", "技能审核需要多长时间？");
        faq2.put("answer", "一般情况下，技能审核会在1-3个工作日内完成。");
        faq.add(faq2);

        Map<String, Object> faq3 = new HashMap<>();
        faq3.put("id", "3");
        faq3.put("question", "如何获得技能认证？");
        faq3.put("answer", "在'技能认证'页面申请认证，提交相关材料后等待审核。");
        faq.add(faq3);

        helpContent.put("faq", faq);

        // 联系支持
        Map<String, Object> support = new HashMap<>();
        support.put("email", "support@skillcenter.com");
        support.put("phone", "400-123-4567");
        support.put("hours", "周一至周五 9:00-18:00");

        helpContent.put("support", support);

        return ResponseEntity.ok(ApiResponse.success(helpContent));
    }

    // ==================== 设置 ====================

    /**
     * 获取个人设置
     * @return 个人设置
     */
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings() {
        Map<String, Object> settings = new HashMap<>();

        // 通知设置
        Map<String, Object> notifications = new HashMap<>();
        notifications.put("email", true);
        notifications.put("push", true);
        notifications.put("sms", false);

        settings.put("notifications", notifications);

        // 隐私设置
        Map<String, Object> privacy = new HashMap<>();
        privacy.put("publicProfile", true);
        privacy.put("showSkills", true);
        privacy.put("showActivity", false);

        settings.put("privacy", privacy);

        // 界面设置
        Map<String, Object> interface_ = new HashMap<>();
        interface_.put("theme", "light");
        interface_.put("language", "zh-CN");
        interface_.put("compactMode", false);

        settings.put("interface", interface_);

        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * 更新个人设置
     * @param settings 个人设置
     * @return 更新结果
     */
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<Boolean>> updateSettings(@RequestBody Map<String, Object> settings) {
        try {
            // 模拟更新设置逻辑
            // 实际项目中，这里应该实现真正的设置更新逻辑

            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新设置失败: " + e.getMessage()));
        }
    }

    // ==================== 功能开关 ====================

    /**
     * 获取功能开关状态
     * @return 功能开关状态
     */
    @GetMapping("/features")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeatureFlags() {
        Map<String, Object> features = new HashMap<>();

        // 功能开关
        features.put("skillPublishing", true);
        features.put("skillMarket", true);
        features.put("skillExecution", true);
        features.put("groupManagement", true);
        features.put("skillAuthentication", true);
        features.put("personalIdentity", true);

        // 功能列表
        List<String> featureList = new ArrayList<>();
        featureList.add("技能发布");
        featureList.add("技能管理");
        featureList.add("技能市场");
        featureList.add("技能执行");
        featureList.add("群组管理");
        featureList.add("技能认证");
        featureList.add("个人身份管理");

        features.put("featureList", featureList);

        return ResponseEntity.ok(ApiResponse.success(features));
    }
}
