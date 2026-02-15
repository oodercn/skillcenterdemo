package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.skill.SkillManager;
import net.ooder.skillcenter.dto.ReceivedSkillDTO;
import net.ooder.skillcenter.dto.SkillShareDTO;
import net.ooder.skillcenter.sdk.AgentSDKWrapper;
import net.ooder.skillcenter.service.ShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分享服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class ShareServiceSdk070Impl implements ShareService {

    private static final Logger log = LoggerFactory.getLogger(ShareServiceSdk070Impl.class);

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    private final Map<String, SkillShareDTO> shareStore = new ConcurrentHashMap<>();

    @Override
    public boolean shareSkill(String skillId, String groupId, String message) {
        SkillShareDTO share = new SkillShareDTO();
        share.setId("share-" + UUID.randomUUID().toString().substring(0, 8));
        share.setSkillId(skillId);
        share.setGroupId(groupId);
        share.setMessage(message != null ? message : "分享了一个技能");
        share.setSharedAt(new Date());
        share.setStatus("shared");
        shareStore.put(share.getId(), share);
        
        log.info("Shared skill {} to group {}", skillId, groupId);
        return true;
    }

    @Override
    public List<SkillShareDTO> getSharedSkills() {
        return new ArrayList<>(shareStore.values());
    }

    @Override
    public List<ReceivedSkillDTO> getReceivedSkills() {
        List<ReceivedSkillDTO> received = new ArrayList<>();
        
        if (sdkWrapper.isInitialized()) {
            SkillManager skillManager = sdkWrapper.getSkillManager();
            if (skillManager != null) {
                Map<String, net.ooder.sdk.skill.Skill> skills = skillManager.getAllSkills();
                for (Map.Entry<String, net.ooder.sdk.skill.Skill> entry : skills.entrySet()) {
                    ReceivedSkillDTO dto = new ReceivedSkillDTO();
                    dto.setId("receive-" + entry.getKey());
                    dto.setSkillId(entry.getKey());
                    dto.setSkillName(entry.getValue().getName());
                    dto.setSharerId("sdk");
                    dto.setSharerName("SDK同步");
                    dto.setReceivedAt(new Date());
                    dto.setStatus("received");
                    received.add(dto);
                }
            }
        }
        
        if (received.isEmpty()) {
            ReceivedSkillDTO skill1 = new ReceivedSkillDTO();
            skill1.setId("receive-001");
            skill1.setSkillId("text-analyzer");
            skill1.setSkillName("文本分析");
            skill1.setSharerId("user123");
            skill1.setSharerName("张三");
            skill1.setGroupId("group-001");
            skill1.setGroupName("Development Team");
            skill1.setReceivedAt(new Date());
            skill1.setMessage("分享一个文本分析工具");
            skill1.setStatus("received");
            received.add(skill1);
        }
        
        return received;
    }

    @Override
    public boolean unshareSkill(String shareId) {
        boolean removed = shareStore.remove(shareId) != null;
        if (removed) {
            log.info("Unshared skill: {}", shareId);
        }
        return removed;
    }
}
