package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.skill.SkillManager;
import net.ooder.sdk.skill.SkillResult;
import net.ooder.skillcenter.dto.SkillResultDTO;
import net.ooder.skillcenter.sdk.AgentSDKWrapper;
import net.ooder.skillcenter.service.ExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 技能执行服务SDK 0.7.0实现
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class ExecutionServiceSdk070Impl implements ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionServiceSdk070Impl.class);

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    private final Map<String, SkillResultDTO> executionResults = new ConcurrentHashMap<>();

    @Override
    public SkillResultDTO executeSkill(String skillId, Map<String, Object> parameters) {
        String executionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        try {
            SkillManager skillManager = sdkWrapper.getSkillManager();
            if (skillManager == null) {
                return createErrorResult(executionId, skillId, "SDK not initialized");
            }

            SkillResult sdkResult = skillManager.executeSkill(skillId, parameters);
            
            SkillResultDTO result = convertToDTO(executionId, skillId, sdkResult, 
                    System.currentTimeMillis() - startTime);
            executionResults.put(executionId, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to execute skill {}: {}", skillId, e.getMessage(), e);
            return createErrorResult(executionId, skillId, e.getMessage());
        }
    }

    @Override
    public String executeSkillAsync(String skillId, Map<String, Object> parameters) {
        String executionId = UUID.randomUUID().toString();
        
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                SkillManager skillManager = sdkWrapper.getSkillManager();
                if (skillManager == null) {
                    executionResults.put(executionId, 
                            createErrorResult(executionId, skillId, "SDK not initialized"));
                    return;
                }

                SkillResult sdkResult = skillManager.executeSkill(skillId, parameters);
                
                SkillResultDTO result = convertToDTO(executionId, skillId, sdkResult,
                        System.currentTimeMillis() - startTime);
                executionResults.put(executionId, result);
            } catch (Exception e) {
                log.error("Async execution failed for skill {}: {}", skillId, e.getMessage(), e);
                executionResults.put(executionId, createErrorResult(executionId, skillId, e.getMessage()));
            }
        });
        
        return executionId;
    }

    @Override
    public SkillResultDTO getExecutionResult(String executionId) {
        return executionResults.get(executionId);
    }

    @Override
    public String getExecutionStatus(String executionId) {
        SkillResultDTO result = executionResults.get(executionId);
        if (result == null) return "PENDING";
        return result.getStatus().toUpperCase();
    }

    @Override
    public boolean clearExecutionResult(String executionId) {
        return executionResults.remove(executionId) != null;
    }

    @Override
    public Map<String, Object> getExecutionStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalExecutions", executionResults.size());
        stats.put("successfulExecutions", executionResults.values().stream()
            .filter(r -> "success".equalsIgnoreCase(r.getStatus())).count());
        stats.put("failedExecutions", executionResults.values().stream()
            .filter(r -> "error".equalsIgnoreCase(r.getStatus())).count());
        return stats;
    }

    private SkillResultDTO convertToDTO(String executionId, String skillId, SkillResult sdkResult, long executionTime) {
        SkillResultDTO dto = new SkillResultDTO();
        dto.setExecutionId(executionId);
        dto.setSkillId(skillId);
        dto.setStatus(sdkResult.isSuccess() ? "success" : "error");
        dto.setOutput(sdkResult.getErrorMessage() != null ? sdkResult.getErrorMessage() : "");
        dto.setExecutedAt(new Date());
        dto.setExecutionTime(executionTime);
        
        if (sdkResult.getData() != null) {
            dto.setOutput(sdkResult.getData().toString());
        }
        
        return dto;
    }

    private SkillResultDTO createErrorResult(String executionId, String skillId, String errorMessage) {
        SkillResultDTO result = new SkillResultDTO();
        result.setExecutionId(executionId);
        result.setSkillId(skillId);
        result.setStatus("error");
        result.setOutput(errorMessage);
        result.setExecutedAt(new Date());
        result.setExecutionTime(0);
        return result;
    }
}
