package net.ooder.skillcenter.sdk.protocol.impl;

import net.ooder.nexus.skillcenter.dto.protocol.LoginDTO;
import net.ooder.nexus.skillcenter.dto.protocol.LoginDTO.AutoLoginConfigDTO;
import net.ooder.nexus.skillcenter.dto.protocol.LoginDTO.CredentialDTO;
import net.ooder.nexus.skillcenter.dto.protocol.LoginDTO.DomainDTO;
import net.ooder.nexus.skillcenter.dto.protocol.LoginDTO.DomainPolicyDTO;
import net.ooder.nexus.skillcenter.dto.protocol.LoginDTO.LoginRequestDTO;
import net.ooder.nexus.skillcenter.dto.protocol.LoginDTO.LoginResultDTO;
import net.ooder.nexus.skillcenter.dto.protocol.LoginDTO.SessionDTO;
import net.ooder.skillcenter.sdk.protocol.LoginProtocolAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class LoginProtocolAdapterMockImpl implements LoginProtocolAdapter {

    private static final Logger log = LoggerFactory.getLogger(LoginProtocolAdapterMockImpl.class);

    private final List<LoginEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, SessionDTO> sessions = new ConcurrentHashMap<>();
    private final Map<String, CredentialDTO> credentials = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<LoginResultDTO> login(LoginRequestDTO request) {
        log.debug("[LoginMock] Login request for user: {}", request.getUsername());
        return CompletableFuture.supplyAsync(() -> {
            LoginResultDTO result = new LoginResultDTO();
            
            if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
                result.setSuccess(true);
                result.setSessionId("session-" + UUID.randomUUID().toString());
                result.setUserId("user-001");
                result.setUserName(request.getUsername());
                result.setToken("token-" + UUID.randomUUID().toString());
                result.setExpiresAt(System.currentTimeMillis() + 3600000L);
                result.setDomains(createMockDomains());
                result.setPolicy(createMockPolicy());
                
                SessionDTO session = new SessionDTO();
                session.setSessionId(result.getSessionId());
                session.setUserId(result.getUserId());
                session.setUserName(result.getUserName());
                session.setDomainId("domain-001");
                session.setCreatedAt(System.currentTimeMillis());
                session.setExpiresAt(result.getExpiresAt());
                session.setLastActiveAt(System.currentTimeMillis());
                session.setStatus("ACTIVE");
                sessions.put(session.getSessionId(), session);
            } else {
                result.setSuccess(false);
                result.setErrorCode("AUTH_FAILED");
                result.setErrorMessage("Invalid username or password");
            }
            
            return result;
        });
    }

    @Override
    public CompletableFuture<Void> logout(String sessionId) {
        log.debug("[LoginMock] Logout session: {}", sessionId);
        return CompletableFuture.runAsync(() -> {
            sessions.remove(sessionId);
        });
    }

    @Override
    public CompletableFuture<LoginResultDTO> autoLogin(AutoLoginConfigDTO config) {
        log.debug("[LoginMock] Auto login for user: {}", config.getUserId());
        return CompletableFuture.supplyAsync(() -> {
            LoginResultDTO result = new LoginResultDTO();
            CredentialDTO cred = credentials.get(config.getUserId());
            
            if (cred != null && cred.getCredential().equals(config.getCredential())) {
                result.setSuccess(true);
                result.setSessionId("session-" + UUID.randomUUID().toString());
                result.setUserId(config.getUserId());
                result.setUserName("auto-user");
                result.setToken("token-" + UUID.randomUUID().toString());
                result.setExpiresAt(System.currentTimeMillis() + 3600000L);
                result.setDomains(createMockDomains());
                result.setPolicy(createMockPolicy());
            } else {
                result.setSuccess(false);
                result.setErrorCode("AUTO_LOGIN_FAILED");
                result.setErrorMessage("Auto login credential invalid");
            }
            
            return result;
        });
    }

    @Override
    public CompletableFuture<SessionDTO> getSession(String sessionId) {
        log.debug("[LoginMock] Get session: {}", sessionId);
        return CompletableFuture.supplyAsync(() -> sessions.get(sessionId));
    }

    @Override
    public CompletableFuture<SessionDTO> validateSession(String sessionId) {
        log.debug("[LoginMock] Validate session: {}", sessionId);
        return CompletableFuture.supplyAsync(() -> {
            SessionDTO session = sessions.get(sessionId);
            if (session != null && session.getExpiresAt() > System.currentTimeMillis()) {
                return session;
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> refreshSession(String sessionId) {
        log.debug("[LoginMock] Refresh session: {}", sessionId);
        return CompletableFuture.runAsync(() -> {
            SessionDTO session = sessions.get(sessionId);
            if (session != null) {
                session.setExpiresAt(System.currentTimeMillis() + 3600000L);
                session.setLastActiveAt(System.currentTimeMillis());
            }
        });
    }

    @Override
    public CompletableFuture<DomainPolicyDTO> getDomainPolicy(String userId) {
        log.debug("[LoginMock] Get domain policy for user: {}", userId);
        return CompletableFuture.supplyAsync(this::createMockPolicy);
    }

    @Override
    public CompletableFuture<Void> saveCredential(CredentialDTO credential) {
        log.debug("[LoginMock] Save credential for user: {}", credential.getUserId());
        return CompletableFuture.runAsync(() -> {
            credentials.put(credential.getUserId(), credential);
        });
    }

    @Override
    public CompletableFuture<CredentialDTO> loadCredential(String userId) {
        log.debug("[LoginMock] Load credential for user: {}", userId);
        return CompletableFuture.supplyAsync(() -> credentials.get(userId));
    }

    @Override
    public CompletableFuture<Void> clearCredential(String userId) {
        log.debug("[LoginMock] Clear credential for user: {}", userId);
        return CompletableFuture.runAsync(() -> {
            credentials.remove(userId);
        });
    }

    @Override
    public void addLoginListener(LoginEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeLoginListener(LoginEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private List<DomainDTO> createMockDomains() {
        List<DomainDTO> domains = new ArrayList<>();
        DomainDTO domain = new DomainDTO();
        domain.setDomainId("domain-001");
        domain.setDomainName("Default Domain");
        domain.setDomainType("ORGANIZATION");
        domain.setRole("MEMBER");
        domains.add(domain);
        return domains;
    }

    private DomainPolicyDTO createMockPolicy() {
        DomainPolicyDTO policy = new DomainPolicyDTO();
        policy.setDomainId("domain-001");
        policy.setAllowedSkills(Arrays.asList("skill-001", "skill-002", "skill-003"));
        policy.setRequiredSkills(Arrays.asList("skill-001"));
        
        Map<String, Object> storageConfig = new HashMap<>();
        storageConfig.put("maxSize", "10GB");
        storageConfig.put("retention", "30d");
        policy.setStorageConfig(storageConfig);
        
        Map<String, Object> securityConfig = new HashMap<>();
        securityConfig.put("encryption", "AES256");
        securityConfig.put("authRequired", true);
        policy.setSecurityConfig(securityConfig);
        
        Map<String, Object> networkConfig = new HashMap<>();
        networkConfig.put("maxConnections", 100);
        networkConfig.put("timeout", 30000);
        policy.setNetworkConfig(networkConfig);
        
        return policy;
    }
}
