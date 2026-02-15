package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.HostingInstanceDTO;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.service.HostingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 托管服务SDK 0.7.0实现 - 符合v0.7.0协议规范
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class HostingServiceSdk070Impl implements HostingService {

    private static final Logger log = LoggerFactory.getLogger(HostingServiceSdk070Impl.class);

    private final Map<String, HostingInstanceDTO> instances = new ConcurrentHashMap<>();

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
        instance.setStatus("pending");
        instances.put(id, instance);
        log.info("Created hosting instance: {} for skill: {}", id, instance.getSkillId());
        return instance;
    }

    @Override
    public HostingInstanceDTO updateInstance(String instanceId, HostingInstanceDTO instance) {
        HostingInstanceDTO existing = instances.get(instanceId);
        if (existing == null) return null;
        instance.setId(instanceId);
        instance.setUpdatedAt(new Date());
        instances.put(instanceId, instance);
        log.info("Updated hosting instance: {}", instanceId);
        return instance;
    }

    @Override
    public boolean deleteInstance(String instanceId) {
        HostingInstanceDTO instance = instances.remove(instanceId);
        if (instance != null) {
            log.info("Deleted hosting instance: {}", instanceId);
            return true;
        }
        return false;
    }

    @Override
    public boolean startInstance(String instanceId) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("running");
        instance.setCurrentInstances(1);
        instance.setHealthStatus("healthy");
        instance.setLastHeartbeat(new Date());
        log.info("Started hosting instance: {}", instanceId);
        return true;
    }

    @Override
    public boolean stopInstance(String instanceId) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("stopped");
        instance.setCurrentInstances(0);
        instance.setHealthStatus("unknown");
        log.info("Stopped hosting instance: {}", instanceId);
        return true;
    }

    @Override
    public boolean restartInstance(String instanceId) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return false;
        instance.setStatus("running");
        instance.setHealthStatus("healthy");
        instance.setLastHeartbeat(new Date());
        log.info("Restarted hosting instance: {}", instanceId);
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
        if (instance == null) return null;
        return instance.getHealthStatus();
    }

    @Override
    public HostingInstanceDTO scaleInstance(String instanceId, int replicas) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return null;
        instance.setMaxInstances(replicas);
        log.info("Scaled hosting instance: {} to {} replicas", instanceId, replicas);
        return instance;
    }

    @Override
    public HostingInstanceDTO updateResources(String instanceId, double cpuLimit, long memoryLimit) {
        HostingInstanceDTO instance = instances.get(instanceId);
        if (instance == null) return null;
        instance.setCpuLimit(cpuLimit);
        instance.setMemoryLimit(memoryLimit);
        log.info("Updated resources for hosting instance: {}", instanceId);
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
