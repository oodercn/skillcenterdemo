package net.ooder.skillcenter.sdk;

import net.ooder.scene.provider.*;
import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.nexus.skillcenter.dto.security.SecurityPolicyDTO;
import net.ooder.nexus.skillcenter.dto.security.SecurityAuditDTO;
import net.ooder.nexus.skillcenter.dto.security.AccessControlDTO;
import net.ooder.nexus.skillcenter.dto.security.ThreatInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Primary
public class SecuritySdkAdapterImpl implements SecuritySdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecuritySdkAdapterImpl.class);

    @Autowired
    private SdkConfig sdkConfig;

    @Autowired
    private SecuritySdkAdapterMockImpl mockAdapter;

    @Autowired
    private SceneEngineAdapter sceneEngineAdapter;

    private SecurityProvider securityProvider;
    private boolean sdkAvailable = false;

    @PostConstruct
    public void init() {
        if (sdkConfig.isMockMode()) {
            log.info("[SecuritySdkAdapter] Running in mock mode");
            return;
        }

        log.info("[SecuritySdkAdapter] Checking SDK availability...");
        sdkAvailable = sceneEngineAdapter.isAvailable();

        if (sdkAvailable) {
            securityProvider = sceneEngineAdapter.getSecurityProvider();
            if (securityProvider != null) {
                log.info("[SecuritySdkAdapter] SecurityProvider available, using real implementation");
            } else {
                sdkAvailable = false;
                log.warn("[SecuritySdkAdapter] SecurityProvider not available, falling back to mock");
            }
        } else {
            log.warn("[SecuritySdkAdapter] SDK not available, falling back to mock");
        }
    }

    @Override
    public Map<String, Object> getSecurityStatus() {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.getSecurityStatus();
        }

        try {
            SecurityStatus status = securityProvider.getStatus();
            Map<String, Object> result = new HashMap<>();
            result.put("status", status.getStatus());
            result.put("securityLevel", status.getSecurityLevel());
            result.put("activePolicies", status.getActivePolicies());
            result.put("totalPolicies", status.getTotalPolicies());
            result.put("recentAlerts", status.getRecentAlerts());
            result.put("blockedAttempts", status.getBlockedAttempts());
            result.put("threatScore", status.getThreatScore());
            result.put("firewallEnabled", status.isFirewallEnabled());
            result.put("encryptionEnabled", status.isEncryptionEnabled());
            result.put("auditEnabled", status.isAuditEnabled());
            result.put("lastScanTime", status.getLastScanTime());
            return result;
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get security status: {}", e.getMessage());
            return mockAdapter.getSecurityStatus();
        }
    }

    @Override
    public Map<String, Object> getSecurityStats() {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.getSecurityStats();
        }

        try {
            SecurityStats stats = securityProvider.getStats();
            Map<String, Object> result = new HashMap<>();
            result.put("totalPolicies", stats.getTotalPolicies());
            result.put("activePolicies", stats.getActivePolicies());
            result.put("totalAcls", stats.getTotalAcls());
            result.put("totalThreats", stats.getTotalThreats());
            result.put("resolvedThreats", stats.getResolvedThreats());
            result.put("pendingThreats", stats.getPendingThreats());
            result.put("auditEvents", stats.getAuditEvents());
            result.put("blockedAttempts", stats.getBlockedAttempts());
            return result;
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get security stats: {}", e.getMessage());
            return mockAdapter.getSecurityStats();
        }
    }

    @Override
    public PageResult<SecurityPolicyDTO> getPolicies(int pageNum, int pageSize) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.getPolicies(pageNum, pageSize);
        }

        try {
            List<SecurityPolicy> policies = securityProvider.listPolicies();
            List<SecurityPolicyDTO> dtoList = new ArrayList<>();
            for (SecurityPolicy policy : policies) {
                dtoList.add(convertPolicyToDTO(policy));
            }
            return paginate(dtoList, pageNum, pageSize);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get policies: {}", e.getMessage());
            return mockAdapter.getPolicies(pageNum, pageSize);
        }
    }

    @Override
    public SecurityPolicyDTO getPolicyById(String policyId) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.getPolicyById(policyId);
        }

        try {
            SecurityPolicy policy = securityProvider.getPolicy(policyId);
            return policy != null ? convertPolicyToDTO(policy) : null;
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get policy: {}", e.getMessage());
            return mockAdapter.getPolicyById(policyId);
        }
    }

    @Override
    public SecurityPolicyDTO createPolicy(SecurityPolicyDTO policy) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.createPolicy(policy);
        }

        try {
            SecurityPolicy newPolicy = convertDTOToPolicy(policy);
            SecurityPolicy created = securityProvider.createPolicy(newPolicy);
            return convertPolicyToDTO(created);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to create policy: {}", e.getMessage());
            return mockAdapter.createPolicy(policy);
        }
    }

    @Override
    public boolean enablePolicy(String policyId) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.enablePolicy(policyId);
        }

        try {
            return securityProvider.enablePolicy(policyId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to enable policy: {}", e.getMessage());
            return mockAdapter.enablePolicy(policyId);
        }
    }

    @Override
    public boolean disablePolicy(String policyId) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.disablePolicy(policyId);
        }

        try {
            return securityProvider.disablePolicy(policyId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to disable policy: {}", e.getMessage());
            return mockAdapter.disablePolicy(policyId);
        }
    }

    @Override
    public boolean deletePolicy(String policyId) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.deletePolicy(policyId);
        }

        try {
            return securityProvider.deletePolicy(policyId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to delete policy: {}", e.getMessage());
            return mockAdapter.deletePolicy(policyId);
        }
    }

    @Override
    public PageResult<SecurityAuditDTO> getAuditLogs(int pageNum, int pageSize, String keyword) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.getAuditLogs(pageNum, pageSize, keyword);
        }

        return mockAdapter.getAuditLogs(pageNum, pageSize, keyword);
    }

    @Override
    public PageResult<AccessControlDTO> getAclList(int pageNum, int pageSize) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.getAclList(pageNum, pageSize);
        }

        try {
            net.ooder.scene.core.PageResult<AccessControl> result = securityProvider.listAcls(pageNum, pageSize);
            List<AccessControlDTO> dtoList = new ArrayList<>();
            for (AccessControl acl : result.getList()) {
                dtoList.add(convertAclToDTO(acl));
            }
            return new PageResult<>(dtoList, result.getTotal(), result.getPageNum(), result.getPageSize());
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get ACL list: {}", e.getMessage());
            return mockAdapter.getAclList(pageNum, pageSize);
        }
    }

    @Override
    public AccessControlDTO createAcl(AccessControlDTO acl) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.createAcl(acl);
        }

        try {
            AccessControl newAcl = convertDTOToAcl(acl);
            AccessControl created = securityProvider.createAcl(newAcl);
            return convertAclToDTO(created);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to create ACL: {}", e.getMessage());
            return mockAdapter.createAcl(acl);
        }
    }

    @Override
    public boolean deleteAcl(String aclId) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.deleteAcl(aclId);
        }

        try {
            return securityProvider.deleteAcl(aclId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to delete ACL: {}", e.getMessage());
            return mockAdapter.deleteAcl(aclId);
        }
    }

    @Override
    public PageResult<ThreatInfoDTO> getThreats(int pageNum, int pageSize) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.getThreats(pageNum, pageSize);
        }

        try {
            net.ooder.scene.core.PageResult<ThreatInfo> result = securityProvider.listThreats(pageNum, pageSize);
            List<ThreatInfoDTO> dtoList = new ArrayList<>();
            for (ThreatInfo threat : result.getList()) {
                dtoList.add(convertThreatToDTO(threat));
            }
            return new PageResult<>(dtoList, result.getTotal(), result.getPageNum(), result.getPageSize());
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get threats: {}", e.getMessage());
            return mockAdapter.getThreats(pageNum, pageSize);
        }
    }

    @Override
    public boolean resolveThreat(String threatId) {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.resolveThreat(threatId);
        }

        try {
            return securityProvider.resolveThreat(threatId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to resolve threat: {}", e.getMessage());
            return mockAdapter.resolveThreat(threatId);
        }
    }

    @Override
    public boolean runSecurityScan() {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.runSecurityScan();
        }

        try {
            return securityProvider.runSecurityScan();
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to run security scan: {}", e.getMessage());
            return mockAdapter.runSecurityScan();
        }
    }

    @Override
    public boolean toggleFirewall() {
        if (!sdkAvailable || securityProvider == null) {
            return mockAdapter.toggleFirewall();
        }

        try {
            return securityProvider.toggleFirewall();
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to toggle firewall: {}", e.getMessage());
            return mockAdapter.toggleFirewall();
        }
    }

    @Override
    public boolean isAvailable() {
        return sdkAvailable || mockAdapter.isAvailable();
    }

    private SecurityPolicyDTO convertPolicyToDTO(SecurityPolicy policy) {
        SecurityPolicyDTO dto = new SecurityPolicyDTO();
        dto.setPolicyId(policy.getPolicyId());
        dto.setPolicyName(policy.getPolicyName());
        dto.setPolicyType(policy.getPolicyType());
        dto.setDescription(policy.getDescription());
        dto.setStatus(policy.getStatus());
        dto.setPriority(policy.getPriority());
        dto.setAction(policy.getAction());
        dto.setCreateTime(policy.getCreatedAt() != null ? policy.getCreatedAt().getTime() : 0);
        dto.setUpdateTime(policy.getUpdatedAt() != null ? policy.getUpdatedAt().getTime() : 0);
        return dto;
    }

    private SecurityPolicy convertDTOToPolicy(SecurityPolicyDTO dto) {
        SecurityPolicy policy = new SecurityPolicy();
        policy.setPolicyId(dto.getPolicyId());
        policy.setPolicyName(dto.getPolicyName());
        policy.setPolicyType(dto.getPolicyType());
        policy.setDescription(dto.getDescription());
        policy.setPriority(dto.getPriority());
        policy.setAction(dto.getAction());
        return policy;
    }

    private AccessControlDTO convertAclToDTO(AccessControl acl) {
        AccessControlDTO dto = new AccessControlDTO();
        dto.setAclId(acl.getAclId());
        dto.setResourceType(acl.getResourceType());
        dto.setResourceId(acl.getResourceId());
        dto.setPrincipalType(acl.getPrincipalType());
        dto.setPrincipalId(acl.getPrincipalId());
        dto.setPermission(acl.getPermission());
        dto.setStatus(acl.getStatus());
        dto.setGrantedAt(acl.getGrantedAt());
        dto.setGrantedBy(acl.getGrantedBy());
        return dto;
    }

    private AccessControl convertDTOToAcl(AccessControlDTO dto) {
        AccessControl acl = new AccessControl();
        acl.setResourceType(dto.getResourceType());
        acl.setResourceId(dto.getResourceId());
        acl.setPrincipalType(dto.getPrincipalType());
        acl.setPrincipalId(dto.getPrincipalId());
        acl.setPermission(dto.getPermission());
        return acl;
    }

    private ThreatInfoDTO convertThreatToDTO(ThreatInfo threat) {
        ThreatInfoDTO dto = new ThreatInfoDTO();
        dto.setThreatId(threat.getThreatId());
        dto.setThreatType(threat.getThreatType());
        dto.setSeverity(threat.getSeverity());
        dto.setSource(threat.getSource());
        dto.setDescription(threat.getDescription());
        dto.setStatus(threat.getStatus());
        dto.setRecommendation(threat.getRecommendation());
        dto.setDetectedAt(threat.getDetectedAt() != null ? threat.getDetectedAt().getTime() : 0);
        dto.setResolvedAt(threat.getResolvedAt() != null ? threat.getResolvedAt().getTime() : 0);
        return dto;
    }

    private <T> PageResult<T> paginate(List<T> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            return new PageResult<>(new ArrayList<>(), total, pageNum, pageSize);
        }

        return new PageResult<>(list.subList(start, end), total, pageNum, pageSize);
    }
}
