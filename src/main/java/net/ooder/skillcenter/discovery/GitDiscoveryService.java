package net.ooder.skillcenter.discovery;

import net.ooder.sdk.api.skill.SkillPackage;
import net.ooder.sdk.discovery.git.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GitDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(GitDiscoveryService.class);

    @Value("${skill.discovery.github.enabled:false}")
    private boolean githubEnabled;

    @Value("${skill.discovery.github.api-url:https://api.github.com}")
    private String githubApiUrl;

    @Value("${skill.discovery.github.token:}")
    private String githubToken;

    @Value("${skill.discovery.github.default-owner:}")
    private String githubDefaultOwner;

    @Value("${skill.discovery.github.skills-path:skills}")
    private String githubSkillsPath;

    @Value("${skill.discovery.github.skills-repo:skills}")
    private String githubSkillsRepo;

    @Value("${skill.discovery.github.single-repo-mode:true}")
    private boolean githubSingleRepoMode;

    @Value("${skill.discovery.gitee.enabled:false}")
    private boolean giteeEnabled;

    @Value("${skill.discovery.gitee.api-url:https://gitee.com/api/v5}")
    private String giteeApiUrl;

    @Value("${skill.discovery.gitee.token:}")
    private String giteeToken;

    @Value("${skill.discovery.gitee.default-owner:}")
    private String giteeDefaultOwner;

    @Value("${skill.discovery.gitee.skills-path:skills}")
    private String giteeSkillsPath;

    @Value("${skill.discovery.gitee.skills-repo:skills}")
    private String giteeSkillsRepo;

    @Value("${skill.discovery.gitee.single-repo-mode:true}")
    private boolean giteeSingleRepoMode;

    @Value("${skill.discovery.cache-ttl:3600000}")
    private long cacheTtl;

    private final Map<String, GitRepositoryDiscoverer> discoverers = new ConcurrentHashMap<>();
    private final Map<String, List<SkillPackage>> skillCache = new ConcurrentHashMap<>();

    private static final String SOURCE_GITHUB = "github";
    private static final String SOURCE_GITEE = "gitee";

    @PostConstruct
    public void init() {
        if (githubEnabled) {
            GitDiscoveryConfig config = GitDiscoveryConfig.forGitHub();
            config.setApiBaseUrl(githubApiUrl);
            if (githubToken != null && !githubToken.isEmpty()) {
                config.setToken(githubToken);
            }
            if (githubDefaultOwner != null && !githubDefaultOwner.isEmpty()) {
                config.setDefaultOwner(githubDefaultOwner);
            }
            config.setSkillsPath(githubSkillsPath);
            config.setCacheTtlMs(cacheTtl);
            discoverers.put(SOURCE_GITHUB, new GitHubDiscoverer(config));
            logger.info("GitHub discovery enabled: {}/{}", 
                githubDefaultOwner, githubSkillsPath);
        }

        if (giteeEnabled) {
            GitDiscoveryConfig config = GitDiscoveryConfig.forGitee();
            config.setApiBaseUrl(giteeApiUrl);
            if (giteeToken != null && !giteeToken.isEmpty()) {
                config.setToken(giteeToken);
            }
            if (giteeDefaultOwner != null && !giteeDefaultOwner.isEmpty()) {
                config.setDefaultOwner(giteeDefaultOwner);
            }
            config.setSkillsPath(giteeSkillsPath);
            config.setCacheTtlMs(cacheTtl);
            discoverers.put(SOURCE_GITEE, new GiteeDiscoverer(config));
            logger.info("Gitee discovery enabled: {}/{}", 
                giteeDefaultOwner, giteeSkillsPath);
        }
        
        logger.info("GitDiscoveryService initialized. GitHub: {}, Gitee: {}", githubEnabled, giteeEnabled);
    }

    public CompletableFuture<List<SkillPackage>> discoverFromGitHub() {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITHUB);
        if (discoverer == null) {
            logger.warn("GitHub discovery is not enabled");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return discoverer.discoverSkills();
    }

    public CompletableFuture<List<SkillPackage>> discoverFromGitHub(String owner) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITHUB);
        if (discoverer == null) {
            logger.warn("GitHub discovery is not enabled");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return discoverer.discoverSkills(owner);
    }

    public CompletableFuture<List<SkillPackage>> discoverFromGitHub(String owner, String skillsPath) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITHUB);
        if (discoverer == null) {
            logger.warn("GitHub discovery is not enabled");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return discoverer.discoverSkills(owner, skillsPath);
    }

    public CompletableFuture<SkillPackage> discoverGitHubSkill(String owner, String repoName) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITHUB);
        if (discoverer == null) {
            return CompletableFuture.completedFuture(null);
        }
        return discoverer.discoverSkill(owner, repoName);
    }

    public CompletableFuture<List<SkillPackage>> discoverFromGitee() {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITEE);
        if (discoverer == null) {
            logger.warn("Gitee discovery is not enabled");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return discoverer.discoverSkills();
    }

    public CompletableFuture<List<SkillPackage>> discoverFromGitee(String owner) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITEE);
        if (discoverer == null) {
            logger.warn("Gitee discovery is not enabled");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return discoverer.discoverSkills(owner);
    }

    public CompletableFuture<List<SkillPackage>> discoverFromGitee(String owner, String skillsPath) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITEE);
        if (discoverer == null) {
            logger.warn("Gitee discovery is not enabled");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return discoverer.discoverSkills(owner, skillsPath);
    }

    public CompletableFuture<SkillPackage> discoverGiteeSkill(String owner, String repoName) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITEE);
        if (discoverer == null) {
            return CompletableFuture.completedFuture(null);
        }
        return discoverer.discoverSkill(owner, repoName);
    }

    public CompletableFuture<List<SkillPackage>> discoverFromAll() {
        List<CompletableFuture<List<SkillPackage>>> futures = new ArrayList<>();
        
        if (githubEnabled) {
            futures.add(discoverFromGitHub());
        }
        if (giteeEnabled) {
            futures.add(discoverFromGitee());
        }

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<SkillPackage> allPackages = new ArrayList<>();
                for (CompletableFuture<List<SkillPackage>> future : futures) {
                    try {
                        allPackages.addAll(future.get());
                    } catch (Exception e) {
                        logger.error("Failed to get discovery results", e);
                    }
                }
                return allPackages;
            });
    }

    public CompletableFuture<List<GitRepositoryDiscoverer.SkillDirectory>> listGitHubSkillDirectories(String owner) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITHUB);
        if (discoverer == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return discoverer.listSkillDirectories(owner);
    }

    public CompletableFuture<List<GitRepositoryDiscoverer.SkillDirectory>> listGiteeSkillDirectories(String owner) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITEE);
        if (discoverer == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return discoverer.listSkillDirectories(owner);
    }

    public CompletableFuture<GitRepositoryDiscoverer.ReleaseInfo> getGitHubRelease(String owner, String repoName) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITHUB);
        if (discoverer == null) {
            return CompletableFuture.completedFuture(null);
        }
        return discoverer.getLatestRelease(owner, repoName);
    }

    public CompletableFuture<GitRepositoryDiscoverer.ReleaseInfo> getGiteeRelease(String owner, String repoName) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITEE);
        if (discoverer == null) {
            return CompletableFuture.completedFuture(null);
        }
        return discoverer.getLatestRelease(owner, repoName);
    }

    public CompletableFuture<String> getGitHubManifestContent(String owner, String repoName) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITHUB);
        if (discoverer == null) {
            return CompletableFuture.completedFuture(null);
        }
        return discoverer.getSkillManifestContent(owner, repoName);
    }

    public CompletableFuture<String> getGiteeManifestContent(String owner, String repoName) {
        GitRepositoryDiscoverer discoverer = discoverers.get(SOURCE_GITEE);
        if (discoverer == null) {
            return CompletableFuture.completedFuture(null);
        }
        return discoverer.getSkillManifestContent(owner, repoName);
    }

    public boolean isGitHubEnabled() {
        return githubEnabled;
    }

    public boolean isGiteeEnabled() {
        return giteeEnabled;
    }

    public GitRepositoryDiscoverer getDiscoverer(String source) {
        return discoverers.get(source);
    }
}
