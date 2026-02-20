package net.ooder.skillcenter.sdk.cloud.impl;

import net.ooder.skillcenter.sdk.cloud.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TencentHostingProviderMockImpl implements CloudHostingProvider {

    private static final Logger log = LoggerFactory.getLogger(TencentHostingProviderMockImpl.class);
    private final Map<String, CloudInstance> instances = new ConcurrentHashMap<>();

    @Override
    public String getProviderName() {
        return "tencent";
    }

    @Override
    public List<String> getSupportedRegions() {
        return Arrays.asList(
            "ap-guangzhou", "ap-shanghai", "ap-beijing", "ap-chengdu",
            "ap-chongqing", "ap-nanjing", "ap-hongkong",
            "ap-singapore", "ap-tokyo", "ap-seoul"
        );
    }

    @Override
    public List<String> getSupportedInstanceTypes() {
        return Arrays.asList("tke", "eks");
    }

    @Override
    public CloudInstance createInstance(CloudHostingConfig config) throws CloudHostingException {
        log.info("[TencentProvider] Creating instance: {} in region: {}", config.getInstanceName(), config.getRegion());
        
        String instanceId = "tencent-" + config.getProviderType() + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        CloudInstance instance = new CloudInstance();
        instance.setId(instanceId);
        instance.setName(config.getInstanceName());
        instance.setProvider("tencent");
        instance.setProviderType(config.getProviderType());
        instance.setRegion(config.getRegion());
        instance.setStatus("creating");
        instance.setReplicas(config.getScaling() != null ? config.getScaling().getMinReplicas() : 1);
        instance.setCreatedAt(System.currentTimeMillis());
        instance.setResources(config.getResources());
        
        instances.put(instanceId, instance);
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                instance.setStatus("running");
                instance.setEndpoint(generateEndpoint(config));
                log.info("[TencentProvider] Instance {} is now running", instanceId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        return instance;
    }

    private String generateEndpoint(CloudHostingConfig config) {
        return String.format("http://%s.%s.tencentcloudapi.com", config.getInstanceName(), config.getRegion());
    }

    @Override
    public CloudInstance getInstance(String instanceId) throws CloudHostingException {
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException("tencent", "InstanceNotFound", "Instance not found: " + instanceId);
        }
        return instance;
    }

    @Override
    public List<CloudInstance> listInstances() throws CloudHostingException {
        return new ArrayList<>(instances.values());
    }

    @Override
    public boolean deleteInstance(String instanceId) throws CloudHostingException {
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException("tencent", "InstanceNotFound", "Instance not found: " + instanceId);
        }
        instance.setStatus("terminating");
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                instances.remove(instanceId);
                log.info("[TencentProvider] Instance {} deleted", instanceId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return true;
    }

    @Override
    public boolean startInstance(String instanceId) throws CloudHostingException {
        CloudInstance instance = getInstance(instanceId);
        if ("running".equals(instance.getStatus())) {
            return true;
        }
        instance.setStatus("starting");
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                instance.setStatus("running");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return true;
    }

    @Override
    public boolean stopInstance(String instanceId) throws CloudHostingException {
        CloudInstance instance = getInstance(instanceId);
        instance.setStatus("stopping");
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                instance.setStatus("stopped");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return true;
    }

    @Override
    public boolean restartInstance(String instanceId) throws CloudHostingException {
        CloudInstance instance = getInstance(instanceId);
        instance.setStatus("restarting");
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                instance.setStatus("running");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return true;
    }

    @Override
    public boolean scaleInstance(String instanceId, int replicas) throws CloudHostingException {
        CloudInstance instance = getInstance(instanceId);
        instance.setReplicas(replicas);
        log.info("[TencentProvider] Scaled instance {} to {} replicas", instanceId, replicas);
        return true;
    }

    @Override
    public InstanceMetrics getMetrics(String instanceId) throws CloudHostingException {
        getInstance(instanceId);
        
        InstanceMetrics metrics = new InstanceMetrics();
        metrics.setCpuUsage(15 + Math.random() * 45);
        metrics.setMemoryUsage(25 + Math.random() * 35);
        metrics.setNetworkIn((long)(Math.random() * 1024 * 1024 * 80));
        metrics.setNetworkOut((long)(Math.random() * 1024 * 1024 * 40));
        metrics.setRequestCount((int)(Math.random() * 8000));
        metrics.setAvgLatency(8 + Math.random() * 40);
        metrics.setTimestamp(System.currentTimeMillis());
        
        return metrics;
    }

    @Override
    public List<LogEntry> getLogs(String instanceId, LogQueryOptions options) throws CloudHostingException {
        getInstance(instanceId);
        
        List<LogEntry> logs = new ArrayList<>();
        String[] levels = {"INFO", "DEBUG", "WARN", "ERROR"};
        String[] messages = {
            "Container started",
            "Service ready",
            "Processing request",
            "Memory usage normal",
            "Connection established"
        };
        
        for (int i = 0; i < (options.getLimit() > 0 ? options.getLimit() : 50); i++) {
            LogEntry entry = new LogEntry();
            entry.setTimestamp(System.currentTimeMillis() - i * 60000);
            entry.setLevel(levels[i % levels.length]);
            entry.setMessage(messages[i % messages.length]);
            entry.setSource(instanceId);
            logs.add(entry);
        }
        
        return logs;
    }

    @Override
    public List<InstanceEvent> getEvents(String instanceId) throws CloudHostingException {
        getInstance(instanceId);
        
        List<InstanceEvent> events = new ArrayList<>();
        String[] types = {"Scale", "Deploy", "Restart", "Update"};
        String[] messages = {
            "Auto-scaled to 3 replicas",
            "Deployed version v2.0.0",
            "Health check triggered restart",
            "Resource limits updated"
        };
        
        for (int i = 0; i < 5; i++) {
            InstanceEvent event = new InstanceEvent();
            event.setTimestamp(System.currentTimeMillis() - i * 3600000);
            event.setType(types[i % types.length]);
            event.setMessage(messages[i % messages.length]);
            event.setReason("System");
            events.add(event);
        }
        
        return events;
    }

    @Override
    public boolean updateResources(String instanceId, ResourceConfig resources) throws CloudHostingException {
        CloudInstance instance = getInstance(instanceId);
        instance.setResources(resources);
        log.info("[TencentProvider] Updated resources for instance {}", instanceId);
        return true;
    }

    @Override
    public ServiceEndpoint getServiceEndpoint(String instanceId) throws CloudHostingException {
        CloudInstance instance = getInstance(instanceId);
        
        ServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.setEndpoint(instance.getEndpoint());
        endpoint.setProtocol("http");
        endpoint.setHost(instance.getName() + "." + instance.getRegion() + ".tencentcloudapi.com");
        endpoint.setPort(8080);
        endpoint.setDnsName(instance.getName() + "." + instance.getRegion() + ".tencentcloudapi.com");
        
        return endpoint;
    }

    @Override
    public boolean healthCheck(String instanceId) {
        CloudInstance instance = instances.get(instanceId);
        return instance != null && "running".equals(instance.getStatus());
    }

    @Override
    public CostEstimate estimateCost(CloudHostingConfig config) {
        CostEstimate estimate = new CostEstimate();
        
        double cpuCost = config.getResources().getCpuLimit() * 0.04;
        double memCost = config.getResources().getMemoryLimit() / 1024.0 / 1024.0 / 1024.0 * 0.008;
        double hourlyCost = cpuCost + memCost;
        
        estimate.setHourlyCost(hourlyCost);
        estimate.setDailyCost(hourlyCost * 24);
        estimate.setMonthlyCost(hourlyCost * 24 * 30);
        estimate.setCurrency("CNY");
        
        CostBreakdown breakdown = new CostBreakdown();
        breakdown.setCompute(cpuCost);
        breakdown.setStorage(0);
        breakdown.setNetwork(0);
        estimate.setBreakdown(breakdown);
        
        return estimate;
    }

    @Override
    public CostActual getActualCost(String instanceId, long startTime, long endTime) {
        CloudInstance instance = instances.get(instanceId);
        
        CostActual cost = new CostActual();
        long hours = (endTime - startTime) / 3600000;
        double hourlyCost = 0.08;
        
        if (instance != null && instance.getResources() != null) {
            hourlyCost = instance.getResources().getCpuLimit() * 0.04 + 
                        instance.getResources().getMemoryLimit() / 1024.0 / 1024.0 / 1024.0 * 0.008;
        }
        
        cost.setTotalCost(hourlyCost * hours);
        cost.setCurrency("CNY");
        cost.setStartTime(startTime);
        cost.setEndTime(endTime);
        
        return cost;
    }
}
