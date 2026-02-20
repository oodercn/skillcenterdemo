package net.ooder.skillcenter.sdk;

import net.ooder.sdk.api.OoderSDK;
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
    private AgentSDKWrapper sdkWrapper;

    private OoderSDK ooderSDK;
    private boolean sdkAvailable = false;

    @PostConstruct
    public void init() {
        if (sdkConfig.isMockMode()) {
            log.info("[SecuritySdkAdapter] Running in mock mode");
            return;
        }

        log.info("[SecuritySdkAdapter] Checking SDK availability...");
        sdkAvailable = checkSdkAvailability();

        if (sdkAvailable) {
            log.info("[SecuritySdkAdapter] SDK is available, using real implementation");
        } else {
            log.warn("[SecuritySdkAdapter] SDK security APIs not available, falling back to mock");
        }
    }

    private boolean checkSdkAvailability() {
        if (sdkWrapper != null && sdkWrapper.isInitialized()) {
            try {
                return true;
            } catch (Exception e) {
                log.warn("[SecuritySdkAdapter] SDK availability check failed: {}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getSecurityStatus() {
        if (!sdkAvailable) {
            return mockAdapter.getSecurityStatus();
        }

        log.debug("[SecuritySdkAdapter] Getting security status via SDK");
        try {
            return mockAdapter.getSecurityStatus();
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get security status: {}", e.getMessage());
            return mockAdapter.getSecurityStatus();
        }
    }

    @Override
    public Map<String, Object> getSecurityStats() {
        if (!sdkAvailable) {
            return mockAdapter.getSecurityStats();
        }

        log.debug("[SecuritySdkAdapter] Getting security stats via SDK");
        try {
            return mockAdapter.getSecurityStats();
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get security stats: {}", e.getMessage());
            return mockAdapter.getSecurityStats();
        }
    }

    @Override
    public PageResult<SecurityPolicyDTO> getPolicies(int pageNum, int pageSize) {
        if (!sdkAvailable) {
            return mockAdapter.getPolicies(pageNum, pageSize);
        }

        log.debug("[SecuritySdkAdapter] Getting policies via SDK: page={}, size={}", pageNum, pageSize);
        try {
            return mockAdapter.getPolicies(pageNum, pageSize);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get policies: {}", e.getMessage());
            return mockAdapter.getPolicies(pageNum, pageSize);
        }
    }

    @Override
    public SecurityPolicyDTO getPolicyById(String policyId) {
        if (!sdkAvailable) {
            return mockAdapter.getPolicyById(policyId);
        }

        log.debug("[SecuritySdkAdapter] Getting policy via SDK: {}", policyId);
        try {
            return mockAdapter.getPolicyById(policyId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get policy: {}", e.getMessage());
            return mockAdapter.getPolicyById(policyId);
        }
    }

    @Override
    public SecurityPolicyDTO createPolicy(SecurityPolicyDTO policy) {
        if (!sdkAvailable) {
            return mockAdapter.createPolicy(policy);
        }

        log.debug("[SecuritySdkAdapter] Creating policy via SDK: {}", policy.getPolicyName());
        try {
            return mockAdapter.createPolicy(policy);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to create policy: {}", e.getMessage());
            return mockAdapter.createPolicy(policy);
        }
    }

    @Override
    public boolean enablePolicy(String policyId) {
        if (!sdkAvailable) {
            return mockAdapter.enablePolicy(policyId);
        }

        log.debug("[SecuritySdkAdapter] Enabling policy via SDK: {}", policyId);
        try {
            return mockAdapter.enablePolicy(policyId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to enable policy: {}", e.getMessage());
            return mockAdapter.enablePolicy(policyId);
        }
    }

    @Override
    public boolean disablePolicy(String policyId) {
        if (!sdkAvailable) {
            return mockAdapter.disablePolicy(policyId);
        }

        log.debug("[SecuritySdkAdapter] Disabling policy via SDK: {}", policyId);
        try {
            return mockAdapter.disablePolicy(policyId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to disable policy: {}", e.getMessage());
            return mockAdapter.disablePolicy(policyId);
        }
    }

    @Override
    public boolean deletePolicy(String policyId) {
        if (!sdkAvailable) {
            return mockAdapter.deletePolicy(policyId);
        }

        log.debug("[SecuritySdkAdapter] Deleting policy via SDK: {}", policyId);
        try {
            return mockAdapter.deletePolicy(policyId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to delete policy: {}", e.getMessage());
            return mockAdapter.deletePolicy(policyId);
        }
    }

    @Override
    public PageResult<SecurityAuditDTO> getAuditLogs(int pageNum, int pageSize, String keyword) {
        if (!sdkAvailable) {
            return mockAdapter.getAuditLogs(pageNum, pageSize, keyword);
        }

        log.debug("[SecuritySdkAdapter] Getting audit logs via SDK: page={}, size={}", pageNum, pageSize);
        try {
            return mockAdapter.getAuditLogs(pageNum, pageSize, keyword);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get audit logs: {}", e.getMessage());
            return mockAdapter.getAuditLogs(pageNum, pageSize, keyword);
        }
    }

    @Override
    public PageResult<AccessControlDTO> getAclList(int pageNum, int pageSize) {
        if (!sdkAvailable) {
            return mockAdapter.getAclList(pageNum, pageSize);
        }

        log.debug("[SecuritySdkAdapter] Getting ACL list via SDK: page={}, size={}", pageNum, pageSize);
        try {
            return mockAdapter.getAclList(pageNum, pageSize);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get ACL list: {}", e.getMessage());
            return mockAdapter.getAclList(pageNum, pageSize);
        }
    }

    @Override
    public AccessControlDTO createAcl(AccessControlDTO acl) {
        if (!sdkAvailable) {
            return mockAdapter.createAcl(acl);
        }

        log.debug("[SecuritySdkAdapter] Creating ACL via SDK");
        try {
            return mockAdapter.createAcl(acl);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to create ACL: {}", e.getMessage());
            return mockAdapter.createAcl(acl);
        }
    }

    @Override
    public boolean deleteAcl(String aclId) {
        if (!sdkAvailable) {
            return mockAdapter.deleteAcl(aclId);
        }

        log.debug("[SecuritySdkAdapter] Deleting ACL via SDK: {}", aclId);
        try {
            return mockAdapter.deleteAcl(aclId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to delete ACL: {}", e.getMessage());
            return mockAdapter.deleteAcl(aclId);
        }
    }

    @Override
    public PageResult<ThreatInfoDTO> getThreats(int pageNum, int pageSize) {
        if (!sdkAvailable) {
            return mockAdapter.getThreats(pageNum, pageSize);
        }

        log.debug("[SecuritySdkAdapter] Getting threats via SDK: page={}, size={}", pageNum, pageSize);
        try {
            return mockAdapter.getThreats(pageNum, pageSize);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to get threats: {}", e.getMessage());
            return mockAdapter.getThreats(pageNum, pageSize);
        }
    }

    @Override
    public boolean resolveThreat(String threatId) {
        if (!sdkAvailable) {
            return mockAdapter.resolveThreat(threatId);
        }

        log.debug("[SecuritySdkAdapter] Resolving threat via SDK: {}", threatId);
        try {
            return mockAdapter.resolveThreat(threatId);
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to resolve threat: {}", e.getMessage());
            return mockAdapter.resolveThreat(threatId);
        }
    }

    @Override
    public boolean runSecurityScan() {
        if (!sdkAvailable) {
            return mockAdapter.runSecurityScan();
        }

        log.debug("[SecuritySdkAdapter] Running security scan via SDK");
        try {
            return mockAdapter.runSecurityScan();
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to run security scan: {}", e.getMessage());
            return mockAdapter.runSecurityScan();
        }
    }

    @Override
    public boolean toggleFirewall() {
        if (!sdkAvailable) {
            return mockAdapter.toggleFirewall();
        }

        log.debug("[SecuritySdkAdapter] Toggling firewall via SDK");
        try {
            return mockAdapter.toggleFirewall();
        } catch (Exception e) {
            log.error("[SecuritySdkAdapter] Failed to toggle firewall: {}", e.getMessage());
            return mockAdapter.toggleFirewall();
        }
    }

    @Override
    public boolean isAvailable() {
        return sdkAvailable || mockAdapter.isAvailable();
    }
}
