package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.service.SystemService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class SystemServiceMockImpl implements SystemService {

    @Override
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "运行中");
        status.put("service", "SkillCenter");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @Override
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("javaVersion", System.getProperty("java.version"));
        config.put("osName", System.getProperty("os.name"));
        return config;
    }

    @Override
    public Map<String, Object> getSystemVersion() {
        Map<String, Object> version = new HashMap<>();
        version.put("version", "2.0");
        version.put("name", "SkillCenter");
        return version;
    }

    @Override
    public Map<String, Object> getSystemResources() {
        Map<String, Object> resources = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        resources.put("totalMemory", runtime.totalMemory());
        resources.put("freeMemory", runtime.freeMemory());
        return resources;
    }

    @Override
    public Map<String, Object> updateSystemConfig(Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置更新成功");
        return result;
    }

    @Override
    public Map<String, Object> restartSystem() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "系统重启命令已发出");
        return result;
    }

    @Override
    public Map<String, Object> shutdownSystem() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "系统关闭命令已发出");
        return result;
    }

    @Override
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "健康");
        health.put("cpu", "正常");
        health.put("memory", "正常");
        return health;
    }

    @Override
    public Map<String, Object> clearSystemCache() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "缓存清理成功");
        return result;
    }

    @Override
    public List<Map<String, Object>> getSystemOperations() {
        List<Map<String, Object>> operations = new ArrayList<>();
        Map<String, Object> op = new HashMap<>();
        op.put("id", "op-1");
        op.put("type", "SYSTEM_START");
        op.put("description", "系统启动成功");
        operations.add(op);
        return operations;
    }

    @Override
    public List<Map<String, Object>> getSystemLogs(String level) {
        List<Map<String, Object>> logs = new ArrayList<>();
        Map<String, Object> log = new HashMap<>();
        log.put("id", "log-1");
        log.put("level", "INFO");
        log.put("message", "系统启动成功");
        logs.add(log);
        return logs;
    }

    @Override
    public Map<String, Object> clearSystemLogs() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "日志清空成功");
        return result;
    }
}
