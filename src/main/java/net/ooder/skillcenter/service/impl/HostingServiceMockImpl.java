package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.HostingInstanceDTO;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.service.HostingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 托管服务Mock实现 - 符合v0.7.0协议规范
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class HostingServiceMockImpl implements HostingService {

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
        instance.setHealthStatus("running".equals(status) ? "healthy" : "unknown");
        instance.setOwner("admin");
        instance.setLastHeartbeat(new Date());
        instances.put(id, instance);
    }

    @Override
    public List<HostingInstanceDTO> getAllInstances() {
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
        return instances.get(instanceId);
    }

    @Override
    public HostingInstanceDTO createInstance(HostingInstanceDTO instance) {
        String id = "hosting-" + UUID.randomUUID().toString().substring(0, 8);
        instance.setId(id);
        instance.setCreatedAt(new Date());
        instance.setStatus("stopped");
        instance.setHealthStatus("unknown");
        instances.put(id, instance);
        return instance;
    }

    @Override
    public HostingInstanceDTO updateInstance(String instanceId, HostingInstanceDTO instance) {
        HostingInstanceDTO existing = instances.get(instanceId);
        if (existing == null) return null;
        instance.setId(instanceId);
        instance.setUpdatedAt(new Date());
        instances.put(instanceId, instance);
        return instance;
    }

    @Override
    public boolean deleteInstance(String instanceId) {
        return instances.remove(instanceId) != null;
    }

    @Override
    public boolean startInstance(String instanceId) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("running");
        instance.setCurrentInstances(1);
        instance.setHealthStatus("healthy");
        instance.setLastHeartbeat(new Date());
        return true;
    }

    @Override
    public boolean stopInstance(String instanceId) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("stopped");
        instance.setCurrentInstances(0);
        instance.setHealthStatus("unknown");
        return true;
    }

    @Override
    public boolean restartInstance(String instanceId) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("restarting");
        instance.setLastHeartbeat(new Date());
        instance.setStatus("running");
        instance.setHealthStatus("healthy");
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
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return null;
        instance.setMaxInstances(replicas);
        if ("running".equals(instance.getStatus())) {
            instance.setCurrentInstances(Math.min(replicas, instance.getCurrentInstances()));
        }
        return instance;
    }

    @Override
    public HostingInstanceDTO updateResources(String instanceId, double cpuLimit, long memoryLimit) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return null;
        instance.setCpuLimit(cpuLimit);
        instance.setMemoryLimit(memoryLimit);
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
}
