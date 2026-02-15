package net.ooder.nexus.skillcenter.controller;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.skillcenter.dto.SkillResultDTO;
import net.ooder.skillcenter.service.SkillService;
import net.ooder.skillcenter.service.ExecutionService;
import net.ooder.nexus.skillcenter.model.ResultModel;
import net.ooder.nexus.skillcenter.dto.skill.*;
import net.ooder.nexus.skillcenter.dto.admin.SkillQueryDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
public class SkillController extends BaseController {

    private final SkillService skillService;
    private final ExecutionService executionService;

    public SkillController(SkillService skillService, ExecutionService executionService) {
        this.skillService = skillService;
        this.executionService = executionService;
    }

    @PostMapping("/skills/list")
    public ResultModel<PageResult<SkillDTO>> getAllSkills(@RequestBody SkillQueryDTO query) {
        long startTime = System.currentTimeMillis();
        logRequestStart("getAllSkills", query);

        try {
            String category = query.getCategory();
            String status = query.getStatus();
            String keyword = query.getKeyword();
            int pageNum = query.getPageNum();
            int pageSize = query.getPageSize();

            PageResult<SkillDTO> result = skillService.getAllSkills(category, status, keyword, pageNum, pageSize);
            logRequestEnd("getAllSkills", result, System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("getAllSkills", e);
            return ResultModel.error(500, "获取技能列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/skills/get")
    public ResultModel<SkillDTO> getSkillById(@RequestBody SkillIdDTO request) {
        long startTime = System.currentTimeMillis();
        logRequestStart("getSkillById", request);

        try {
            String skillId = request.getSkillId();
            if (isParamEmpty(skillId, "skillId")) {
                return ResultModel.badRequest("技能ID不能为空");
            }

            SkillDTO skill = skillService.getSkillById(skillId);
            if (skill == null) {
                return ResultModel.notFound("技能不存在");
            }
            logRequestEnd("getSkillById", skill, System.currentTimeMillis() - startTime);
            return ResultModel.success(skill);
        } catch (Exception e) {
            logRequestError("getSkillById", e);
            return ResultModel.error(500, "获取技能失败: " + e.getMessage());
        }
    }

    @PostMapping("/skills/add")
    public ResultModel<SkillDTO> addSkill(@RequestBody SkillDTO skillDTO) {
        long startTime = System.currentTimeMillis();
        logRequestStart("addSkill", skillDTO);

        try {
            SkillDTO result = skillService.addSkill(skillDTO);
            logRequestEnd("addSkill", result, System.currentTimeMillis() - startTime);
            return ResultModel.success("添加技能成功", result);
        } catch (Exception e) {
            logRequestError("addSkill", e);
            return ResultModel.error(500, "添加技能失败: " + e.getMessage());
        }
    }

    @PostMapping("/skills/update")
    public ResultModel<SkillDTO> updateSkill(@RequestBody SkillDTO skillDTO) {
        long startTime = System.currentTimeMillis();
        logRequestStart("updateSkill", skillDTO);

        try {
            String skillId = skillDTO.getId();
            if (isParamEmpty(skillId, "skillId")) {
                return ResultModel.badRequest("技能ID不能为空");
            }

            SkillDTO result = skillService.updateSkill(skillId, skillDTO);
            if (result == null) {
                return ResultModel.notFound("技能不存在");
            }
            logRequestEnd("updateSkill", result, System.currentTimeMillis() - startTime);
            return ResultModel.success("更新技能成功", result);
        } catch (Exception e) {
            logRequestError("updateSkill", e);
            return ResultModel.error(500, "更新技能失败: " + e.getMessage());
        }
    }

    @PostMapping("/skills/delete")
    public ResultModel<Boolean> deleteSkill(@RequestBody SkillIdDTO request) {
        long startTime = System.currentTimeMillis();
        logRequestStart("deleteSkill", request);

        try {
            String skillId = request.getSkillId();
            if (isParamEmpty(skillId, "skillId")) {
                return ResultModel.badRequest("技能ID不能为空");
            }

            boolean result = skillService.deleteSkill(skillId);
            logRequestEnd("deleteSkill", result, System.currentTimeMillis() - startTime);
            return ResultModel.success("删除技能成功", result);
        } catch (Exception e) {
            logRequestError("deleteSkill", e);
            return ResultModel.error(500, "删除技能失败: " + e.getMessage());
        }
    }

    @PostMapping("/skills/{skillId}/approve")
    public ResultModel<Boolean> approveSkill(@PathVariable String skillId) {
        long startTime = System.currentTimeMillis();
        logRequestStart("approveSkill", skillId);

        try {
            boolean result = skillService.approveSkill(skillId);
            logRequestEnd("approveSkill", result, System.currentTimeMillis() - startTime);
            return ResultModel.success("审核技能成功", result);
        } catch (Exception e) {
            logRequestError("approveSkill", e);
            return ResultModel.error(500, "审核技能失败: " + e.getMessage());
        }
    }

    @PostMapping("/skills/{skillId}/reject")
    public ResultModel<Boolean> rejectSkill(@PathVariable String skillId) {
        long startTime = System.currentTimeMillis();
        logRequestStart("rejectSkill", skillId);

        try {
            boolean result = skillService.rejectSkill(skillId);
            logRequestEnd("rejectSkill", result, System.currentTimeMillis() - startTime);
            return ResultModel.success("拒绝技能成功", result);
        } catch (Exception e) {
            logRequestError("rejectSkill", e);
            return ResultModel.error(500, "拒绝技能失败: " + e.getMessage());
        }
    }

    @PostMapping("/skills/{skillId}/execute")
    public ResultModel<ExecutionResultDTO> executeSkill(
            @PathVariable String skillId,
            @RequestBody SkillExecutionDTO request) {
        long startTime = System.currentTimeMillis();
        logRequestStart("executeSkill", request);

        try {
            if (isParamEmpty(skillId, "skillId")) {
                return ResultModel.badRequest("技能ID不能为空");
            }

            SkillResultDTO skillResult = executionService.executeSkill(skillId, request.getParameters());

            ExecutionResultDTO result = new ExecutionResultDTO();
            result.setExecutionId(skillResult.getExecutionId());
            result.setStatus(skillResult.getStatus());
            result.setOutput(skillResult.getOutput() != null ? String.valueOf(skillResult.getOutput()) : null);
            result.setExecutionTime(skillResult.getExecutionTime());

            logRequestEnd("executeSkill", result, System.currentTimeMillis() - startTime);
            return ResultModel.success("技能执行成功", result);
        } catch (Exception e) {
            logRequestError("executeSkill", e);
            return ResultModel.error(500, "技能执行失败: " + e.getMessage());
        }
    }
}
