package net.ooder.skillcenter.sdk;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.nexus.skillcenter.dto.security.SecurityPolicyDTO;
import net.ooder.nexus.skillcenter.dto.security.SecurityAuditDTO;
import net.ooder.nexus.skillcenter.dto.security.AccessControlDTO;
import net.ooder.nexus.skillcenter.dto.security.ThreatInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SecuritySdkAdapterMockImpl implements SecuritySdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecuritySdkAdapterMockImpl.class);

    private final Map<String, SecurityPolicyDTO> policyStore = new ConcurrentHashMap<>();
    private final Map<String, AccessControlDTO> aclStore = new ConcurrentHashMap<>();
    private final Map<String, ThreatInfoDTO> threatStore = new ConcurrentHashMap<>();
    private boolean firewallEnabled = true;

    @PostConstruct
    public void init() {
        log.info("[SecuritySdkAdapter] Initializing mock adapter");
        initMockData();
    }

    private void initMockData() {
        String[] types = {"访问控制", "数据保护", "网络安全", "认证授权"};
        String[] actions = {"允许", "拒绝", "审计", "告警"};

        for (int i = 1; i <= 10; i++) {
            SecurityPolicyDTO policy = new SecurityPolicyDTO();
            policy.setPolicyId("policy-" + i);
            policy.setPolicyName("安全策略 " + i);
            policy.setPolicyType(types[i % types.length]);
            policy.setDescription("这是安全策略 " + i + " 的描述信息");
            policy.setStatus(i <= 8 ? "启用" : "禁用");
            policy.setPriority(i);
            policy.setAction(actions[i % actions.length]);
            policy.setCreatedAt(new Date(System.currentTimeMillis() - i * 86400000L));
            policy.setUpdatedAt(new Date(System.currentTimeMillis() - i * 3600000L));
            policyStore.put(policy.getPolicyId(), policy);
        }

        String[] resourceTypes = {"技能", "场景", "用户", "系统"};
        String[] permissions = {"读取", "写入", "执行", "管理"};

        for (int i = 1; i <= 20; i++) {
            AccessControlDTO acl = new AccessControlDTO();
            acl.setAclId("acl-" + i);
            acl.setId("acl-" + i);
            acl.setResourceType(resourceTypes[i % resourceTypes.length]);
            acl.setResourceId("resource-" + i);
            acl.setPrincipalType("用户");
            acl.setPrincipalId("user-" + (i % 10 + 1));
            acl.setPermission(permissions[i % permissions.length]);
            acl.setStatus("有效");
            acl.setGrantedAt(System.currentTimeMillis() - i * 86400000L);
            acl.setGrantedBy("admin");
            aclStore.put(acl.getAclId(), acl);
        }

        String[] threatTypes = {"恶意访问", "异常登录", "数据泄露风险", "权限滥用"};
        String[] severities = {"低", "中", "高", "严重"};
        String[] statuses = {"待处理", "处理中", "已解决", "已忽略"};

        for (int i = 1; i <= 15; i++) {
            ThreatInfoDTO threat = new ThreatInfoDTO();
            threat.setThreatId("threat-" + i);
            threat.setThreatType(threatTypes[i % threatTypes.length]);
            threat.setSeverity(severities[i % 4]);
            threat.setSource("IP: 192.168.1." + (i % 255 + 1));
            threat.setDescription("检测到" + threatTypes[i % threatTypes.length] + "行为");
            threat.setStatus(statuses[i % 4]);
            threat.setRecommendation("建议立即处理");
            threat.setDetectedAt(System.currentTimeMillis() - i * 3600000L);
            threat.setResolvedAt(i % 4 == 2 ? System.currentTimeMillis() - i * 1800000L : null);
            threatStore.put(threat.getThreatId(), threat);
        }

        log.info("[SecuritySdkAdapter] Mock data initialized: {} policies, {} acls, {} threats", 
            policyStore.size(), aclStore.size(), threatStore.size());
    }

    @Override
    public Map<String, Object> getSecurityStatus() {
        log.debug("[SecuritySdkAdapter] Getting security status");
        Map<String, Object> status = new HashMap<>();
        status.put("status", "安全");
        status.put("securityLevel", "高");
        status.put("activePolicies", policyStore.values().stream().filter(p -> "启用".equals(p.getStatus())).count());
        status.put("totalPolicies", policyStore.size());
        status.put("recentAlerts", 3);
        status.put("blockedAttempts", 15);
        status.put("threatScore", 12.5);
        status.put("firewallEnabled", firewallEnabled);
        status.put("encryptionEnabled", true);
        status.put("auditEnabled", true);
        status.put("lastScanTime", System.currentTimeMillis() - 3600000L);
        return status;
    }

    @Override
    public Map<String, Object> getSecurityStats() {
        log.debug("[SecuritySdkAdapter] Getting security stats");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", 1250);
        stats.put("criticalEvents", 5);
        stats.put("warningEvents", 45);
        stats.put("infoEvents", 1200);
        stats.put("blockedAttempts", 15);
        stats.put("allowedRequests", 9850);
        stats.put("avgResponseTime", 12.5);
        stats.put("totalBytesScanned", 1024L * 1024 * 500);
        stats.put("activeThreats", threatStore.values().stream().filter(t -> 
            !"已解决".equals(t.getStatus()) && !"已忽略".equals(t.getStatus())).count());
        stats.put("resolvedThreats", threatStore.values().stream().filter(t -> 
            "已解决".equals(t.getStatus())).count());
        return stats;
    }

    @Override
    public PageResult<SecurityPolicyDTO> getPolicies(int pageNum, int pageSize) {
        log.debug("[SecuritySdkAdapter] Getting policies: page={}, size={}", pageNum, pageSize);
        List<SecurityPolicyDTO> all = new ArrayList<>(policyStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public SecurityPolicyDTO getPolicyById(String policyId) {
        log.debug("[SecuritySdkAdapter] Getting policy: {}", policyId);
        return policyStore.get(policyId);
    }

    @Override
    public SecurityPolicyDTO createPolicy(SecurityPolicyDTO policy) {
        log.debug("[SecuritySdkAdapter] Creating policy: {}", policy.getPolicyName());
        String id = "policy-" + System.currentTimeMillis();
        policy.setPolicyId(id);
        policy.setStatus("启用");
        policy.setCreatedAt(new Date());
        policy.setUpdatedAt(new Date());
        policyStore.put(id, policy);
        log.info("[SecuritySdkAdapter] Policy created: {}", id);
        return policy;
    }

    @Override
    public boolean enablePolicy(String policyId) {
        log.debug("[SecuritySdkAdapter] Enabling policy: {}", policyId);
        SecurityPolicyDTO policy = policyStore.get(policyId);
        if (policy != null) {
            policy.setStatus("启用");
            policy.setUpdatedAt(new Date());
            log.info("[SecuritySdkAdapter] Policy enabled: {}", policyId);
            return true;
        }
        return false;
    }

    @Override
    public boolean disablePolicy(String policyId) {
        log.debug("[SecuritySdkAdapter] Disabling policy: {}", policyId);
        SecurityPolicyDTO policy = policyStore.get(policyId);
        if (policy != null) {
            policy.setStatus("禁用");
            policy.setUpdatedAt(new Date());
            log.info("[SecuritySdkAdapter] Policy disabled: {}", policyId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deletePolicy(String policyId) {
        log.debug("[SecuritySdkAdapter] Deleting policy: {}", policyId);
        boolean removed = policyStore.remove(policyId) != null;
        if (removed) {
            log.info("[SecuritySdkAdapter] Policy deleted: {}", policyId);
        }
        return removed;
    }

    @Override
    public PageResult<SecurityAuditDTO> getAuditLogs(int pageNum, int pageSize, String keyword) {
        log.debug("[SecuritySdkAdapter] Getting audit logs: page={}, size={}, keyword={}", pageNum, pageSize, keyword);
        List<SecurityAuditDTO> allLogs = new ArrayList<>();
        long now = System.currentTimeMillis();

        String[] eventTypes = {"登录", "访问", "修改", "删除", "导出"};
        String[] severities = {"信息", "警告", "严重"};
        String[] results = {"成功", "失败"};

        for (int i = 1; i <= 50; i++) {
            SecurityAuditDTO audit = new SecurityAuditDTO();
            audit.setAuditId("audit-" + i);
            audit.setEventType(eventTypes[i % eventTypes.length]);
            audit.setSeverity(severities[i % 3]);
            audit.setSource("用户-" + (i % 10 + 1));
            audit.setTarget("资源-" + (i % 5 + 1));
            audit.setDescription("执行了" + eventTypes[i % eventTypes.length] + "操作");
            audit.setResult(results[i % 2]);
            audit.setDetails("详细操作信息");
            audit.setTimestamp(now - i * 60000L);
            audit.setUserId("user-" + (i % 10 + 1));
            audit.setIpAddress("192.168.1." + (i % 255 + 1));
            allLogs.add(audit);
        }

        return paginate(allLogs, pageNum, pageSize);
    }

    @Override
    public PageResult<AccessControlDTO> getAclList(int pageNum, int pageSize) {
        log.debug("[SecuritySdkAdapter] Getting ACL list: page={}, size={}", pageNum, pageSize);
        List<AccessControlDTO> all = new ArrayList<>(aclStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public AccessControlDTO createAcl(AccessControlDTO acl) {
        log.debug("[SecuritySdkAdapter] Creating ACL: {}", acl.getResourceId());
        String id = "acl-" + System.currentTimeMillis();
        acl.setAclId(id);
        acl.setId(id);
        acl.setStatus("有效");
        acl.setGrantedAt(System.currentTimeMillis());
        acl.setGrantedBy("admin");
        aclStore.put(id, acl);
        log.info("[SecuritySdkAdapter] ACL created: {}", id);
        return acl;
    }

    @Override
    public boolean deleteAcl(String aclId) {
        log.debug("[SecuritySdkAdapter] Deleting ACL: {}", aclId);
        boolean removed = aclStore.remove(aclId) != null;
        if (removed) {
            log.info("[SecuritySdkAdapter] ACL deleted: {}", aclId);
        }
        return removed;
    }

    @Override
    public PageResult<ThreatInfoDTO> getThreats(int pageNum, int pageSize) {
        log.debug("[SecuritySdkAdapter] Getting threats: page={}, size={}", pageNum, pageSize);
        List<ThreatInfoDTO> all = new ArrayList<>(threatStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public boolean resolveThreat(String threatId) {
        log.debug("[SecuritySdkAdapter] Resolving threat: {}", threatId);
        ThreatInfoDTO threat = threatStore.get(threatId);
        if (threat != null) {
            threat.setStatus("已解决");
            threat.setResolvedAt(System.currentTimeMillis());
            log.info("[SecuritySdkAdapter] Threat resolved: {}", threatId);
            return true;
        }
        return false;
    }

    @Override
    public boolean runSecurityScan() {
        log.debug("[SecuritySdkAdapter] Running security scan");
        log.info("[SecuritySdkAdapter] Security scan completed");
        return true;
    }

    @Override
    public boolean toggleFirewall() {
        log.debug("[SecuritySdkAdapter] Toggling firewall");
        firewallEnabled = !firewallEnabled;
        log.info("[SecuritySdkAdapter] Firewall toggled: {}", firewallEnabled ? "enabled" : "disabled");
        return true;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private <T> PageResult<T> paginate(List<T> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<T> pageData = start < total ? list.subList(start, end) : new ArrayList<>();
        return new PageResult<>(pageData, total, pageNum, pageSize);
    }
}
