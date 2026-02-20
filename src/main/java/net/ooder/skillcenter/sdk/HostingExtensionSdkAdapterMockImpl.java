package net.ooder.skillcenter.sdk;

import net.ooder.nexus.skillcenter.dto.hosting.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HostingExtensionSdkAdapterMockImpl implements HostingExtensionSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(HostingExtensionSdkAdapterMockImpl.class);

    private final Map<String, AutoScalePolicyDTO> policies = new ConcurrentHashMap<>();
    private final Map<String, ServiceEndpointDTO> services = new ConcurrentHashMap<>();
    private final Map<String, VolumeDTO> volumes = new ConcurrentHashMap<>();

    public HostingExtensionSdkAdapterMockImpl() {
        initMockData();
    }

    private void initMockData() {
        AutoScalePolicyDTO policy = new AutoScalePolicyDTO();
        policy.setPolicyId("policy-001");
        policy.setInstanceId("inst-001");
        policy.setName("CPU自动扩缩容");
        policy.setEnabled(true);
        policy.setMinReplicas(1);
        policy.setMaxReplicas(5);
        policy.setCooldownPeriod(300000);

        List<AutoScalePolicyDTO.ScaleRule> scaleUpRules = new ArrayList<>();
        AutoScalePolicyDTO.ScaleRule scaleUp = new AutoScalePolicyDTO.ScaleRule();
        scaleUp.setMetricName("cpu_usage");
        scaleUp.setMetricType("resource");
        scaleUp.setThreshold(80.0);
        scaleUp.setOperator(">");
        scaleUp.setStep(1);
        scaleUp.setEvaluationPeriod(60000);
        scaleUpRules.add(scaleUp);
        policy.setScaleUpRules(scaleUpRules);

        List<AutoScalePolicyDTO.ScaleRule> scaleDownRules = new ArrayList<>();
        AutoScalePolicyDTO.ScaleRule scaleDown = new AutoScalePolicyDTO.ScaleRule();
        scaleDown.setMetricName("cpu_usage");
        scaleDown.setMetricType("resource");
        scaleDown.setThreshold(30.0);
        scaleDown.setOperator("<");
        scaleDown.setStep(1);
        scaleDown.setEvaluationPeriod(120000);
        scaleDownRules.add(scaleDown);
        policy.setScaleDownRules(scaleDownRules);

        policies.put(policy.getPolicyId(), policy);

        ServiceEndpointDTO service = new ServiceEndpointDTO();
        service.setServiceId("svc-001");
        service.setInstanceId("inst-001");
        service.setServiceName("weather-api-service");
        service.setEndpoint("http://weather-api-service.skillcenter.svc.cluster.local:8080");
        service.setProtocol("http");
        service.setHost("weather-api-service.skillcenter.svc.cluster.local");
        service.setPort(8080);
        service.setStatus("active");
        service.setAliases(Arrays.asList("weather", "weather-api"));

        ServiceEndpointDTO.LoadBalancerConfig lb = new ServiceEndpointDTO.LoadBalancerConfig();
        lb.setStrategy("round-robin");
        lb.setHealthCheckInterval(30000);
        lb.setHealthCheckTimeout(5000);
        lb.setHealthCheckPath("/health");
        service.setLoadBalancer(lb);

        services.put(service.getServiceId(), service);

        VolumeDTO volume = new VolumeDTO();
        volume.setVolumeId("vol-001");
        volume.setName("data-volume");
        volume.setType("ssd");
        volume.setSize(10L * 1024 * 1024 * 1024);
        volume.setStatus("available");
        volume.setAccessMode("ReadWriteOnce");
        volume.setStorageClass("standard");

        List<VolumeDTO.VolumeMount> mounts = new ArrayList<>();
        VolumeDTO.VolumeMount mount = new VolumeDTO.VolumeMount();
        mount.setInstanceId("inst-001");
        mount.setMountPath("/data");
        mount.setReadOnly(false);
        mounts.add(mount);
        volume.setMounts(mounts);

        volumes.put(volume.getVolumeId(), volume);

        log.info("[HostingExtensionSdkAdapter] Mock data initialized: {} policies, {} services, {} volumes",
            policies.size(), services.size(), volumes.size());
    }

    @Override
    public HostingCompatibilityDTO checkCompatibility(String skillId) {
        log.debug("[HostingExtensionSdkAdapter] Checking hosting compatibility for skill: {}", skillId);
        
        HostingCompatibilityDTO result = new HostingCompatibilityDTO();
        result.setSkillId(skillId);
        result.setSkillName("示例技能-" + skillId);
        result.setSkillType("api-skill");
        result.setCompatibilityScore(85.0);
        result.setRecommendation("recommended");

        List<HostingCompatibilityDTO.CompatibilityCheck> checks = new ArrayList<>();

        HostingCompatibilityDTO.CompatibilityCheck networkCheck = new HostingCompatibilityDTO.CompatibilityCheck();
        networkCheck.setName("网络通信支持");
        networkCheck.setStatus("pass");
        networkCheck.setMessage("技能支持HTTP API接口");
        networkCheck.setRequired(true);
        checks.add(networkCheck);

        HostingCompatibilityDTO.CompatibilityCheck stateCheck = new HostingCompatibilityDTO.CompatibilityCheck();
        stateCheck.setName("状态管理");
        stateCheck.setStatus("pass");
        stateCheck.setMessage("技能无状态或状态可持久化");
        stateCheck.setRequired(true);
        checks.add(stateCheck);

        HostingCompatibilityDTO.CompatibilityCheck healthCheck = new HostingCompatibilityDTO.CompatibilityCheck();
        healthCheck.setName("健康检查");
        healthCheck.setStatus("warning");
        healthCheck.setMessage("建议添加/health端点");
        healthCheck.setRequired(false);
        checks.add(healthCheck);

        HostingCompatibilityDTO.CompatibilityCheck resourceCheck = new HostingCompatibilityDTO.CompatibilityCheck();
        resourceCheck.setName("资源配置");
        resourceCheck.setStatus("pass");
        resourceCheck.setMessage("资源需求合理: CPU 0.5核, 内存 256MB");
        resourceCheck.setRequired(false);
        checks.add(resourceCheck);

        result.setChecks(checks);

        List<String> issues = new ArrayList<>();
        issues.add("未配置健康检查端点");
        result.setIssues(issues);

        List<String> suggestions = new ArrayList<>();
        suggestions.add("建议添加/health端点以支持健康检查");
        suggestions.add("建议配置优雅关闭逻辑");
        suggestions.add("建议使用环境变量进行配置管理");
        result.setSuggestions(suggestions);

        return result;
    }

    @Override
    public AutoScalePolicyDTO getAutoScalePolicy(String instanceId) {
        log.debug("[HostingExtensionSdkAdapter] Getting auto scale policy for instance: {}", instanceId);
        return policies.values().stream()
            .filter(p -> instanceId.equals(p.getInstanceId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public AutoScalePolicyDTO createAutoScalePolicy(AutoScalePolicyDTO policy) {
        log.debug("[HostingExtensionSdkAdapter] Creating auto scale policy: {}", policy.getName());
        String policyId = "policy-" + UUID.randomUUID().toString().substring(0, 8);
        policy.setPolicyId(policyId);
        policies.put(policyId, policy);
        return policy;
    }

    @Override
    public AutoScalePolicyDTO updateAutoScalePolicy(String policyId, AutoScalePolicyDTO policy) {
        log.debug("[HostingExtensionSdkAdapter] Updating auto scale policy: {}", policyId);
        if (!policies.containsKey(policyId)) {
            return null;
        }
        policy.setPolicyId(policyId);
        policies.put(policyId, policy);
        return policy;
    }

    @Override
    public boolean deleteAutoScalePolicy(String policyId) {
        log.debug("[HostingExtensionSdkAdapter] Deleting auto scale policy: {}", policyId);
        return policies.remove(policyId) != null;
    }

    @Override
    public boolean enableAutoScalePolicy(String policyId) {
        log.debug("[HostingExtensionSdkAdapter] Enabling auto scale policy: {}", policyId);
        AutoScalePolicyDTO policy = policies.get(policyId);
        if (policy != null) {
            policy.setEnabled(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean disableAutoScalePolicy(String policyId) {
        log.debug("[HostingExtensionSdkAdapter] Disabling auto scale policy: {}", policyId);
        AutoScalePolicyDTO policy = policies.get(policyId);
        if (policy != null) {
            policy.setEnabled(false);
            return true;
        }
        return false;
    }

    @Override
    public ServiceEndpointDTO registerService(String instanceId, ServiceEndpointDTO service) {
        log.debug("[HostingExtensionSdkAdapter] Registering service for instance: {}", instanceId);
        String serviceId = "svc-" + UUID.randomUUID().toString().substring(0, 8);
        service.setServiceId(serviceId);
        service.setInstanceId(instanceId);
        service.setStatus("active");
        services.put(serviceId, service);
        return service;
    }

    @Override
    public boolean unregisterService(String serviceId) {
        log.debug("[HostingExtensionSdkAdapter] Unregistering service: {}", serviceId);
        return services.remove(serviceId) != null;
    }

    @Override
    public ServiceEndpointDTO getService(String serviceId) {
        log.debug("[HostingExtensionSdkAdapter] Getting service: {}", serviceId);
        return services.get(serviceId);
    }

    @Override
    public List<ServiceEndpointDTO> getServicesByInstance(String instanceId) {
        log.debug("[HostingExtensionSdkAdapter] Getting services for instance: {}", instanceId);
        return services.values().stream()
            .filter(s -> instanceId.equals(s.getInstanceId()))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<ServiceEndpointDTO> discoverService(String serviceName) {
        log.debug("[HostingExtensionSdkAdapter] Discovering service: {}", serviceName);
        return services.values().stream()
            .filter(s -> serviceName.equals(s.getServiceName()) || 
                        (s.getAliases() != null && s.getAliases().contains(serviceName)))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public VolumeDTO createVolume(VolumeDTO volume) {
        log.debug("[HostingExtensionSdkAdapter] Creating volume: {}", volume.getName());
        String volumeId = "vol-" + UUID.randomUUID().toString().substring(0, 8);
        volume.setVolumeId(volumeId);
        volume.setStatus("available");
        volumes.put(volumeId, volume);
        return volume;
    }

    @Override
    public VolumeDTO getVolume(String volumeId) {
        log.debug("[HostingExtensionSdkAdapter] Getting volume: {}", volumeId);
        return volumes.get(volumeId);
    }

    @Override
    public boolean deleteVolume(String volumeId) {
        log.debug("[HostingExtensionSdkAdapter] Deleting volume: {}", volumeId);
        VolumeDTO volume = volumes.get(volumeId);
        if (volume != null && volume.getMounts() != null && !volume.getMounts().isEmpty()) {
            log.warn("[HostingExtensionSdkAdapter] Cannot delete volume {} with active mounts", volumeId);
            return false;
        }
        return volumes.remove(volumeId) != null;
    }

    @Override
    public boolean mountVolume(String volumeId, String instanceId, String mountPath, boolean readOnly) {
        log.debug("[HostingExtensionSdkAdapter] Mounting volume {} to instance {} at {}", volumeId, instanceId, mountPath);
        VolumeDTO volume = volumes.get(volumeId);
        if (volume == null) {
            return false;
        }

        VolumeDTO.VolumeMount mount = new VolumeDTO.VolumeMount();
        mount.setInstanceId(instanceId);
        mount.setMountPath(mountPath);
        mount.setReadOnly(readOnly);

        if (volume.getMounts() == null) {
            volume.setMounts(new ArrayList<>());
        }
        volume.getMounts().add(mount);
        volume.setStatus("in-use");

        return true;
    }

    @Override
    public boolean unmountVolume(String volumeId, String instanceId) {
        log.debug("[HostingExtensionSdkAdapter] Unmounting volume {} from instance {}", volumeId, instanceId);
        VolumeDTO volume = volumes.get(volumeId);
        if (volume == null || volume.getMounts() == null) {
            return false;
        }

        boolean removed = volume.getMounts().removeIf(m -> instanceId.equals(m.getInstanceId()));
        if (removed && volume.getMounts().isEmpty()) {
            volume.setStatus("available");
        }

        return removed;
    }

    @Override
    public List<VolumeDTO> listVolumes() {
        log.debug("[HostingExtensionSdkAdapter] Listing all volumes");
        return new ArrayList<>(volumes.values());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
