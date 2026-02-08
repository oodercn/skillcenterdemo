/*
 * Copyright (c) 2024 Ooder Team
 *
 * This software is released under the MIT License.
 * https://opensource.org/licenses/MIT
 */
package net.ooder.skillcenter.controller;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.skillcenter.model.ApiResponse;
import net.ooder.skillcenter.service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skill REST API Controller
 * Handles skill management HTTP requests
 */
@RestController
@RequestMapping("/api")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    // ==================== 技能管理 ====================

    /**
     * 获取所有技能
     */
    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<PageResult<SkillDTO>>> getAllSkills(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<SkillDTO> result = skillService.getAllSkills(category, status, keyword, pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 根据ID获取技能
     */
    @GetMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<SkillDTO>> getSkillById(@PathVariable String skillId) {
        SkillDTO skill = skillService.getSkillById(skillId);
        if (skill == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "技能不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(skill));
    }

    /**
     * 添加技能
     */
    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillDTO>> addSkill(@RequestBody SkillDTO skillDTO) {
        SkillDTO result = skillService.addSkill(skillDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 更新技能
     */
    @PutMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<SkillDTO>> updateSkill(
            @PathVariable String skillId,
            @RequestBody SkillDTO skillDTO) {
        SkillDTO result = skillService.updateSkill(skillId, skillDTO);
        if (result == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "技能不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 删除技能
     */
    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteSkill(@PathVariable String skillId) {
        boolean result = skillService.deleteSkill(skillId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 审核技能
     */
    @PostMapping("/skills/{skillId}/approve")
    public ResponseEntity<ApiResponse<Boolean>> approveSkill(@PathVariable String skillId) {
        boolean result = skillService.approveSkill(skillId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 拒绝技能
     */
    @PostMapping("/skills/{skillId}/reject")
    public ResponseEntity<ApiResponse<Boolean>> rejectSkill(@PathVariable String skillId) {
        boolean result = skillService.rejectSkill(skillId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
