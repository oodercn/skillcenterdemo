package net.ooder.skillcenter.sdk.kubernetes;

import net.ooder.skillcenter.sdk.cloud.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KubernetesHostingProviderMockImpl implements CloudHostingProvider {

    private static final Logger log = LoggerFactory.getLogger(KubernetesHostingProviderMockImpl.class);
    private static final String PROVIDER_NAME = "kubernetes";
    
    private final Map<String, CloudInstance> instances = new HashMap<>();
    
    public KubernetesHostingProviderMockImpl() {
        log.info("[KubernetesProvider] Mock implementation initialized");
        initMockData();
    }
    
    private void initMockData() {
        CloudInstance instance1 = new CloudInstance();
        instance1.setId("k8s-deploy-001");
        instance1.setName("skill-web-service");
        instance1.setProvider(PROVIDER_NAME);
        instance1.setProviderType("deployment");
        instance1.setRegion("default");
        instance1.setStatus("running");
        instance1.setReplicas(3);
        instance1.setCreatedAt(System.currentTimeMillis() - 3600000);
        instances.put(instance1.getId(), instance1);
        
        log.info("[KubernetesProvider] Mock data initialized: {} instances", instances.size());
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public List<String> getSupportedRegions() {
        return Arrays.asList("default", "kube-system", "production", "staging", "development");
    }

    @Override
    public List<String> getSupportedInstanceTypes() {
        return Arrays.asList("deployment", "statefulset", "daemonset", "job", "cronjob");
    }

    @Override
    public CloudInstance createInstance(CloudHostingConfig config) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Creating instance: {}", config.getInstanceName());
        
        CloudInstance instance = new CloudInstance();
        instance.setId("k8s-" + UUID.randomUUID().toString().substring(0, 8));
        instance.setName(config.getInstanceName());
        instance.setProvider(PROVIDER_NAME);
        instance.setProviderType(config.getProviderType() != null ? config.getProviderType() : "deployment");
        instance.setRegion(config.getRegion() != null ? config.getRegion() : "default");
        instance.setStatus("creating");
        instance.setReplicas(config.getScaling() != null ? config.getScaling().getMinReplicas() : 1);
        instance.setCreatedAt(System.currentTimeMillis());
        
        instances.put(instance.getId(), instance);
        
        log.info("[KubernetesProvider:Mock] Created instance: {}", instance.getId());
        return instance;
    }

    @Override
    public List<CloudInstance> listInstances() throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Listing instances");
        return new ArrayList<>(instances.values());
    }

    @Override
    public CloudInstance getInstance(String instanceId) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Getting instance: {}", instanceId);
        
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        return instance;
    }

    @Override
    public boolean deleteInstance(String instanceId) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Deleting instance: {}", instanceId);
        
        if (!instances.containsKey(instanceId)) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        instances.remove(instanceId);
        log.info("[KubernetesProvider:Mock] Deleted instance: {}", instanceId);
        return true;
    }

    @Override
    public boolean startInstance(String instanceId) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Starting instance: {}", instanceId);
        
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        instance.setStatus("running");
        instance.setReplicas(instance.getReplicas() > 0 ? instance.getReplicas() : 1);
        return true;
    }

    @Override
    public boolean stopInstance(String instanceId) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Stopping instance: {}", instanceId);
        
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        instance.setStatus("stopped");
        instance.setReplicas(0);
        return true;
    }

    @Override
    public boolean restartInstance(String instanceId) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Restarting instance: {}", instanceId);
        
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        instance.setStatus("starting");
        return true;
    }

    @Override
    public boolean scaleInstance(String instanceId, int replicas) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Scaling instance: {} to {} replicas", instanceId, replicas);
        
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        instance.setReplicas(replicas);
        return true;
    }

    @Override
    public InstanceMetrics getMetrics(String instanceId) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Getting metrics for instance: {}", instanceId);
        
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        InstanceMetrics metrics = new InstanceMetrics();
        metrics.setInstanceId(instanceId);
        metrics.setTimestamp(System.currentTimeMillis());
        metrics.setCpuUsage(Math.random() * 2);
        metrics.setMemoryUsage(Math.random() * 1024 * 1024 * 1024);
        metrics.setReplicas(instance.getReplicas());
        
        return metrics;
    }

    @Override
    public List<LogEntry> getLogs(String instanceId, LogQueryOptions options) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Getting logs for instance: {}", instanceId);
        
        if (!instances.containsKey(instanceId)) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        List<LogEntry> logs = new ArrayList<>();
        int limit = options != null && options.getLimit() > 0 ? options.getLimit() : 50;
        
        for (int i = 0; i < Math.min(limit, 10); i++) {
            LogEntry entry = new LogEntry();
            entry.setTimestamp(System.currentTimeMillis() - i * 1000);
            entry.setMessage("[INFO] Pod " + instanceId + " log entry " + i);
            entry.setLevel("INFO");
            entry.setSource("pod-" + instanceId + "-" + i);
            logs.add(entry);
        }
        
        return logs;
    }

    @Override
    public List<InstanceEvent> getEvents(String instanceId) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Getting events for instance: {}", instanceId);
        
        if (!instances.containsKey(instanceId)) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        List<InstanceEvent> events = new ArrayList<>();
        
        InstanceEvent event1 = new InstanceEvent();
        event1.setEventId(UUID.randomUUID().toString());
        event1.setTimestamp(System.currentTimeMillis() - 3600000);
        event1.setType("Normal");
        event1.setReason("Scheduled");
        event1.setMessage("Successfully assigned pod to node");
        events.add(event1);
        
        InstanceEvent event2 = new InstanceEvent();
        event2.setEventId(UUID.randomUUID().toString());
        event2.setTimestamp(System.currentTimeMillis() - 3000000);
        event2.setType("Normal");
        event2.setReason("Started");
        event2.setMessage("Started container");
        events.add(event2);
        
        return events;
    }

    @Override
    public boolean updateResources(String instanceId, ResourceConfig resources) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Updating resources for instance: {}", instanceId);
        
        if (!instances.containsKey(instanceId)) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        return true;
    }

    @Override
    public ServiceEndpoint getServiceEndpoint(String instanceId) throws CloudHostingException {
        log.info("[KubernetesProvider:Mock] Getting service endpoint for instance: {}", instanceId);
        
        CloudInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new CloudHostingException(PROVIDER_NAME, "InstanceNotFound", "Instance not found: " + instanceId);
        }
        
        ServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.setInstanceId(instanceId);
        endpoint.setType("ClusterIP");
        endpoint.setUrl("http://" + instance.getName() + "." + instance.getRegion() + ".svc.cluster.local");
        
        return endpoint;
    }

    @Override
    public boolean healthCheck(String instanceId) {
        CloudInstance instance = instances.get(instanceId);
        return instance != null && "running".equals(instance.getStatus());
    }

    @Override
    public CostEstimate estimateCost(CloudHostingConfig config) {
        log.info("[KubernetesProvider:Mock] Estimating cost");
        
        CostEstimate estimate = new CostEstimate();
        estimate.setCurrency("CNY");
        
        double cpuCost = config.getResources().getCpuLimit() * 0.05;
        double memoryCost = config.getResources().getMemoryLimit() / (1024.0 * 1024 * 1024) * 0.01;
        
        double hourlyCost = cpuCost + memoryCost;
        
        estimate.setHourlyCost(hourlyCost);
        estimate.setDailyCost(hourlyCost * 24);
        estimate.setMonthlyCost(hourlyCost * 24 * 30);
        
        CostBreakdown breakdown = new CostBreakdown();
        breakdown.setCompute(cpuCost);
        breakdown.setMemory(memoryCost);
        estimate.setBreakdown(breakdown);
        
        return estimate;
    }

    @Override
    public CostActual getActualCost(String instanceId, long startTime, long endTime) {
        log.info("[KubernetesProvider:Mock] Getting actual cost for instance: {}", instanceId);
        
        CostActual cost = new CostActual();
        cost.setInstanceId(instanceId);
        cost.setCurrency("CNY");
        cost.setStartTime(startTime);
        cost.setEndTime(endTime);
        
        long hours = (endTime - startTime) / (1000 * 60 * 60);
        if (hours <= 0) hours = 1;
        
        double hourlyCost = 0.08;
        cost.setTotalCost(hourlyCost * hours);
        
        CostBreakdown breakdown = new CostBreakdown();
        breakdown.setCompute(hourlyCost * hours * 0.7);
        breakdown.setMemory(hourlyCost * hours * 0.3);
        cost.setBreakdown(breakdown);
        
        return cost;
    }
}
