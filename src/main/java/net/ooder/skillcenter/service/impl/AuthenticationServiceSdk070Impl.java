package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.security.AuthenticationResult;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.AuthenticationRequestDTO;
import net.ooder.skillcenter.sdk.AgentSDKWrapper;
import net.ooder.skillcenter.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 认证服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class AuthenticationServiceSdk070Impl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceSdk070Impl.class);

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    private final Map<String, AuthenticationRequestDTO> requestStore = new ConcurrentHashMap<>();

    @javax.annotation.PostConstruct
    public void init() {
        initDefaultRequests();
    }

    private void initDefaultRequests() {
        addRequest("req-001", "skill-001", "文本处理器", "user-002", "开发者", "pending", "publish", "申请发布技能");
        addRequest("req-002", "skill-002", "代码生成器", "user-003", "测试员", "approved", "authenticate", "申请技能认证");
        addRequest("req-003", "skill-003", "图像识别", "user-002", "开发者", "pending", "publish", "申请发布技能");
        log.info("Initialized {} default authentication requests", requestStore.size());
    }

    private void addRequest(String id, String skillId, String skillName, String requesterId, 
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
        
        log.info("Updated request {} status to {}", requestId, status);
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
