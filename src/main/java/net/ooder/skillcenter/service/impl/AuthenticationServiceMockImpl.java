package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.AuthenticationRequestDTO;
import net.ooder.skillcenter.service.AuthenticationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class AuthenticationServiceMockImpl implements AuthenticationService {

    private final Map<String, AuthenticationRequestDTO> requestStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        initMockData();
    }

    private void initMockData() {
        addMockRequest("req-001", "skill-001", "文本处理器", "user-002", "开发者", "pending", "publish", "申请发布技能");
        addMockRequest("req-002", "skill-002", "代码生成器", "user-003", "测试员", "approved", "authenticate", "申请技能认证");
        addMockRequest("req-003", "skill-003", "图像识别", "user-002", "开发者", "pending", "publish", "申请发布技能");
    }

    private void addMockRequest(String id, String skillId, String skillName, String requesterId, 
                                String requesterName, String status, String requestType, String description) {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO();
        request.setId(id);
        request.setSkillId(skillId);
        request.setSkillName(skillName);
        request.setRequesterId(requesterId);
        request.setRequesterName(requesterName);
        request.setStatus(status);
        request.setRequestType(requestType);
        request.setDescription(description);
        request.setCreatedAt(new Date());
        request.setUpdatedAt(new Date());
        requestStore.put(id, request);
    }

    @Override
    public PageResult<AuthenticationRequestDTO> getAllRequests(int pageNum, int pageSize) {
        List<AuthenticationRequestDTO> list = new ArrayList<>(requestStore.values());
        list.sort(Comparator.comparing(AuthenticationRequestDTO::getCreatedAt).reversed());
        return paginate(list, pageNum, pageSize);
    }

    @Override
    public AuthenticationRequestDTO getRequestById(String requestId) {
        return requestStore.get(requestId);
    }

    @Override
    public boolean updateRequestStatus(String requestId, String status, String comments) {
        AuthenticationRequestDTO request = requestStore.get(requestId);
        if (request == null) {
            return false;
        }
        request.setStatus(status);
        request.setComments(comments);
        request.setUpdatedAt(new Date());
        request.setProcessedAt(new Date());
        return true;
    }

    private PageResult<AuthenticationRequestDTO> paginate(List<AuthenticationRequestDTO> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            return PageResult.empty();
        }

        List<AuthenticationRequestDTO> pageList = list.subList(start, end);
        return PageResult.of(pageList, total, pageNum, pageSize);
    }
}
