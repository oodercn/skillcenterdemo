package net.ooder.skillcenter.sdk.protocol.impl;

import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.AddMemberRequestDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.CreateDomainRequestDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.DomainInfoDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.DomainInvitationDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.DomainMemberDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.DomainPolicyConfigDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.DomainQueryDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.InvitationRequestDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DomainDTO.UpdateDomainRequestDTO;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.sdk.protocol.DomainManagementProtocolAdapter;
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
public class DomainManagementProtocolAdapterMockImpl implements DomainManagementProtocolAdapter {

    private static final Logger log = LoggerFactory.getLogger(DomainManagementProtocolAdapterMockImpl.class);

    private final List<DomainEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, DomainInfoDTO> domains = new ConcurrentHashMap<>();
    private final Map<String, List<DomainMemberDTO>> domainMembers = new ConcurrentHashMap<>();
    private final Map<String, List<DomainInvitationDTO>> domainInvitations = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<DomainInfoDTO> createDomain(CreateDomainRequestDTO request) {
        log.debug("[DomainMock] Create domain: {}", request.getDomainName());
        return CompletableFuture.supplyAsync(() -> {
            String domainId = "domain-" + UUID.randomUUID().toString().substring(0, 8);
            DomainInfoDTO domain = new DomainInfoDTO();
            domain.setDomainId(domainId);
            domain.setDomainName(request.getDomainName());
            domain.setDomainType(request.getDomainType());
            domain.setOwnerId(request.getOwnerId());
            domain.setMemberCount(1);
            domain.setStatus("ACTIVE");
            domain.setCreatedAt(System.currentTimeMillis());
            domain.setUpdatedAt(System.currentTimeMillis());
            domain.setConfig(request.getConfig());
            
            domains.put(domainId, domain);
            domainMembers.put(domainId, new ArrayList<>());
            domainInvitations.put(domainId, new ArrayList<>());
            
            return domain;
        });
    }

    @Override
    public CompletableFuture<Void> deleteDomain(String domainId) {
        log.debug("[DomainMock] Delete domain: {}", domainId);
        return CompletableFuture.runAsync(() -> {
            domains.remove(domainId);
            domainMembers.remove(domainId);
            domainInvitations.remove(domainId);
        });
    }

    @Override
    public CompletableFuture<DomainInfoDTO> getDomain(String domainId) {
        log.debug("[DomainMock] Get domain: {}", domainId);
        return CompletableFuture.supplyAsync(() -> {
            DomainInfoDTO domain = domains.get(domainId);
            if (domain == null) {
                domain = createMockDomain(domainId);
            }
            return domain;
        });
    }

    @Override
    public CompletableFuture<PageResult<DomainInfoDTO>> listDomains(DomainQueryDTO query) {
        log.debug("[DomainMock] List domains with query: type={}, owner={}", query.getDomainType(), query.getOwnerId());
        return CompletableFuture.supplyAsync(() -> {
            List<DomainInfoDTO> allDomains = new ArrayList<>(domains.values());
            if (allDomains.isEmpty()) {
                allDomains.add(createMockDomain("domain-001"));
                allDomains.add(createMockDomain("domain-002"));
            }
            
            int start = query.getPage() * query.getPageSize();
            int end = Math.min(start + query.getPageSize(), allDomains.size());
            List<DomainInfoDTO> pageData = allDomains.subList(start, end);
            
            return PageResult.of(pageData, allDomains.size(), query.getPage(), query.getPageSize());
        });
    }

    @Override
    public CompletableFuture<Void> updateDomain(String domainId, UpdateDomainRequestDTO request) {
        log.debug("[DomainMock] Update domain: {}", domainId);
        return CompletableFuture.runAsync(() -> {
            DomainInfoDTO domain = domains.get(domainId);
            if (domain != null) {
                if (request.getDomainName() != null) {
                    domain.setDomainName(request.getDomainName());
                }
                domain.setUpdatedAt(System.currentTimeMillis());
            }
        });
    }

