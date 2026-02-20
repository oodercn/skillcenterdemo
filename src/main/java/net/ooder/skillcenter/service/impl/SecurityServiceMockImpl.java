package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.service.SecurityService;
import net.ooder.nexus.skillcenter.dto.security.SecurityPolicyDTO;
import net.ooder.nexus.skillcenter.dto.security.SecurityAuditDTO;
import net.ooder.nexus.skillcenter.dto.security.AccessControlDTO;
import net.ooder.nexus.skillcenter.dto.security.ThreatInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class SecurityServiceMockImpl implements SecurityService {

    private static final Logger log = LoggerFactory.getLogger(SecurityServiceMockImpl.class);

    private final Map<String, SecurityPolicyDTO> policyStore = new ConcurrentHashMap<>();
    private final Map<String, SecurityAuditDTO> auditStore = new ConcurrentHashMap<>();
    private final Map<String, AccessControlDTO> aclStore = new ConcurrentHashMap<>();
    private final Map<String, ThreatInfoDTO> threatStore = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);
    private boolean firewallEnabled = true;

    @PostConstruct
    public void init() {
        log.info("[SecurityServiceMockImpl] Initialized with Mock mode");
        initMockData();
    }

    private void initMockData() {
        String[] types = {"访问控制", "入侵检测", "数据保护", "审计策略"};
        String[] actions = {"允许", "拒绝", "告警", "记录"};

        for (int i = 1; i <= 5; i++) {
            SecurityPolicyDTO policy = new SecurityPolicyDTO();
            policy.setId("policy-" + i);
            policy.setPolicyId("policy-" + i);
            policy.setPolicyName("安全策略-" + i);
            policy.setPolicyType(types[i % 4]);
            policy.setPriority(i);
            policy.setAction(actions[i % 4]);
            policy.setStatus(i % 2 == 0 ? "启用" : "禁用");
            policy.setDescription("策略描述 " + i);
            policy.setCreatedAt(new Date(System.currentTimeMillis() - i * 86400000L));
            policy.setUpdatedAt(new Date(System.currentTimeMillis() - i * 3600000L));
            policyStore.put(policy.getPolicyId(), policy);
        }

        String[] eventTypes = {"登录", "访问", "修改", "删除", "导出"};
        String[] severities = {"信息", "警告", "严重"};
        String[] results = {"成功", "失败"};

        for (int i = 1; i <= 10; i++) {
            SecurityAuditDTO audit = new SecurityAuditDTO();
            audit.setId("audit-" + i);
            audit.setAuditId("audit-" + i);
            audit.setEventType(eventTypes[i % 5]);
            audit.setSeverity(severities[i % 3]);
            audit.setSource("192.168.1." + (i % 255 + 1));
            audit.setTarget("资源-" + i);
            audit.setResult(results[i % 2]);
            audit.setDescription("审计事件描述 " + i);
            audit.setTimestamp(System.currentTimeMillis() - i * 60000L);
            audit.setUserId("user-" + (i % 3 + 1));
            auditStore.put(audit.getAuditId(), audit);
        }

        String[] resourceTypes = {"技能", "场景", "数据", "配置"};
        String[] permissions = {"读取", "写入", "执行", "管理", "完全控制"};

        for (int i = 1; i <= 5; i++) {
            AccessControlDTO acl = new AccessControlDTO();
            acl.setId("acl-" + i);
            acl.setAclId("acl-" + i);
            acl.setResourceType(resourceTypes[i % 4]);
            acl.setResourceId("resource-" + i);
            acl.setPrincipalType(i % 2 == 0 ? "用户" : "角色");
            acl.setPrincipalId("principal-" + i);
            acl.setPermission(permissions[i % 5]);
            acl.setStatus("有效");
            acl.setGrantedAt(System.currentTimeMillis() - i * 3600000L);
            acl.setGrantedBy("admin");
            aclStore.put(acl.getAclId(), acl);
        }

        String[] threatTypes = {"恶意访问", "异常流量", "权限滥用", "数据泄露"};
        String[] threatSeverities = {"低", "中", "高", "严重"};
        String[] threatStatuses = {"待处理", "处理中", "已解决", "已忽略"};

        for (int i = 1; i <= 3; i++) {
            ThreatInfoDTO threat = new ThreatInfoDTO();
            threat.setThreatId("threat-" + i);
            threat.setThreatType(threatTypes[i % 4]);
            threat.setSeverity(threatSeverities[i % 4]);
            threat.setSource("192.168.1." + (i * 10));
            threat.setDescription("威胁描述 " + i);
            threat.setStatus(threatStatuses[i % 2]);
            threat.setRecommendation("建议处理措施 " + i);
            threat.setDetectedAt(System.currentTimeMillis() - i * 7200000L);
            threatStore.put(threat.getThreatId(), threat);
        }
    }

    @Override
    public Map<String, Object> getSecurityStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("securityLevel", firewallEnabled ? "高" : "中");
        status.put("activePolicies", policyStore.values().stream()
            .filter(p -> "启用".equals(p.getStatus())).count());
        status.put("totalPolicies", policyStore.size());
        status.put("recentAlerts", 5);
        status.put("blockedAttempts", 12);
        status.put("activeThreats", threatStore.values().stream()
            .filter(t -> !"已解决".equals(t.getStatus())).count());
        status.put("firewallEnabled", firewallEnabled);
        status.put("encryptionEnabled", true);
        status.put("auditEnabled", true);
        return status;
    }

    @Override
    public Map<String, Object> getSecurityStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPolicies", policyStore.size());
        stats.put("activePolicies", policyStore.values().stream()
            .filter(p -> "启用".equals(p.getStatus())).count());
        stats.put("totalAclEntries", aclStore.size());
        stats.put("totalThreats", threatStore.size());
        stats.put("resolvedThreats", threatStore.values().stream()
            .filter(t -> "已解决".equals(t.getStatus())).count());
        stats.put("auditLogsToday", auditStore.size());
        return stats;
    }

    @Override
    public PageResult<SecurityPolicyDTO> getPolicies(int pageNum, int pageSize) {
        List<SecurityPolicyDTO> all = new ArrayList<>(policyStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public SecurityPolicyDTO getPolicyById(String policyId) {
        return policyStore.get(policyId);
    }

    @Override
    public SecurityPolicyDTO createPolicy(SecurityPolicyDTO policy) {
        String id = "policy-" + idGenerator.getAndIncrement();
        policy.setId(id);
        policy.setPolicyId(id);
        policy.setStatus("启用");
        policy.setCreatedAt(new Date());
        policy.setUpdatedAt(new Date());
        policyStore.put(id, policy);
        log.info("[SecurityServiceMockImpl] Policy created: {}", id);
        return policy;
    }

    @Override
    public boolean enablePolicy(String policyId) {
        SecurityPolicyDTO policy = policyStore.get(policyId);
        if (policy != null) {
            policy.setStatus("启用");
            policy.setUpdatedAt(new Date());
            log.info("[SecurityServiceMockImpl] Policy enabled: {}", policyId);
            return true;
        }
        return false;
    }

    @Override
    public boolean disablePolicy(String policyId) {
        SecurityPolicyDTO policy = policyStore.get(policyId);
        if (policy != null) {
            policy.setStatus("禁用");
            policy.setUpdatedAt(new Date());
            log.info("[SecurityServiceMockImpl] Policy disabled: {}", policyId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<SecurityAuditDTO> getAuditLogs(int pageNum, int pageSize, String keyword) {
        List<SecurityAuditDTO> all = new ArrayList<>(auditStore.values());
        if (keyword != null && !keyword.isEmpty()) {
            all.removeIf(a -> 
                !a.getEventType().contains(keyword) && 
                !a.getSource().contains(keyword) &&
                !a.getTarget().contains(keyword)
            );
        }
        all.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public PageResult<AccessControlDTO> getAclList(int pageNum, int pageSize) {
        List<AccessControlDTO> all = new ArrayList<>(aclStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public AccessControlDTO createAcl(AccessControlDTO acl) {
        String id = "acl-" + idGenerator.getAndIncrement();
        acl.setId(id);
        acl.setAclId(id);
        acl.setStatus("有效");
        acl.setGrantedAt(System.currentTimeMillis());
        acl.setGrantedBy("admin");
        aclStore.put(id, acl);
        log.info("[SecurityServiceMockImpl] ACL created: {}", id);
        return acl;
    }

    @Override
    public boolean deleteAcl(String aclId) {
        AccessControlDTO removed = aclStore.remove(aclId);
        if (removed != null) {
            log.info("[SecurityServiceMockImpl] ACL deleted: {}", aclId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<ThreatInfoDTO> getThreats(int pageNum, int pageSize) {
        List<ThreatInfoDTO> all = new ArrayList<>(threatStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public boolean resolveThreat(String threatId) {
        ThreatInfoDTO threat = threatStore.get(threatId);
        if (threat != null) {
            threat.setStatus("已解决");
            threat.setResolvedAt(System.currentTimeMillis());
            log.info("[SecurityServiceMockImpl] Threat resolved: {}", threatId);
            return true;
        }
        return false;
    }

    @Override
    public boolean runSecurityScan() {
        log.info("[SecurityServiceMockImpl] Security scan initiated");
        return true;
    }

    @Override
    public boolean toggleFirewall() {
        firewallEnabled = !firewallEnabled;
        log.info("[SecurityServiceMockImpl] Firewall toggled: {}", firewallEnabled ? "ON" : "OFF");
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
