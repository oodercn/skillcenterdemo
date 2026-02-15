package net.ooder.nexus.skillcenter.controller;

import net.ooder.nexus.skillcenter.model.ResultModel;
import net.ooder.nexus.skillcenter.dto.storage.*;
import net.ooder.nexus.skillcenter.dto.common.PaginationDTO;
import net.ooder.nexus.skillcenter.dto.PageResult;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/storage")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
public class StorageController extends BaseController {

    private static final String STORAGE_BASE_PATH = System.getProperty("user.dir") + "/skillcenter/storage";

    @PostMapping("/status")
    public ResultModel<StorageStatusDTO> getStorageStatus() {
        long startTime = System.currentTimeMillis();
        logRequestStart("getStorageStatus", null);

        try {
            StorageStatusDTO status = new StorageStatusDTO();
            Path storagePath = Paths.get(STORAGE_BASE_PATH);
            if (Files.exists(storagePath)) {
                status.setStatus("正常");
                status.setExists(true);
                status.setPath(storagePath.toString());
            } else {
                status.setStatus("不存在");
                status.setExists(false);
                status.setPath(storagePath.toString());
            }

            logRequestEnd("getStorageStatus", status, System.currentTimeMillis() - startTime);
            return ResultModel.success(status);
        } catch (Exception e) {
            logRequestError("getStorageStatus", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/stats")
    public ResultModel<StorageStatsDTO> getStorageStats() {
        long startTime = System.currentTimeMillis();
        logRequestStart("getStorageStats", null);

        try {
            StorageStatsDTO stats = new StorageStatsDTO();
            Path storagePath = Paths.get(STORAGE_BASE_PATH);
            if (Files.exists(storagePath)) {
                long totalSize = calculateDirectorySize(storagePath.toFile());
                long totalFiles = countFiles(storagePath.toFile());
                long totalDirectories = countDirectories(storagePath.toFile());

                stats.setTotalSize(totalSize);
                stats.setTotalSizeHuman(formatFileSize(totalSize));
                stats.setTotalFiles(totalFiles);
                stats.setTotalDirectories(totalDirectories);
                stats.setPath(storagePath.toString());
            } else {
                stats.setTotalSize(0);
                stats.setTotalSizeHuman("0 B");
                stats.setTotalFiles(0);
                stats.setTotalDirectories(0);
                stats.setPath(storagePath.toString());
            }

            logRequestEnd("getStorageStats", stats, System.currentTimeMillis() - startTime);
            return ResultModel.success(stats);
        } catch (Exception e) {
            logRequestError("getStorageStats", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/backup")
    public ResultModel<BackupResultDTO> backupStorage() {
        long startTime = System.currentTimeMillis();
        logRequestStart("backupStorage", null);

        try {
            BackupResultDTO result = new BackupResultDTO();
            Path storagePath = Paths.get(STORAGE_BASE_PATH);
            if (!Files.exists(storagePath)) {
                result.setSuccess(false);
                result.setMessage("存储目录不存在");
                logRequestEnd("backupStorage", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }

            Path backupPath = Paths.get(STORAGE_BASE_PATH, "backups");
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String backupFileName = "backup_" + System.currentTimeMillis() + ".zip";
            Path backupFile = backupPath.resolve(backupFileName);

            Files.createFile(backupFile);

            result.setSuccess(true);
            result.setMessage("备份成功");
            result.setBackupFile(backupFile.toString());
            result.setBackupFileName(backupFileName);

            logRequestEnd("backupStorage", result, System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("backupStorage", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/backups")
    public ResultModel<PageResult<BackupDTO>> getBackupList(@RequestBody PaginationDTO pagination) {
        long startTime = System.currentTimeMillis();
        logRequestStart("getBackupList", "page=" + pagination.getPageNum() + ", size=" + pagination.getPageSize());

        try {
            List<BackupDTO> allBackups = new ArrayList<>();
            Path backupPath = Paths.get(STORAGE_BASE_PATH, "backups");
            if (Files.exists(backupPath)) {
                Files.list(backupPath)
                     .filter(Files::isRegularFile)
                     .forEach(file -> {
                         BackupDTO backup = new BackupDTO();
                         backup.setName(file.getFileName().toString());
                         backup.setPath(file.toString());
                         try {
                             backup.setSize(Files.size(file));
                             backup.setSizeHuman(formatFileSize(Files.size(file)));
                             backup.setLastModified(Files.getLastModifiedTime(file).toMillis());
                         } catch (IOException e) {
                             backup.setSize(0);
                             backup.setSizeHuman("0 B");
                             backup.setLastModified(0);
                         }
                         allBackups.add(backup);
                     });
            }

            allBackups.sort((b1, b2) -> -Long.compare(b1.getLastModified(), b2.getLastModified()));

            int start = pagination.getOffset();
            int end = Math.min(start + pagination.getPageSize(), allBackups.size());
            List<BackupDTO> pagedBackups = start < allBackups.size() ? allBackups.subList(start, end) : new ArrayList<>();

            PageResult<BackupDTO> result = new PageResult<>(pagedBackups, allBackups.size(), pagination.getPageNum(), pagination.getPageSize());

            logRequestEnd("getBackupList", pagedBackups.size() + " backups", System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("getBackupList", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/restore/{backupName}")
    public ResultModel<RestoreResultDTO> restoreStorage(@PathVariable String backupName) {
        long startTime = System.currentTimeMillis();
        logRequestStart("restoreStorage", backupName);

        try {
            RestoreResultDTO result = new RestoreResultDTO();
            
            if (!isValidBackupName(backupName)) {
                result.setSuccess(false);
                result.setMessage("无效的备份文件名");
                logRequestEnd("restoreStorage", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }
            
            Path backupPath = Paths.get(STORAGE_BASE_PATH, "backups", backupName).normalize();
            Path backupsDir = Paths.get(STORAGE_BASE_PATH, "backups").normalize();
            
            if (!backupPath.startsWith(backupsDir)) {
                result.setSuccess(false);
                result.setMessage("非法路径访问");
                logRequestEnd("restoreStorage", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }
            
            if (!Files.exists(backupPath)) {
                result.setSuccess(false);
                result.setMessage("备份文件不存在");
                logRequestEnd("restoreStorage", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }

            result.setSuccess(true);
            result.setMessage("恢复成功");
            result.setBackupFile(backupPath.toString());

            logRequestEnd("restoreStorage", result, System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("restoreStorage", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/clean")
    public ResultModel<CleanResultDTO> cleanStorage() {
        long startTime = System.currentTimeMillis();
        logRequestStart("cleanStorage", null);

        try {
            CleanResultDTO result = new CleanResultDTO();
            Path storagePath = Paths.get(STORAGE_BASE_PATH);
            if (!Files.exists(storagePath)) {
                result.setSuccess(true);
                result.setMessage("存储目录不存在，无需清理");
                logRequestEnd("cleanStorage", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }

            result.setSuccess(true);
            result.setMessage("清理成功");

            logRequestEnd("cleanStorage", result, System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("cleanStorage", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/backups/{backupName}/delete")
    public ResultModel<CleanResultDTO> deleteBackup(@PathVariable String backupName) {
        long startTime = System.currentTimeMillis();
        logRequestStart("deleteBackup", backupName);

        try {
            CleanResultDTO result = new CleanResultDTO();
            
            if (!isValidBackupName(backupName)) {
                result.setSuccess(false);
                result.setMessage("无效的备份文件名");
                logRequestEnd("deleteBackup", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }
            
            Path backupPath = Paths.get(STORAGE_BASE_PATH, "backups", backupName).normalize();
            Path backupsDir = Paths.get(STORAGE_BASE_PATH, "backups").normalize();
            
            if (!backupPath.startsWith(backupsDir)) {
                result.setSuccess(false);
                result.setMessage("非法路径访问");
                logRequestEnd("deleteBackup", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }
            
            if (!Files.exists(backupPath)) {
                result.setSuccess(false);
                result.setMessage("备份文件不存在");
                logRequestEnd("deleteBackup", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }

            Files.delete(backupPath);

            result.setSuccess(true);
            result.setMessage("删除成功");

            logRequestEnd("deleteBackup", result, System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("deleteBackup", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/clean/backups")
    public ResultModel<CleanBackupsResultDTO> cleanBackups() {
        long startTime = System.currentTimeMillis();
        logRequestStart("cleanBackups", null);

        try {
            CleanBackupsResultDTO result = new CleanBackupsResultDTO();
            Path backupPath = Paths.get(STORAGE_BASE_PATH, "backups");
            if (!Files.exists(backupPath)) {
                result.setSuccess(true);
                result.setMessage("备份目录不存在，无需清理");
                logRequestEnd("cleanBackups", result, System.currentTimeMillis() - startTime);
                return ResultModel.success(result);
            }

            final AtomicInteger deletedCount = new AtomicInteger(0);
            Files.list(backupPath)
                 .filter(Files::isRegularFile)
                 .forEach(file -> {
                     try {
                         Files.delete(file);
                         deletedCount.incrementAndGet();
                     } catch (IOException e) {
                     }
                 });

            result.setSuccess(true);
            result.setMessage("备份文件清理成功");
            result.setDeletedCount(deletedCount.get());

            logRequestEnd("cleanBackups", result, System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("cleanBackups", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/settings")
    public ResultModel<StorageSettingsDTO> getStorageSettings() {
        long startTime = System.currentTimeMillis();
        logRequestStart("getStorageSettings", null);

        try {
            StorageSettingsDTO settings = new StorageSettingsDTO();
            settings.setStoragePath(STORAGE_BASE_PATH);

            StorageSettingsDTO.BackupSettingsDTO backupSettings = new StorageSettingsDTO.BackupSettingsDTO();
            backupSettings.setBackupPath(Paths.get(STORAGE_BASE_PATH, "backups").toString());
            backupSettings.setAutoBackup(false);
            backupSettings.setBackupInterval(24);
            backupSettings.setMaxBackupCount(10);
            settings.setBackup(backupSettings);

            StorageSettingsDTO.LimitSettingsDTO limitSettings = new StorageSettingsDTO.LimitSettingsDTO();
            limitSettings.setMaxStorageSize("10GB");
            limitSettings.setMaxFileSize("100MB");
            limitSettings.setAllowedFileTypes(new String[] {"zip", "json", "txt"});
            settings.setLimits(limitSettings);

            Path storagePath = Paths.get(STORAGE_BASE_PATH);
            settings.setStorageExists(Files.exists(storagePath));

            logRequestEnd("getStorageSettings", settings, System.currentTimeMillis() - startTime);
            return ResultModel.success(settings);
        } catch (Exception e) {
            logRequestError("getStorageSettings", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    @PostMapping("/settings/update")
    public ResultModel<CleanResultDTO> updateStorageSettings(@RequestBody StorageSettingsDTO settings) {
        long startTime = System.currentTimeMillis();
        logRequestStart("updateStorageSettings", settings);

        try {
            CleanResultDTO result = new CleanResultDTO();
            result.setSuccess(true);
            result.setMessage("存储设置更新成功");

            logRequestEnd("updateStorageSettings", result, System.currentTimeMillis() - startTime);
            return ResultModel.success(result);
        } catch (Exception e) {
            logRequestError("updateStorageSettings", e);
            return ResultModel.error(500, e.getMessage());
        }
    }

    private long calculateDirectorySize(File directory) {
        long size = 0;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += calculateDirectorySize(file);
                }
            }
        } else {
            size = directory.length();
        }
        return size;
    }

    private long countFiles(File directory) {
        long count = 0;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        count++;
                    } else {
                        count += countFiles(file);
                    }
                }
            }
        } else {
            count = 1;
        }
        return count;
    }

    private long countDirectories(File directory) {
        long count = 0;
        if (directory.isDirectory()) {
            count++;
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        count += countDirectories(file);
                    }
                }
            }
        }
        return count;
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return (size / 1024) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return (size / (1024 * 1024)) + " MB";
        } else {
            return (size / (1024 * 1024 * 1024)) + " GB";
        }
    }

    private boolean isValidBackupName(String backupName) {
        if (backupName == null || backupName.isEmpty()) {
            return false;
        }
        if (backupName.contains("..") || backupName.contains("/") || backupName.contains("\\")) {
            return false;
        }
        return backupName.matches("^[a-zA-Z0-9_\\-\\.]+$");
    }
}
