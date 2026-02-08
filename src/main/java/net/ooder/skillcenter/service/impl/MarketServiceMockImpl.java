package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.SkillDTO;
import net.ooder.skillcenter.dto.SkillReviewDTO;
import net.ooder.skillcenter.service.MarketService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class MarketServiceMockImpl implements MarketService {

    private final Map<String, SkillDTO> skillStore = new ConcurrentHashMap<>();
    private final Map<String, List<SkillReviewDTO>> reviewStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        initMockData();
    }

    private void initMockData() {
        addMockSkill("market-skill-1", "智能客服", "AI智能客服技能", "text-processing", 4.5, 850);
        addMockSkill("market-skill-2", "代码审查", "自动代码审查技能", "development", 4.8, 720);
        addMockSkill("market-skill-3", "图片处理", "图片编辑处理技能", "media", 4.2, 580);
        addMockSkill("market-skill-4", "数据分析", "数据分析可视化技能", "storage", 4.6, 420);

        reviewStore.put("market-skill-1", createMockReviews("market-skill-1"));
    }

    private void addMockSkill(String id, String name, String description, String category, double rating, int downloads) {
        SkillDTO skill = new SkillDTO();
        skill.setId(id);
        skill.setName(name);
        skill.setDescription(description);
        skill.setCategory(category);
        skill.setRating(rating);
        skill.setDownloadCount(downloads);
        skill.setStatus("active");
        skill.setCreatedAt(new Date());
        skill.setUpdatedAt(new Date());
        skillStore.put(id, skill);
    }

    private List<SkillReviewDTO> createMockReviews(String skillId) {
        List<SkillReviewDTO> reviews = new ArrayList<>();
        SkillReviewDTO review1 = new SkillReviewDTO();
        review1.setId("review-1");
        review1.setSkillId(skillId);
        review1.setUserId("user-1");
        review1.setUsername("用户1");
        review1.setRating(5.0);
        review1.setComment("非常好用！");
        review1.setCreatedAt(new Date());
        reviews.add(review1);
        return reviews;
    }

    @Override
    public PageResult<SkillDTO> getMarketSkills(int page, int size, String sortBy, String sortDirection) {
        List<SkillDTO> skills = new ArrayList<>(skillStore.values());
        skills = sortSkills(skills, sortBy, sortDirection);
        return paginate(skills, page, size);
    }

    @Override
    public SkillDTO getSkillDetails(String skillId) {
        return skillStore.get(skillId);
    }

    @Override
    public PageResult<SkillDTO> searchSkills(String keyword, int page, int size, String sortBy, String sortDirection) {
        List<SkillDTO> filtered = skillStore.values().stream()
            .filter(s -> s.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        s.getDescription().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        filtered = sortSkills(filtered, sortBy, sortDirection);
        return paginate(filtered, page, size);
    }

    @Override
    public List<String> getSkillCategories() {
        return skillStore.values().stream()
            .map(SkillDTO::getCategory)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public PageResult<SkillDTO> getSkillsByCategory(String category, int page, int size, String sortBy, String sortDirection) {
        List<SkillDTO> filtered = skillStore.values().stream()
            .filter(s -> category.equals(s.getCategory()))
            .collect(Collectors.toList());
        filtered = sortSkills(filtered, sortBy, sortDirection);
        return paginate(filtered, page, size);
    }

    @Override
    public List<SkillDTO> getPopularSkills(int limit) {
        return skillStore.values().stream()
            .sorted(Comparator.comparing(SkillDTO::getDownloadCount).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public List<SkillDTO> getLatestSkills(int limit) {
        return skillStore.values().stream()
            .sorted(Comparator.comparing(SkillDTO::getCreatedAt).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public boolean rateSkill(String skillId, double rating, String comment, String userId) {
        SkillReviewDTO review = new SkillReviewDTO();
        review.setId("review-" + idGenerator.getAndIncrement());
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
        return "skill-data".getBytes();
    }

    @Override
    public boolean publishSkill(SkillDTO skill) {
        String id = "market-skill-" + idGenerator.getAndIncrement();
        skill.setId(id);
        skill.setCreatedAt(new Date());
        skillStore.put(id, skill);
        return true;
    }

    @Override
    public boolean updateSkill(String skillId, SkillDTO skill) {
        skill.setId(skillId);
        skill.setUpdatedAt(new Date());
        skillStore.put(skillId, skill);
        return true;
    }

    @Override
    public boolean deleteSkill(String skillId) {
        return skillStore.remove(skillId) != null;
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
