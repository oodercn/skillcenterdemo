package net.ooder.skillcenter.sdk;

import net.ooder.skillcenter.dto.HostingInstanceDTO;
import net.ooder.skillcenter.dto.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class HostingSdkAdapterMockImpl implements HostingSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(HostingSdkAdapterMockImpl.class);

    private final Map<String, HostingInstanceDTO> instances = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        createMockInstance("hosting-001", "文本处理服务", "text-to-uppercase-skill", "running");
        createMockInstance("hosting-002", "天气查询服务", "weather-api-skill", "stopped");
        createMockInstance("hosting-003", "股票查询服务", "stock-api-skill", "running");
    }

    private void createMockInstance(String id, String name, String skillId, String status) {
        HostingInstanceDTO instance = HostingInstanceDTO.of(id, name, skillId);
        instance.setSkillName(name);
        instance.setStatus(status);
        instance.setHost("localhost");
        instance.setPort(8080 + instances.size());
        instance.setProtocol("http");
        instance.setEndpoint("http://localhost:" + instance.getPort());
        instance.setCurrentInstances("running".equals(status) ? 1 : 0);
        instance.setMaxInstances(3);
        instance.setHealthStatus("running".equals(status) ? "healthy" : "unknown");
        instance.setOwner("admin");
        instance.setLastHeartbeat(new Date());
        instance.setCpuLimit(1.0);
        instance.setMemoryLimit(512);
        instances.put(id, instance);
    }

    @Override
    public List<HostingInstanceDTO> getAllInstances() {
        log.debug("[MockAdapter] Getting all instances");
        return new ArrayList<>(instances.values());
    }

    @Override
    public PageResult<HostingInstanceDTO> getInstances(int page, int size) {
        List<HostingInstanceDTO> all = getAllInstances();
        int total = all.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<HostingInstanceDTO> items = from < total ? all.subList(from, to) : new ArrayList<>();
        return new PageResult<>(items, total, page, size);
    }

    @Override
    public HostingInstanceDTO getInstance(String instanceId) {
        log.debug("[MockAdapter] Getting instance: {}", instanceId);
        return instances.get(instanceId);
    }

    @Override
    public HostingInstanceDTO createInstance(HostingInstanceDTO instance) {
        log.debug("[MockAdapter] Creating instance: {}", instance.getName());
        String id = "hosting-" + UUID.randomUUID().toString().substring(0, 8);
        instance.setId(id);
        instance.setCreatedAt(new Date());
        instance.setStatus("stopped");
        instance.setHealthStatus("unknown");
        instance.setCurrentInstances(0);
        instance.setMaxInstances(1);
        instances.put(id, instance);
        log.info("[MockAdapter] Instance created: {}", id);
        return instance;
    }

    @Override
    public HostingInstanceDTO updateInstance(String instanceId, HostingInstanceDTO instance) {
        log.debug("[MockAdapter] Updating instance: {}", instanceId);
        HostingInstanceDTO existing = instances.get(instanceId);
        if (existing == null) return null;
        instance.setId(instanceId);
        instance.setUpdatedAt(new Date());
        instances.put(instanceId, instance);
        log.info("[MockAdapter] Instance updated: {}", instanceId);
        return instance;
    }

    @Override
    public boolean deleteInstance(String instanceId) {
        log.debug("[MockAdapter] Deleting instance: {}", instanceId);
        boolean removed = instances.remove(instanceId) != null;
        if (removed) {
            log.info("[MockAdapter] Instance deleted: {}", instanceId);
        }
        return removed;
    }

    @Override
    public boolean startInstance(String instanceId) {
        log.debug("[MockAdapter] Starting instance: {}", instanceId);
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("running");
        instance.setCurrentInstances(1);
        instance.setHealthStatus("healthy");
        instance.setLastHeartbeat(new Date());
        log.info("[MockAdapter] Instance started: {}", instanceId);
        return true;
    }

    @Override
    public boolean stopInstance(String instanceId) {
        log.debug("[MockAdapter] Stopping instance: {}", instanceId);
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("stopped");
        instance.setCurrentInstances(0);
        instance.setHealthStatus("unknown");
        log.info("[MockAdapter] Instance stopped: {}", instanceId);
        return true;
    }

    @Override
    public boolean restartInstance(String instanceId) {
        log.debug("[MockAdapter] Restarting instance: {}", instanceId);
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("restarting");
        instance.setLastHeartbeat(new Date());
        instance.setStatus("running");
        instance.setHealthStatus("healthy");
        log.info("[MockAdapter] Instance restarted: {}", instanceId);
        return true;
    }

    @Override
    public String getInstanceStatus(String instanceId) {
        HostingInstanceDTO instance = instances.get(instanceId);
        return instance != null ? instance.getStatus() : null;
    }

    @Override
    public String getInstanceHealth(String instanceId) {
        HostingInstanceDTO instance = instances.get(instanceId);
        return instance != null ? instance.getHealthStatus() : null;
    }

    @Override
    public HostingInstanceDTO scaleInstance(String instanceId, int replicas) {
        log.debug("[MockAdapter] Scaling instance {} to {} replicas", instanceId, replicas);
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return null;
        instance.setMaxInstances(replicas);
        if ("running".equals(instance.getStatus())) {
            instance.setCurrentInstances(Math.min(replicas, instance.getCurrentInstances()));
        }
        log.info("[MockAdapter] Instance {} scaled to {} replicas", instanceId, replicas);
        return instance;
    }

    @Override
    public HostingInstanceDTO updateResources(String instanceId, double cpuLimit, long memoryLimit) {
        log.debug("[MockAdapter] Updating resources for instance {}: cpu={}, memory={}", instanceId, cpuLimit, memoryLimit);
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return null;
        instance.setCpuLimit(cpuLimit);
        instance.setMemoryLimit(memoryLimit);
        log.info("[MockAdapter] Resources updated for instance {}", instanceId);
        return instance;
    }

    @Override
    public List<HostingInstanceDTO> getInstancesBySkill(String skillId) {
        return instances.values().stream()
                .filter(i -> skillId.equals(i.getSkillId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<HostingInstanceDTO> getInstancesByOwner(String owner) {
        return instances.values().stream()
                .filter(i -> owner.equals(i.getOwner()))
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalInstances() {
        return instances.size();
    }

    @Override
    public long getRunningInstances() {
        return instances.values().stream()
                .filter(i -> "running".equals(i.getStatus()))
                .count();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
