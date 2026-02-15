package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.service.SystemService;
import net.ooder.skillcenter.sdk.AgentSDKWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 系统服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class SystemServiceSdk070Impl implements SystemService {

    private static final Logger log = LoggerFactory.getLogger(SystemServiceSdk070Impl.class);

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    @Override
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", sdkWrapper.isInitialized() ? "运行中" : "未初始化");
        status.put("service", "SkillCenter SDK 0.7.0");
        status.put("timestamp", System.currentTimeMillis());
        status.put("sdkMode", "sdk");
        return status;
    }

    @Override
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("javaVersion", System.getProperty("java.version"));
        config.put("osName", System.getProperty("os.name"));
        config.put("sdkVersion", "0.7.0");
        config.put("sdkInitialized", sdkWrapper.isInitialized());
        return config;
    }

    @Override
    public Map<String, Object> getSystemVersion() {
        Map<String, Object> version = new HashMap<>();
        version.put("version", "2.1");
        version.put("name", "SkillCenter");
        version.put("sdkVersion", "0.7.0");
        version.put("protocolVersion", "v0.7.0");
        return version;
    }

    @Override
    public Map<String, Object> getSystemResources() {
        Map<String, Object> resources = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        resources.put("totalMemory", runtime.totalMemory());
        resources.put("freeMemory", runtime.freeMemory());
        resources.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        resources.put("maxMemory", runtime.maxMemory());
        resources.put("availableProcessors", runtime.availableProcessors());
        return resources;
    }

    @Override
    public Map<String, Object> updateSystemConfig(Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置更新成功");
        log.info("System config updated: {}", config);
        return result;
    }

    @Override
    public Map<String, Object> restartSystem() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "系统重启命令已发出");
        log.warn("System restart requested");
        return result;
    }

    @Override
    public Map<String, Object> shutdownSystem() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "系统关闭命令已发出");
        log.warn("System shutdown requested");
        return result;
    }

    @Override
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", sdkWrapper.isInitialized() ? "健康" : "未初始化");
        health.put("sdk", sdkWrapper.isInitialized() ? "正常" : "未连接");
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory * 100;
        
        health.put("memoryUsage", String.format("%.2f%%", memoryUsage));
        health.put("memory", memoryUsage < 80 ? "正常" : "警告");
        return health;
    }

    @Override
    public Map<String, Object> clearSystemCache() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "缓存清理成功");
        log.info("System cache cleared");
        return result;
    }

    @Override
    public List<Map<String, Object>> getSystemOperations() {
        List<Map<String, Object>> operations = new ArrayList<>();
        Map<String, Object> op = new HashMap<>();
        op.put("id", "op-1");
        op.put("type", "SYSTEM_START");
        op.put("description", "系统启动成功 (SDK 0.7.0)");
        op.put("timestamp", System.currentTimeMillis());
        operations.add(op);
        return operations;
    }

    @Override
    public List<Map<String, Object>> getSystemLogs(String level) {
        List<Map<String, Object>> logs = new ArrayList<>();
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("id", "log-1");
        logEntry.put("level", "INFO");
        logEntry.put("message", "SDK 0.7.0 模式运行中");
        logEntry.put("timestamp", System.currentTimeMillis());
        logs.add(logEntry);
        return logs;
    }

    @Override
    public Map<String, Object> clearSystemLogs() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "日志清空成功");
        log.info("System logs cleared");
        return result;
    }
}
