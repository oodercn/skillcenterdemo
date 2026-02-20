package net.ooder.skillcenter.sdk.cloud.impl;

import net.ooder.skillcenter.sdk.cloud.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AliyunHostingProviderMockImpl implements CloudHostingProvider {

    private static final Logger log = LoggerFactory.getLogger(AliyunHostingProviderMockImpl.class);
    private final Map<String, CloudInstance> instances = new ConcurrentHashMap<>();

    @Override
    public String getProviderName() {
        return "aliyun";
    }

    @Override
    public List<String> getSupportedRegions() {
        return Arrays.asList(
            "cn-hangzhou", "cn-shanghai", "cn-beijing", "cn-shenzhen",
            "cn-qingdao", "cn-zhangjiakou", "cn-huhehaote", "cn-wulanchabu",
            "ap-northeast-1", "ap-southeast-1", "ap-southeast-2", "ap-southeast-3"
        );
    }

    @Override
    public List<String> getSupportedInstanceTypes() {
        return Arrays.asList("ack", "acs");
    }

    @Override
    public CloudInstance createInstance(CloudHostingConfig config) throws CloudHostingException {
        log.info("[AliyunProvider] Creating instance: {} in region: {}", config.getInstanceName(), config.getRegion());
        
        String instanceId = "aliyun-" + config.getProviderType() + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        CloudInstance instance = new CloudInstance();
        instance.setId(instanceId);
        instance.setName(config.getInstanceName());
        instance.setProvider("aliyun");
        instance.setProviderType(config.getProviderType());
        instance.setRegion(config.getRegion());
        instance.setStatus("creating");
        instance.setReplicas(config.getScaling() != null ? config.getScaling().getMinReplicas() : 1);
        instance.setCreatedAt(System.currentTimeMillis());
        instance.setResources(config.getResources());
        
        instances.put(instanceId, instance);
        
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                instance.setStatus("running");
                instance.setEndpoint(generateEndpoint(config));
                log.info("[AliyunProvider] Instance {} is now running", instanceId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        return instance;
    }

    private String generateEndpoint(CloudHostingConfig config) {
        return String.format("http://%s.%s.aliyuncs.com", config.getInstanceName(), config.getRegion());
    }

    @Override
    public CloudInstance getInstance(String instanceId) throws CloudHostingException {
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException("aliyun", "InstanceNotFound", "Instance not found: " + instanceId);
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
            throw new CloudHostingException("aliyun", "InstanceNotFound", "Instance not found: " + instanceId);
        }
        instance.setStatus("terminating");
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                instances.remove(instanceId);
                log.info("[AliyunProvider] Instance {} deleted", instanceId);
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
                Thread.sleep(2000);
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
                Thread.sleep(2000);
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
                Thread.sleep(3000);
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
        log.info("[AliyunProvider] Scaled instance {} to {} replicas", instanceId, replicas);
        return true;
    }

    @Override
    public InstanceMetrics getMetrics(String instanceId) throws CloudHostingException {
        getInstance(instanceId);
        
        InstanceMetrics metrics = new InstanceMetrics();
        metrics.setCpuUsage(20 + Math.random() * 40);
        metrics.setMemoryUsage(30 + Math.random() * 30);
        metrics.setNetworkIn((long)(Math.random() * 1024 * 1024 * 100));
        metrics.setNetworkOut((long)(Math.random() * 1024 * 1024 * 50));
        metrics.setRequestCount((int)(Math.random() * 10000));
        metrics.setAvgLatency(10 + Math.random() * 50);
        metrics.setTimestamp(System.currentTimeMillis());
        
        return metrics;
    }

    @Override
    public List<LogEntry> getLogs(String instanceId, LogQueryOptions options) throws CloudHostingException {
        getInstance(instanceId);
        
        List<LogEntry> logs = new ArrayList<>();
        String[] levels = {"INFO", "DEBUG", "WARN", "ERROR"};
        String[] messages = {
            "Request processed successfully",
            "Connecting to external service",
            "Cache updated",
            "Health check passed",
            "Configuration reloaded"
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
        String[] types = {"Scale", "Deploy", "Restart", "Config"};
        String[] messages = {
            "Scaled from 1 to 2 replicas",
            "Deployed new version v1.0.0",
            "Instance restarted",
            "Configuration updated"
        };
        
        for (int i = 0; i < 5; i++) {
            InstanceEvent event = new InstanceEvent();
            event.setTimestamp(System.currentTimeMillis() - i * 3600000);
            event.setType(types[i % types.length]);
            event.setMessage(messages[i % messages.length]);
            event.setReason("User action");
            events.add(event);
        }
        
        return events;
    }

    @Override
    public boolean updateResources(String instanceId, ResourceConfig resources) throws CloudHostingException {
        CloudInstance instance = getInstance(instanceId);
        instance.setResources(resources);
        log.info("[AliyunProvider] Updated resources for instance {}", instanceId);
        return true;
    }

    @Override
    public ServiceEndpoint getServiceEndpoint(String instanceId) throws CloudHostingException {
        CloudInstance instance = getInstance(instanceId);
        
        ServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.setEndpoint(instance.getEndpoint());
        endpoint.setProtocol("http");
        endpoint.setHost(instance.getName() + "." + instance.getRegion() + ".aliyuncs.com");
        endpoint.setPort(8080);
        endpoint.setDnsName(instance.getName() + "." + instance.getRegion() + ".aliyuncs.com");
        
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
        
        double cpuCost = config.getResources().getCpuLimit() * 0.05;
        double memCost = config.getResources().getMemoryLimit() / 1024.0 / 1024.0 / 1024.0 * 0.01;
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
        double hourlyCost = 0.1;
        
        if (instance != null && instance.getResources() != null) {
            hourlyCost = instance.getResources().getCpuLimit() * 0.05 + 
                        instance.getResources().getMemoryLimit() / 1024.0 / 1024.0 / 1024.0 * 0.01;
        }
        
        cost.setTotalCost(hourlyCost * hours);
        cost.setCurrency("CNY");
        cost.setStartTime(startTime);
        cost.setEndTime(endTime);
        
        return cost;
    }
}
