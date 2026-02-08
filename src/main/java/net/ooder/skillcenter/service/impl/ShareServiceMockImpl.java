package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.ReceivedSkillDTO;
import net.ooder.skillcenter.dto.SkillShareDTO;
import net.ooder.skillcenter.service.ShareService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class ShareServiceMockImpl implements ShareService {

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
        return true;
    }

    @Override
    public List<SkillShareDTO> getSharedSkills() {
        return new ArrayList<>(shareStore.values());
    }

    @Override
    public List<ReceivedSkillDTO> getReceivedSkills() {
        List<ReceivedSkillDTO> received = new ArrayList<>();
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
        return received;
    }

    @Override
    public boolean unshareSkill(String shareId) {
        return shareStore.remove(shareId) != null;
    }
}
