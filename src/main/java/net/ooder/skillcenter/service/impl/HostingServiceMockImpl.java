package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.HostingInstanceDTO;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.sdk.HostingSdkAdapter;
import net.ooder.skillcenter.service.HostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostingServiceMockImpl implements HostingService {

    @Autowired
    private HostingSdkAdapter sdkAdapter;

    @Override
    public List<HostingInstanceDTO> getAllInstances() {
        return sdkAdapter.getAllInstances();
    }

    @Override
    public PageResult<HostingInstanceDTO> getInstances(int page, int size) {
        return sdkAdapter.getInstances(page, size);
    }

    @Override
    public HostingInstanceDTO getInstance(String instanceId) {
        return sdkAdapter.getInstance(instanceId);
    }

    @Override
    public HostingInstanceDTO createInstance(HostingInstanceDTO instance) {
        return sdkAdapter.createInstance(instance);
    }

    @Override
    public HostingInstanceDTO updateInstance(String instanceId, HostingInstanceDTO instance) {
        return sdkAdapter.updateInstance(instanceId, instance);
    }

    @Override
    public boolean deleteInstance(String instanceId) {
        return sdkAdapter.deleteInstance(instanceId);
    }

    @Override
    public boolean startInstance(String instanceId) {
        return sdkAdapter.startInstance(instanceId);
    }

    @Override
    public boolean stopInstance(String instanceId) {
        return sdkAdapter.stopInstance(instanceId);
    }

    @Override
    public boolean restartInstance(String instanceId) {
        return sdkAdapter.restartInstance(instanceId);
    }

    @Override
    public String getInstanceStatus(String instanceId) {
        return sdkAdapter.getInstanceStatus(instanceId);
    }

    @Override
    public String getInstanceHealth(String instanceId) {
        return sdkAdapter.getInstanceHealth(instanceId);
    }

    @Override
    public HostingInstanceDTO scaleInstance(String instanceId, int replicas) {
        return sdkAdapter.scaleInstance(instanceId, replicas);
    }

    @Override
    public HostingInstanceDTO updateResources(String instanceId, double cpuLimit, long memoryLimit) {
        return sdkAdapter.updateResources(instanceId, cpuLimit, memoryLimit);
    }

    @Override
    public List<HostingInstanceDTO> getInstancesBySkill(String skillId) {
        return sdkAdapter.getInstancesBySkill(skillId);
    }

    @Override
    public List<HostingInstanceDTO> getInstancesByOwner(String owner) {
        return sdkAdapter.getInstancesByOwner(owner);
    }

    @Override
    public long getTotalInstances() {
        return sdkAdapter.getTotalInstances();
    }

    @Override
    public long getRunningInstances() {
        return sdkAdapter.getRunningInstances();
    }
}
