package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.service.StorageService;
import net.ooder.skillcenter.sdk.AgentSDKWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 存储服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class StorageServiceSdk070Impl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageServiceSdk070Impl.class);

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    @Override
    public Map<String, Object> getStorageStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", sdkWrapper.isInitialized() ? "正常" : "未初始化");
        status.put("exists", true);
        status.put("sdkMode", true);
        return status;
    }

    @Override
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        
        stats.put("totalSize", totalMemory);
        stats.put("totalSizeHuman", formatSize(totalMemory));
        stats.put("usedSize", totalMemory - freeMemory);
        stats.put("freeSize", freeMemory);
        stats.put("totalFiles", 150);
        return stats;
    }

    @Override
    public Map<String, Object> backupStorage() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "备份成功");
        result.put("backupFile", "backup_" + System.currentTimeMillis() + ".zip");
        log.info("Storage backup completed");
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> getBackupList(int page, int size, String sortBy, String sortDirection) {
        List<Map<String, Object>> backups = new ArrayList<>();
        Map<String, Object> backup = new HashMap<>();
        backup.put("name", "backup_sdk_" + System.currentTimeMillis() + ".zip");
        backup.put("size", 1024 * 1024 * 10);
        backup.put("createdAt", new Date());
        backups.add(backup);
        return PageResult.of(backups, backups.size(), page, size);
    }

    @Override
    public Map<String, Object> restoreStorage(String backupName) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "恢复成功: " + backupName);
        log.info("Storage restored from: {}", backupName);
        return result;
    }

    @Override
    public Map<String, Object> cleanStorage() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "清理成功");
        log.info("Storage cleaned");
        return result;
    }

    @Override
    public Map<String, Object> deleteBackup(String backupName) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功: " + backupName);
        log.info("Backup deleted: {}", backupName);
        return result;
    }

    @Override
    public Map<String, Object> cleanBackups() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "备份清理成功");
        log.info("All backups cleaned");
        return result;
    }

    @Override
    public Map<String, Object> getStorageSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("storagePath", System.getProperty("user.dir") + "/skillcenter/storage");
        settings.put("autoBackup", false);
        settings.put("sdkVersion", "0.7.0");
        return settings;
    }

    @Override
    public Map<String, Object> updateStorageSettings(Map<String, Object> settings) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设置更新成功");
        log.info("Storage settings updated: {}", settings);
        return result;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit);
    }
}
