package net.ooder.skillcenter.sdk.protocol.impl;

import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.AlertInfoDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.AlertRuleConfigDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.LogQueryDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.MetricQueryDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.ObservationConfigDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.ObservationLogDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.ObservationMetricDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.ObservationReportDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.ObservationSnapshotDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.ObservationStatusDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.ObservationTraceDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.ReportConfigDTO;
import net.ooder.nexus.skillcenter.dto.protocol.ObservationDTO.TraceQueryDTO;
import net.ooder.skillcenter.sdk.protocol.ObservationProtocolAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ObservationProtocolAdapterMockImpl implements ObservationProtocolAdapter {

    private static final Logger log = LoggerFactory.getLogger(ObservationProtocolAdapterMockImpl.class);

    private final List<ObservationEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, ObservationStatusDTO> observationStatuses = new ConcurrentHashMap<>();
    private final Map<String, List<AlertRuleConfigDTO>> alertRules = new ConcurrentHashMap<>();
    private final Map<String, List<AlertInfoDTO>> activeAlerts = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> startObservation(String targetId, ObservationConfigDTO config) {
        log.debug("[ObservationMock] Start observation for target: {}", targetId);
        return CompletableFuture.runAsync(() -> {
            ObservationStatusDTO status = new ObservationStatusDTO();
            status.setTargetId(targetId);
            status.setObserving(true);
            status.setStartTime(System.currentTimeMillis());
            status.setLastUpdate(System.currentTimeMillis());
            status.setMetricsCount(0);
            status.setLogsCount(0);
            status.setTracesCount(0);
            status.setAlertsCount(0);
            observationStatuses.put(targetId, status);
        });
    }

    @Override
    public CompletableFuture<Void> stopObservation(String targetId) {
        log.debug("[ObservationMock] Stop observation for target: {}", targetId);
        return CompletableFuture.runAsync(() -> {
            ObservationStatusDTO status = observationStatuses.get(targetId);
            if (status != null) {
                status.setObserving(false);
            }
        });
    }

    @Override
    public CompletableFuture<ObservationStatusDTO> getObservationStatus(String targetId) {
        log.debug("[ObservationMock] Get observation status for target: {}", targetId);
        return CompletableFuture.supplyAsync(() -> {
            ObservationStatusDTO status = observationStatuses.get(targetId);
            if (status == null) {
                status = new ObservationStatusDTO();
                status.setTargetId(targetId);
                status.setObserving(false);
                status.setMetricsCount(100);
                status.setLogsCount(500);
                status.setTracesCount(50);
                status.setAlertsCount(2);
            }
            return status;
        });
    }

    @Override
    public CompletableFuture<List<ObservationMetricDTO>> getMetrics(String targetId, MetricQueryDTO query) {
        log.debug("[ObservationMock] Get metrics for target: {}", targetId);
        return CompletableFuture.supplyAsync(() -> createMockMetrics(targetId, query.getLimit()));
    }

    @Override
    public CompletableFuture<List<ObservationLogDTO>> getLogs(String targetId, LogQueryDTO query) {
        log.debug("[ObservationMock] Get logs for target: {}", targetId);
        return CompletableFuture.supplyAsync(() -> createMockLogs(targetId, query.getLimit()));
    }

    @Override
    public CompletableFuture<List<ObservationTraceDTO>> getTraces(String targetId, TraceQueryDTO query) {
        log.debug("[ObservationMock] Get traces for target: {}", targetId);
        return CompletableFuture.supplyAsync(() -> createMockTraces(targetId, query.getLimit()));
    }

    @Override
    public CompletableFuture<ObservationSnapshotDTO> getSnapshot(String targetId) {
        log.debug("[ObservationMock] Get snapshot for target: {}", targetId);
        return CompletableFuture.supplyAsync(() -> createMockSnapshot(targetId));
    }

    @Override
    public CompletableFuture<Void> setAlertRule(String targetId, AlertRuleConfigDTO rule) {
        log.debug("[ObservationMock] Set alert rule for target: {}", targetId);
        return CompletableFuture.runAsync(() -> {
            List<AlertRuleConfigDTO> rules = alertRules.computeIfAbsent(targetId, id -> new ArrayList<>());
            rules.add(rule);
        });
    }

    @Override
    public CompletableFuture<Void> removeAlertRule(String ruleId) {
        log.debug("[ObservationMock] Remove alert rule: {}", ruleId);
        return CompletableFuture.runAsync(() -> {
            alertRules.values().forEach(list -> {
                list.removeIf(rule -> ruleId.equals(rule.getRuleId()));
            });
        });
    }

    @Override
    public CompletableFuture<List<AlertRuleConfigDTO>> getAlertRules(String targetId) {
        log.debug("[ObservationMock] Get alert rules for target: {}", targetId);
        return CompletableFuture.supplyAsync(() -> {
            List<AlertRuleConfigDTO> rules = alertRules.get(targetId);
            if (rules == null || rules.isEmpty()) {
                rules = createMockAlertRules(targetId);
            }
            return rules;
        });
    }

    @Override
    public CompletableFuture<List<AlertInfoDTO>> getActiveAlerts(String targetId) {
        log.debug("[ObservationMock] Get active alerts for target: {}", targetId);
        return CompletableFuture.supplyAsync(() -> {
            List<AlertInfoDTO> alerts = activeAlerts.get(targetId);
            if (alerts == null || alerts.isEmpty()) {
                alerts = createMockAlerts(targetId);
            }
            return alerts;
        });
    }

    @Override
    public CompletableFuture<Void> acknowledgeAlert(String alertId) {
        log.debug("[ObservationMock] Acknowledge alert: {}", alertId);
        return CompletableFuture.runAsync(() -> {
            activeAlerts.values().forEach(list -> {
                list.forEach(alert -> {
                    if (alertId.equals(alert.getAlertId())) {
                        alert.setAcknowledged(true);
                        alert.setAcknowledgedAt(System.currentTimeMillis());
                        alert.setAcknowledgedBy("admin");
                    }
                });
            });
        });
    }

    @Override
    public CompletableFuture<ObservationReportDTO> generateReport(String targetId, ReportConfigDTO config) {
        log.debug("[ObservationMock] Generate report for target: {}", targetId);
        return CompletableFuture.supplyAsync(() -> createMockReport(targetId, config));
    }

    @Override
    public void addObservationListener(ObservationEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeObservationListener(ObservationEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private List<ObservationMetricDTO> createMockMetrics(String targetId, int limit) {
        List<ObservationMetricDTO> metrics = new ArrayList<>();
        String[] types = {"cpu_usage", "memory_usage", "network_latency", "request_count"};
        
        for (int i = 0; i < Math.min(limit, 20); i++) {
            ObservationMetricDTO metric = new ObservationMetricDTO();
            metric.setMetricId("metric-" + UUID.randomUUID().toString().substring(0, 8));
            metric.setMetricType(types[i % types.length]);
            metric.setTargetId(targetId);
            metric.setValue(Math.random() * 100);
            metric.setUnit("%");
            metric.setTimestamp(System.currentTimeMillis() - (i * 60000L));
            
            Map<String, String> tags = new HashMap<>();
            tags.put("host", "server-001");
            tags.put("region", "default");
            metric.setTags(tags);
            
            metrics.add(metric);
        }
        return metrics;
    }

    private List<ObservationLogDTO> createMockLogs(String targetId, int limit) {
        List<ObservationLogDTO> logs = new ArrayList<>();
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] messages = {
            "Request processed successfully",
            "Connection timeout detected",
            "Failed to execute skill",
            "Cache miss for key"
        };
        
        for (int i = 0; i < Math.min(limit, 50); i++) {
            ObservationLogDTO logEntry = new ObservationLogDTO();
            logEntry.setLogId("log-" + UUID.randomUUID().toString().substring(0, 8));
            logEntry.setTargetId(targetId);
            logEntry.setLevel(i % 4);
            logEntry.setMessage(messages[i % messages.length]);
            logEntry.setTimestamp(System.currentTimeMillis() - (i * 30000L));
            
            Map<String, Object> context = new HashMap<>();
            context.put("thread", "main-" + i);
            context.put("requestId", "req-" + i);
            logEntry.setContext(context);
            
            logs.add(logEntry);
        }
        return logs;
    }

    private List<ObservationTraceDTO> createMockTraces(String targetId, int limit) {
        List<ObservationTraceDTO> traces = new ArrayList<>();
        String[] operations = {"executeSkill", "loadManifest", "syncState", "joinGroup"};
        
        for (int i = 0; i < Math.min(limit, 10); i++) {
            ObservationTraceDTO trace = new ObservationTraceDTO();
            trace.setTraceId("trace-" + UUID.randomUUID().toString().substring(0, 8));
            trace.setTargetId(targetId);
            trace.setOperationType("RPC");
            trace.setOperationName(operations[i % operations.length]);
            trace.setDuration((long)(Math.random() * 1000));
            trace.setSuccess(Math.random() > 0.1);
            trace.setErrorMessage(trace.isSuccess() ? null : "Operation failed");
            trace.setTimestamp(System.currentTimeMillis() - (i * 120000L));
            trace.setSpans(new ArrayList<>());
            
            traces.add(trace);
        }
        return traces;
    }

    private ObservationSnapshotDTO createMockSnapshot(String targetId) {
        ObservationSnapshotDTO snapshot = new ObservationSnapshotDTO();
        snapshot.setTargetId(targetId);
        snapshot.setTimestamp(System.currentTimeMillis());
        snapshot.setLatestMetrics(new HashMap<>());
        snapshot.setRecentLogs(createMockLogs(targetId, 10));
        snapshot.setActiveAlerts(createMockAlerts(targetId));
        snapshot.setHealthStatus("HEALTHY");
        return snapshot;
    }

    private List<AlertRuleConfigDTO> createMockAlertRules(String targetId) {
        List<AlertRuleConfigDTO> rules = new ArrayList<>();
        
        AlertRuleConfigDTO rule1 = new AlertRuleConfigDTO();
        rule1.setRuleId("rule-001");
        rule1.setTargetId(targetId);
        rule1.setMetricType("cpu_usage");
        rule1.setCondition("greater_than");
        rule1.setThreshold(80.0);
        rule1.setDuration(300);
        rule1.setSeverity("WARNING");
        rule1.setMessage("CPU usage exceeds 80%");
        rule1.setEnabled(true);
        rules.add(rule1);
        
        AlertRuleConfigDTO rule2 = new AlertRuleConfigDTO();
        rule2.setRuleId("rule-002");
        rule2.setTargetId(targetId);
        rule2.setMetricType("memory_usage");
        rule2.setCondition("greater_than");
        rule2.setThreshold(90.0);
        rule2.setDuration(180);
        rule2.setSeverity("CRITICAL");
        rule2.setMessage("Memory usage exceeds 90%");
        rule2.setEnabled(true);
        rules.add(rule2);
        
        return rules;
    }

    private List<AlertInfoDTO> createMockAlerts(String targetId) {
        List<AlertInfoDTO> alerts = new ArrayList<>();
        
        AlertInfoDTO alert1 = new AlertInfoDTO();
        alert1.setAlertId("alert-001");
        alert1.setRuleId("rule-001");
        alert1.setTargetId(targetId);
        alert1.setMetricType("cpu_usage");
        alert1.setValue(85.5);
        alert1.setThreshold(80.0);
        alert1.setSeverity("WARNING");
        alert1.setMessage("CPU usage is 85.5%, exceeds threshold 80%");
        alert1.setTriggeredAt(System.currentTimeMillis() - 3600000L);
        alert1.setAcknowledged(false);
        alerts.add(alert1);
        
        AlertInfoDTO alert2 = new AlertInfoDTO();
        alert2.setAlertId("alert-002");
        alert2.setRuleId("rule-002");
        alert2.setTargetId(targetId);
        alert2.setMetricType("memory_usage");
        alert2.setValue(92.3);
        alert2.setThreshold(90.0);
        alert2.setSeverity("CRITICAL");
        alert2.setMessage("Memory usage is 92.3%, exceeds threshold 90%");
        alert2.setTriggeredAt(System.currentTimeMillis() - 1800000L);
        alert2.setAcknowledged(true);
        alert2.setAcknowledgedBy("admin");
        alert2.setAcknowledgedAt(System.currentTimeMillis() - 900000L);
        alerts.add(alert2);
        
        return alerts;
    }

    private ObservationReportDTO createMockReport(String targetId, ReportConfigDTO config) {
        ObservationReportDTO report = new ObservationReportDTO();
        report.setReportId("report-" + UUID.randomUUID().toString().substring(0, 8));
        report.setTargetId(targetId);
        report.setReportType(config.getReportType());
        report.setStartTime(config.getStartTime());
        report.setEndTime(config.getEndTime());
        report.setGeneratedAt(System.currentTimeMillis());
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRequests", 1000);
        summary.put("successRate", 98.5);
        summary.put("avgLatency", 45.2);
        summary.put("errorCount", 15);
        report.setSummary(summary);
        
        report.setMetrics(createMockMetrics(targetId, 10));
        report.setAlerts(createMockAlerts(targetId));
        report.setContent("Observation report generated for target: " + targetId);
        
        return report;
    }
}
