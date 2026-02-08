package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.SkillResultDTO;
import net.ooder.skillcenter.service.ExecutionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class ExecutionServiceMockImpl implements ExecutionService {

    private final Map<String, SkillResultDTO> executionResults = new ConcurrentHashMap<>();

    @Override
    public SkillResultDTO executeSkill(String skillId, Map<String, Object> parameters) {
        String executionId = UUID.randomUUID().toString();
        SkillResultDTO result = new SkillResultDTO();
        result.setExecutionId(executionId);
        result.setSkillId(skillId);
        result.setStatus("success");
        result.setOutput("执行成功: " + parameters);
        result.setExecutedAt(new Date());
        result.setExecutionTime(150);
        executionResults.put(executionId, result);
        return result;
    }

    @Override
    public String executeSkillAsync(String skillId, Map<String, Object> parameters) {
        String executionId = UUID.randomUUID().toString();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                SkillResultDTO result = new SkillResultDTO();
                result.setExecutionId(executionId);
                result.setSkillId(skillId);
                result.setStatus("success");
                result.setOutput("异步执行成功: " + parameters);
                result.setExecutedAt(new Date());
                result.setExecutionTime(1000);
                executionResults.put(executionId, result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
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
            .filter(r -> "success".equals(r.getStatus())).count());
        return stats;
    }
}