    @Override
    public CompletableFuture<Void> addDomainMember(String domainId, AddMemberRequestDTO request) {
        log.debug("[DomainMock] Add member to domain: {} -> {}", domainId, request.getMemberId());
        return CompletableFuture.runAsync(() -> {
            List<DomainMemberDTO> members = domainMembers.computeIfAbsent(domainId, id -> new ArrayList<>());
            DomainMemberDTO member = new DomainMemberDTO();
            member.setMemberId(request.getMemberId());
            member.setMemberName(request.getMemberName());
            member.setDomainRole(request.getDomainRole());
            member.setStatus("ACTIVE");
            member.setJoinedAt(System.currentTimeMillis());
            member.setLastActiveAt(System.currentTimeMillis());
            members.add(member);
            
            DomainInfoDTO domain = domains.get(domainId);
            if (domain != null) {
                domain.setMemberCount(members.size());
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeDomainMember(String domainId, String memberId) {
        log.debug("[DomainMock] Remove member from domain: {} -> {}", domainId, memberId);
        return CompletableFuture.runAsync(() -> {
            List<DomainMemberDTO> members = domainMembers.get(domainId);
            if (members != null) {
                members.removeIf(m -> memberId.equals(m.getMemberId()));
            }
        });
    }

    @Override
    public CompletableFuture<List<DomainMemberDTO>> listDomainMembers(String domainId) {
        log.debug("[DomainMock] List members for domain: {}", domainId);
        return CompletableFuture.supplyAsync(() -> {
            List<DomainMemberDTO> members = domainMembers.get(domainId);
            if (members == null || members.isEmpty()) {
                members = createMockMembers();
            }
            return members;
        });
    }

    @Override
    public CompletableFuture<Void> setDomainPolicy(String domainId, DomainPolicyConfigDTO policy) {
        log.debug("[DomainMock] Set policy for domain: {}", domainId);
        return CompletableFuture.runAsync(() -> {
            log.info("[DomainMock] Policy set for domain: {}", domainId);
        });
    }

    @Override
    public CompletableFuture<DomainPolicyConfigDTO> getDomainPolicy(String domainId) {
        log.debug("[DomainMock] Get policy for domain: {}", domainId);
        return CompletableFuture.supplyAsync(() -> createMockPolicy(domainId));
    }

    @Override
    public CompletableFuture<Void> inviteToDomain(String domainId, InvitationRequestDTO request) {
        log.debug("[DomainMock] Invite to domain: {} -> {}", domainId, request.getTargetId());
        return CompletableFuture.runAsync(() -> {
            List<DomainInvitationDTO> invs = domainInvitations.computeIfAbsent(domainId, id -> new ArrayList<>());
            DomainInvitationDTO inv = new DomainInvitationDTO();
            inv.setInvitationId("inv-" + UUID.randomUUID().toString().substring(0, 8));
            inv.setDomainId(domainId);
            inv.setDomainName(domains.get(domainId) != null ? domains.get(domainId).getDomainName() : "Unknown");
            inv.setInviterId("system");
            inv.setTargetId(request.getTargetId());
            inv.setCreatedAt(System.currentTimeMillis());
            inv.setExpiresAt(System.currentTimeMillis() + 86400000L);
            inv.setStatus("PENDING");
            invs.add(inv);
        });
    }

    @Override
    public CompletableFuture<List<DomainInvitationDTO>> listPendingInvitations(String domainId) {
        log.debug("[DomainMock] List pending invitations for domain: {}", domainId);
        return CompletableFuture.supplyAsync(() -> {
            List<DomainInvitationDTO> invs = domainInvitations.get(domainId);
            if (invs == null) {
                invs = new ArrayList<>();
            }
            return invs;
        });
    }

    @Override
    public CompletableFuture<Void> revokeInvitation(String invitationId) {
        log.debug("[DomainMock] Revoke invitation: {}", invitationId);
        return CompletableFuture.runAsync(() -> {
            domainInvitations.values().forEach(list -> {
                list.removeIf(inv -> invitationId.equals(inv.getInvitationId()));
            });
        });
    }

    @Override
    public void addDomainListener(DomainEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeDomainListener(DomainEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private DomainInfoDTO createMockDomain(String domainId) {
        DomainInfoDTO domain = new DomainInfoDTO();
        domain.setDomainId(domainId);
        domain.setDomainName("Domain-" + domainId.substring(0, 4));
        domain.setDomainType("ORGANIZATION");
        domain.setOwnerId("owner-001");
        domain.setMemberCount(5);
        domain.setStatus("ACTIVE");
        domain.setCreatedAt(System.currentTimeMillis() - 86400000L);
        domain.setUpdatedAt(System.currentTimeMillis());
        domain.setConfig(new HashMap<>());
        return domain;
    }

    private List<DomainMemberDTO> createMockMembers() {
        List<DomainMemberDTO> members = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            DomainMemberDTO member = new DomainMemberDTO();
            member.setMemberId("member-" + i);
            member.setMemberName("User-" + i);
            member.setDomainRole(i == 1 ? "ADMIN" : "MEMBER");
            member.setStatus("ACTIVE");
            member.setJoinedAt(System.currentTimeMillis() - (i * 86400000L));
            member.setLastActiveAt(System.currentTimeMillis() - (i * 3600000L));
            members.add(member);
        }
        return members;
    }

    private DomainPolicyConfigDTO createMockPolicy(String domainId) {
        DomainPolicyConfigDTO policy = new DomainPolicyConfigDTO();
        policy.setDomainId(domainId);
        policy.setAllowedSkills(Arrays.asList("skill-001", "skill-002", "skill-003"));
        policy.setRequiredSkills(Arrays.asList("skill-001"));
        
        Map<String, Object> storageConfig = new HashMap<>();
        storageConfig.put("maxSize", "50GB");
        storageConfig.put("retention", "90d");
        policy.setStorageConfig(storageConfig);
        
        Map<String, Object> securityConfig = new HashMap<>();
        securityConfig.put("encryption", "AES256");
        securityConfig.put("authRequired", true);
        securityConfig.put("mfaEnabled", true);
        policy.setSecurityConfig(securityConfig);
        
        Map<String, Object> networkConfig = new HashMap<>();
        networkConfig.put("maxConnections", 500);
        networkConfig.put("timeout", 60000);
        policy.setNetworkConfig(networkConfig);
        
        Map<String, Object> collaborationConfig = new HashMap<>();
        collaborationConfig.put("maxGroups", 10);
        collaborationConfig.put("autoAcceptInvitations", false);
        policy.setCollaborationConfig(collaborationConfig);
        
        return policy;
    }
}
