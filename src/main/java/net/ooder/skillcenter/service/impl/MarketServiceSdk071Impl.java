package net.ooder.skillcenter.service.impl;

import net.ooder.sdk.api.skill.*;
import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.skillcenter.dto.SkillReviewDTO;
import net.ooder.skillcenter.service.MarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "sdk")
public class MarketServiceSdk071Impl implements MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketServiceSdk071Impl.class);

    private final SdkConfig sdkConfig;
    private SkillDiscoverer skillDiscoverer;
    private SkillRegistry skillRegistry;
    private final Map<String, List<SkillReviewDTO>> reviewStore = new ConcurrentHashMap<>();
    private final AtomicLong reviewIdGenerator = new AtomicLong(1);

    public MarketServiceSdk071Impl(SdkConfig sdkConfig) {
        this.sdkConfig = sdkConfig;
    }

    @PostConstruct
    public void init() {
        log.info("MarketService initialized with SDK 0.7.1");
    }

    @Override
    public PageResult<SkillDTO> getMarketSkills(int page, int size, String sortBy, String sortDirection) {
        List<SkillDTO> skills = createMockSkills();
        skills = sortSkills(skills, sortBy, sortDirection);
        return paginate(skills, page, size);
    }

    @Override
    public SkillDTO getSkillDetails(String skillId) {
        List<SkillDTO> skills = createMockSkills();
        return skills.stream()
            .filter(s -> skillId.equals(s.getId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public PageResult<SkillDTO> searchSkills(String keyword, int page, int size, String sortBy, String sortDirection) {
        List<SkillDTO> skills = createMockSkills();
        if (keyword != null && !keyword.isEmpty()) {
            skills = skills.stream()
                .filter(s -> s.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    s.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
        }
        skills = sortSkills(skills, sortBy, sortDirection);
        return paginate(skills, page, size);
    }

    @Override
    public List<String> getSkillCategories() {
        return Arrays.asList("enterprise", "development", "integration", "infrastructure", "utilities");
    }

    @Override
    public PageResult<SkillDTO> getSkillsByCategory(String category, int page, int size, String sortBy, String sortDirection) {
        List<SkillDTO> skills = createMockSkills().stream()
            .filter(s -> category.equals(s.getCategory()))
            .collect(Collectors.toList());
        skills = sortSkills(skills, sortBy, sortDirection);
        return paginate(skills, page, size);
    }

    @Override
    public List<SkillDTO> getPopularSkills(int limit) {
        return createMockSkills().stream()
            .sorted((a, b) -> Integer.compare(b.getDownloadCount(), a.getDownloadCount()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public List<SkillDTO> getLatestSkills(int limit) {
        return createMockSkills().stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public boolean rateSkill(String skillId, double rating, String comment, String userId) {
        SkillReviewDTO review = new SkillReviewDTO();
        review.setId("review-" + reviewIdGenerator.getAndIncrement());
        review.setSkillId(skillId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(new Date());
        reviewStore.computeIfAbsent(skillId, k -> new ArrayList<>()).add(review);
        return true;
    }

    @Override
    public PageResult<SkillReviewDTO> getSkillReviews(String skillId, int page, int size, String sortBy, String sortDirection) {
        List<SkillReviewDTO> reviews = reviewStore.getOrDefault(skillId, new ArrayList<>());
        return paginate(reviews, page, size);
    }

    @Override
    public byte[] downloadSkill(String skillId) {
        log.info("Download skill: {}", skillId);
        return ("Mock skill data for " + skillId).getBytes();
    }

    @Override
    public boolean publishSkill(SkillDTO skill) {
        log.info("Publish skill: {}", skill.getName());
        return true;
    }

    @Override
    public boolean updateSkill(String skillId, SkillDTO skill) {
        log.info("Update skill: {}", skillId);
        return true;
    }

    @Override
    public boolean deleteSkill(String skillId) {
        log.info("Delete skill: {}", skillId);
        return true;
    }

    private List<SkillDTO> createMockSkills() {
        List<SkillDTO> skills = new ArrayList<>();
        
        SkillDTO skill1 = new SkillDTO();
        skill1.setId("skill-001");
        skill1.setName("Data Processing Skill");
        skill1.setVersion("1.0.0");
        skill1.setType("tool-skill");
        skill1.setDescription("A skill for processing data");
        skill1.setAuthor("Ooder Team");
        skill1.setCategory("development");
        skill1.setDownloadCount(150);
        skill1.setRating(4.5);
        skill1.setStatus("active");
        skill1.setAvailable(true);
        skills.add(skill1);
        
        SkillDTO skill2 = new SkillDTO();
        skill2.setId("skill-002");
        skill2.setName("API Integration Skill");
        skill2.setVersion("2.1.0");
        skill2.setType("integration-skill");
        skill2.setDescription("A skill for API integration");
        skill2.setAuthor("Ooder Team");
        skill2.setCategory("integration");
        skill2.setDownloadCount(280);
        skill2.setRating(4.8);
        skill2.setStatus("active");
        skill2.setAvailable(true);
        skills.add(skill2);
        
        return skills;
    }

    private List<SkillDTO> sortSkills(List<SkillDTO> skills, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) return skills;
        Comparator<SkillDTO> comparator;
        switch (sortBy) {
            case "name":
                comparator = Comparator.comparing(SkillDTO::getName);
                break;
            case "rating":
                comparator = Comparator.comparing(SkillDTO::getRating);
                break;
            case "downloadCount":
                comparator = Comparator.comparing(SkillDTO::getDownloadCount);
                break;
            default:
                comparator = Comparator.comparing(SkillDTO::getId);
        }
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        return skills.stream().sorted(comparator).collect(Collectors.toList());
    }

    private <T> PageResult<T> paginate(List<T> list, int page, int size) {
        int total = list.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        if (start >= total) return PageResult.empty();
        return PageResult.of(list.subList(start, end), total, page, size);
    }
}
