package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.service.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class StorageServiceMockImpl implements StorageService {

    @Override
    public Map<String, Object> getStorageStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "正常");
        status.put("exists", true);
        return status;
    }

    @Override
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSize", 1024 * 1024 * 100);
        stats.put("totalSizeHuman", "100 MB");
        stats.put("totalFiles", 150);
        return stats;
    }

    @Override
    public Map<String, Object> backupStorage() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "备份成功");
        result.put("backupFile", "backup_" + System.currentTimeMillis() + ".zip");
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> getBackupList(int page, int size, String sortBy, String sortDirection) {
        List<Map<String, Object>> backups = new ArrayList<>();
        Map<String, Object> backup = new HashMap<>();
        backup.put("name", "backup_1.zip");
        backup.put("size", 1024 * 1024 * 10);
        backups.add(backup);
        return PageResult.of(backups, backups.size(), page, size);
    }

    @Override
    public Map<String, Object> restoreStorage(String backupName) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "恢复成功");
        return result;
    }

    @Override
    public Map<String, Object> cleanStorage() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "清理成功");
        return result;
    }

    @Override
    public Map<String, Object> deleteBackup(String backupName) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    @Override
    public Map<String, Object> cleanBackups() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "备份清理成功");
        return result;
    }

    @Override
    public Map<String, Object> getStorageSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("storagePath", "/data/storage");
        settings.put("autoBackup", false);
        return settings;
    }

    @Override
    public Map<String, Object> updateStorageSettings(Map<String, Object> settings) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设置更新成功");
        return result;
    }
}
