package net.ooder.skillcenter.sdk.protocol.impl;

import net.ooder.nexus.skillcenter.dto.protocol.DiscoveryDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DiscoveryDTO.DiscoveryRequestDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DiscoveryDTO.DiscoveryResultDTO;
import net.ooder.nexus.skillcenter.dto.protocol.DiscoveryDTO.PeerDTO;
import net.ooder.skillcenter.sdk.protocol.DiscoveryProtocolAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DiscoveryProtocolAdapterMockImpl implements DiscoveryProtocolAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryProtocolAdapterMockImpl.class);

    private final List<DiscoveryEventListener> listeners = new CopyOnWriteArrayList<>();
    private boolean broadcasting = false;

    @Override
    public CompletableFuture<DiscoveryResultDTO> discoverPeers(DiscoveryRequestDTO request) {
        log.debug("[DiscoveryMock] Discovering peers with request: {}", request.getRequestId());
        return CompletableFuture.supplyAsync(() -> {
            DiscoveryResultDTO result = new DiscoveryResultDTO();
            result.setRequestId(request.getRequestId());
            result.setSuccess(true);
            result.setPeers(generateMockPeers());
            result.setMcp(generateMockMcp());
            return result;
        });
    }

    @Override
    public CompletableFuture<List<PeerDTO>> listDiscoveredPeers() {
        log.debug("[DiscoveryMock] Listing discovered peers");
        return CompletableFuture.supplyAsync(this::generateMockPeers);
    }

    @Override
    public CompletableFuture<PeerDTO> discoverMcp() {
        log.debug("[DiscoveryMock] Discovering MCP");
        return CompletableFuture.supplyAsync(this::generateMockMcp);
    }

    @Override
    public void addDiscoveryListener(DiscoveryEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeDiscoveryListener(DiscoveryEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void startBroadcast() {
        log.info("[DiscoveryMock] Starting broadcast");
        broadcasting = true;
    }

    @Override
    public void stopBroadcast() {
        log.info("[DiscoveryMock] Stopping broadcast");
        broadcasting = false;
    }

    @Override
    public boolean isBroadcasting() {
        return broadcasting;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private List<PeerDTO> generateMockPeers() {
        List<PeerDTO> peers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            PeerDTO peer = new PeerDTO();
            peer.setPeerId("peer-" + UUID.randomUUID().toString().substring(0, 8));
            peer.setPeerName("Agent-" + i);
            peer.setPeerType(i % 2 == 0 ? "ROUTE_AGENT" : "END_AGENT");
            peer.setIpAddress("192.168.1." + (100 + i));
            peer.setPort(8080 + i);
            peer.setLastSeen(System.currentTimeMillis() - (i * 60000L));
            peer.setDomainId("domain-001");
            peers.add(peer);
        }
        return peers;
    }

    private PeerDTO generateMockMcp() {
        PeerDTO mcp = new PeerDTO();
        mcp.setPeerId("mcp-" + UUID.randomUUID().toString().substring(0, 8));
        mcp.setPeerName("MCP-Agent-Main");
        mcp.setPeerType("MCP_AGENT");
        mcp.setIpAddress("192.168.1.10");
        mcp.setPort(9000);
        mcp.setLastSeen(System.currentTimeMillis());
        mcp.setDomainId("domain-001");
        return mcp;
    }
}
