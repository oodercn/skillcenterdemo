package net.ooder.nexus.skillcenter.controller;

import net.ooder.nexus.skillcenter.model.ResultModel;
import net.ooder.nexus.skillcenter.dto.share.*;
import net.ooder.skillcenter.manager.GroupManager;
import net.ooder.skillcenter.manager.SkillManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/share")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
public class ShareController extends BaseController {

    private final SkillManager skillManager;
    private final GroupManager groupManager;
    private final Map<String, SkillShareDTO> shareMap;

    public ShareController() {
        this.skillManager = SkillManager.getInstance();
        this.groupManager = GroupManager.getInstance();
        this.shareMap = new HashMap<>();
    }

    @PostMapping
    public ResultModel<Boolean> shareSkill(@RequestBody ShareRequestDTO request) {
        long startTime = System.currentTimeMillis();
        logRequestStart("shareSkill", request);

        try {
            if (request.getSkillId() == null || request.getGroupId() == null) {
                logRequestError("shareSkill", new IllegalArgumentException("Skill ID and Group ID are required"));
                return ResultModel.error(400, "Skill ID and Group ID are required");
            }

            if (skillManager.getSkill(request.getSkillId()) == null) {
                logRequestError("shareSkill", new IllegalArgumentException("Skill not found"));
                return ResultModel.notFound("Skill not found");
            }

            if (groupManager.getGroup(request.getGroupId()) == null) {
                logRequestError("shareSkill", new IllegalArgumentException("Group not found"));
                return ResultModel.notFound("Group not found");
            }

            SkillShareDTO share = new SkillShareDTO();
            share.setId("share-" + UUID.randomUUID().toString().substring(0, 8));
            share.setSkillId(request.getSkillId());
            share.setGroupId(request.getGroupId());
            share.setMessage(request.getMessage() != null ? request.getMessage() : "分享了一个技能");
            share.setSharedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            share.setStatus("shared");

            shareMap.put(share.getId(), share);

            logRequestEnd("shareSkill", true, System.currentTimeMillis() - startTime);
            return ResultModel.success(true);
        } catch (Exception e) {
            logRequestError("shareSkill", e);
            return ResultModel.error(500, "Failed to share skill: " + e.getMessage());
        }
    }

    @PostMapping("/shared")
    public ResultModel<List<SkillShareDTO>> getSharedSkills() {
        long startTime = System.currentTimeMillis();
        logRequestStart("getSharedSkills", null);

        try {
            List<SkillShareDTO> sharedSkills = new ArrayList<>(shareMap.values());
            logRequestEnd("getSharedSkills", sharedSkills.size() + " skills", System.currentTimeMillis() - startTime);
            return ResultModel.success(sharedSkills);
        } catch (Exception e) {
            logRequestError("getSharedSkills", e);
            return ResultModel.error(500, "Failed to get shared skills: " + e.getMessage());
        }
    }

    @PostMapping("/received")
    public ResultModel<List<ReceivedSkillDTO>> getReceivedSkills() {
        long startTime = System.currentTimeMillis();
        logRequestStart("getReceivedSkills", null);

        try {
            List<ReceivedSkillDTO> receivedSkills = new ArrayList<>();

            ReceivedSkillDTO skill1 = new ReceivedSkillDTO();
            skill1.setId("receive-001");
            skill1.setSkillId("text-analyzer");
            skill1.setSkillName("文本分析");
            skill1.setSharerId("user123");
            skill1.setSharerName("张三");
            skill1.setGroupId("group-001");
            skill1.setGroupName("Development Team");
            skill1.setReceivedAt(LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            skill1.setMessage("分享一个文本分析工具，挺好用的");
            skill1.setStatus("received");
            receivedSkills.add(skill1);

            ReceivedSkillDTO skill2 = new ReceivedSkillDTO();
            skill2.setId("receive-002");
            skill2.setSkillId("image-resizer");
            skill2.setSkillName("图片 resize");
            skill2.setSharerId("user456");
            skill2.setSharerName("李四");
            skill2.setGroupId("group-002");
            skill2.setGroupName("Design Team");
            skill2.setReceivedAt(LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            skill2.setMessage("市场团队的图片处理工具");
            skill2.setStatus("received");
            receivedSkills.add(skill2);

            logRequestEnd("getReceivedSkills", receivedSkills.size() + " skills", System.currentTimeMillis() - startTime);
            return ResultModel.success(receivedSkills);
        } catch (Exception e) {
            logRequestError("getReceivedSkills", e);
            return ResultModel.error(500, "Failed to get received skills: " + e.getMessage());
        }
    }

    @PostMapping("/{shareId}/delete")
    public ResultModel<Boolean> unshareSkill(@PathVariable String shareId) {
        long startTime = System.currentTimeMillis();
        logRequestStart("unshareSkill", shareId);

        try {
            if (shareId == null || shareId.isEmpty()) {
                logRequestError("unshareSkill", new IllegalArgumentException("Share ID is required"));
                return ResultModel.error(400, "Share ID is required");
            }

            boolean result = shareMap.remove(shareId) != null;
            if (!result) {
                logRequestError("unshareSkill", new IllegalArgumentException("Share not found"));
                return ResultModel.notFound("Share not found");
            }

            logRequestEnd("unshareSkill", true, System.currentTimeMillis() - startTime);
            return ResultModel.success(true);
        } catch (Exception e) {
            logRequestError("unshareSkill", e);
            return ResultModel.error(500, "Failed to unshare skill: " + e.getMessage());
        }
    }
}
