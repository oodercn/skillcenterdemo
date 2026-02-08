package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.skillcenter.service.SkillService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能服务SDK真实实现
 * 通过调用ooderAgent-SDK 0.6.5的API获取真实数据
 */
@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "real")
public class SkillServiceSdkImpl implements SkillService {

    private final SdkConfig sdkConfig;
    private final RestTemplate restTemplate;

    public SkillServiceSdkImpl(SdkConfig sdkConfig) {
        this.sdkConfig = sdkConfig;
        this.restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return sdkConfig.getBaseUrl();
    }

    @Override
    public PageResult<SkillDTO> getAllSkills(String category, String status, String keyword, int pageNum, int pageSize) {
        String url = getBaseUrl() + "/api/skills?pageNum={pageNum}&pageSize={pageSize}";
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        
        if (category != null && !category.isEmpty()) {
            url += "&category={category}";
            params.put("category", category);
        }
        if (status != null && !status.isEmpty()) {
            url += "&status={status}";
            params.put("status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            url += "&keyword={keyword}";
            params.put("keyword", keyword);
        }

        ResponseEntity<PageResult<SkillDTO>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<PageResult<SkillDTO>>() {},
            params
        );
        return response.getBody();
    }

    @Override
    public SkillDTO getSkillById(String skillId) {
        String url = getBaseUrl() + "/api/skills/{skillId}";
        return restTemplate.getForObject(url, SkillDTO.class, skillId);
    }

    @Override
    public SkillDTO addSkill(SkillDTO skillDTO) {
        String url = getBaseUrl() + "/api/skills";
        return restTemplate.postForObject(url, skillDTO, SkillDTO.class);
    }

    @Override
    public SkillDTO updateSkill(String skillId, SkillDTO skillDTO) {
        String url = getBaseUrl() + "/api/skills/{skillId}";
        restTemplate.put(url, skillDTO, skillId);
        return skillDTO;
    }

    @Override
    public boolean deleteSkill(String skillId) {
        String url = getBaseUrl() + "/api/skills/{skillId}";
        restTemplate.delete(url, skillId);
        return true;
    }

    @Override
    public boolean approveSkill(String skillId) {
        String url = getBaseUrl() + "/api/skills/{skillId}/approve";
        restTemplate.postForObject(url, null, Boolean.class, skillId);
        return true;
    }

    @Override
    public boolean rejectSkill(String skillId) {
        String url = getBaseUrl() + "/api/skills/{skillId}/reject";
        restTemplate.postForObject(url, null, Boolean.class, skillId);
        return true;
    }

    @Override
    public PageResult<SkillDTO> getMarketSkills(String category, String status, String keyword, int pageNum, int pageSize) {
        String url = getBaseUrl() + "/api/market/skills?pageNum={pageNum}&pageSize={pageSize}";
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        
        if (category != null && !category.isEmpty()) {
            url += "&category={category}";
            params.put("category", category);
        }
        if (status != null && !status.isEmpty()) {
            url += "&status={status}";
            params.put("status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            url += "&keyword={keyword}";
            params.put("keyword", keyword);
        }

        ResponseEntity<PageResult<SkillDTO>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<PageResult<SkillDTO>>() {},
            params
        );
        return response.getBody();
    }

    @Override
    public SkillDTO addMarketSkill(SkillDTO skillDTO) {
        String url = getBaseUrl() + "/api/market/skills";
        return restTemplate.postForObject(url, skillDTO, SkillDTO.class);
    }

    @Override
    public SkillDTO updateMarketSkill(String skillId, SkillDTO skillDTO) {
        String url = getBaseUrl() + "/api/market/skills/{skillId}";
        restTemplate.put(url, skillDTO, skillId);
        return skillDTO;
    }

    @Override
    public boolean removeMarketSkill(String skillId) {
        String url = getBaseUrl() + "/api/market/skills/{skillId}";
        restTemplate.delete(url, skillId);
        return true;
    }
}
