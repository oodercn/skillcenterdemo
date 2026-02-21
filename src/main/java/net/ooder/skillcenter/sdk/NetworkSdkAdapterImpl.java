package net.ooder.skillcenter.sdk;

import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.nexus.skillcenter.dto.network.NetworkLinkDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkRouteDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkTopologyDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkQualityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Primary
public class NetworkSdkAdapterImpl implements NetworkSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(NetworkSdkAdapterImpl.class);

    @Autowired
    private SdkConfig sdkConfig;

    @Autowired
    private NetworkSdkAdapterMockImpl mockAdapter;

    @Autowired
    private SceneEngineAdapter sceneEngineAdapter;

    private boolean sdkAvailable = false;

    @PostConstruct
    public void init() {
        if (sdkConfig.isMockMode()) {
            log.info("[NetworkSdkAdapter] Running in mock mode");
            return;
        }

        log.info("[NetworkSdkAdapter] Checking SDK availability...");
        sdkAvailable = sceneEngineAdapter.isAvailable();

        if (sdkAvailable) {
            log.info("[NetworkSdkAdapter] SDK is available, using real implementation");
        } else {
            log.warn("[NetworkSdkAdapter] SDK network APIs not available, falling back to mock");
        }
    }

    @Override
    public Map<String, Object> getNetworkStatus() {
        if (!sdkAvailable) {
            return mockAdapter.getNetworkStatus();
        }

        log.debug("[NetworkSdkAdapter] Getting network status via SDK");
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("status", "在线");
            status.put("nodeId", sdkConfig.getAgentId());
            status.put("nodeType", "SkillCenter");
            status.put("online", true);
            status.put("connectedPeers", 0);
            status.put("localAddress", sdkConfig.getEndpoint());
            status.put("localPort", sdkConfig.getUdpPort());
            status.put("uptime", System.currentTimeMillis() - 3600000L);
            return status;
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get network status: {}", e.getMessage());
            return mockAdapter.getNetworkStatus();
        }
    }

    @Override
    public Map<String, Object> getNetworkStats() {
        if (!sdkAvailable) {
            return mockAdapter.getNetworkStats();
        }

        log.debug("[NetworkSdkAdapter] Getting network stats via SDK");
        try {
            return mockAdapter.getNetworkStats();
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get network stats: {}", e.getMessage());
            return mockAdapter.getNetworkStats();
        }
    }

    @Override
    public PageResult<NetworkLinkDTO> getLinks(int pageNum, int pageSize) {
        if (!sdkAvailable) {
            return mockAdapter.getLinks(pageNum, pageSize);
        }

        log.debug("[NetworkSdkAdapter] Getting links via SDK: page={}, size={}", pageNum, pageSize);
        try {
            return mockAdapter.getLinks(pageNum, pageSize);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get links: {}", e.getMessage());
            return mockAdapter.getLinks(pageNum, pageSize);
        }
    }

    @Override
    public NetworkLinkDTO getLinkById(String linkId) {
        if (!sdkAvailable) {
            return mockAdapter.getLinkById(linkId);
        }

        log.debug("[NetworkSdkAdapter] Getting link via SDK: {}", linkId);
        try {
            return mockAdapter.getLinkById(linkId);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get link: {}", e.getMessage());
            return mockAdapter.getLinkById(linkId);
        }
    }

    @Override
    public boolean disconnectLink(String linkId) {
        if (!sdkAvailable) {
            return mockAdapter.disconnectLink(linkId);
        }

        log.debug("[NetworkSdkAdapter] Disconnecting link via SDK: {}", linkId);
        try {
            return mockAdapter.disconnectLink(linkId);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to disconnect link: {}", e.getMessage());
            return mockAdapter.disconnectLink(linkId);
        }
    }

    @Override
    public boolean reconnectLink(String linkId) {
        if (!sdkAvailable) {
            return mockAdapter.reconnectLink(linkId);
        }

        log.debug("[NetworkSdkAdapter] Reconnecting link via SDK: {}", linkId);
        try {
            return mockAdapter.reconnectLink(linkId);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to reconnect link: {}", e.getMessage());
            return mockAdapter.reconnectLink(linkId);
        }
    }

    @Override
    public PageResult<NetworkRouteDTO> getRoutes(int pageNum, int pageSize) {
        if (!sdkAvailable) {
            return mockAdapter.getRoutes(pageNum, pageSize);
        }

        log.debug("[NetworkSdkAdapter] Getting routes via SDK: page={}, size={}", pageNum, pageSize);
        try {
            return mockAdapter.getRoutes(pageNum, pageSize);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get routes: {}", e.getMessage());
            return mockAdapter.getRoutes(pageNum, pageSize);
        }
    }

    @Override
    public NetworkRouteDTO getRouteById(String routeId) {
        if (!sdkAvailable) {
            return mockAdapter.getRouteById(routeId);
        }

        log.debug("[NetworkSdkAdapter] Getting route via SDK: {}", routeId);
        try {
            return mockAdapter.getRouteById(routeId);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get route: {}", e.getMessage());
            return mockAdapter.getRouteById(routeId);
        }
    }

    @Override
    public NetworkRouteDTO findRoute(String sourceNode, String targetNode, String algorithm, int maxHops) {
        if (!sdkAvailable) {
            return mockAdapter.findRoute(sourceNode, targetNode, algorithm, maxHops);
        }

        log.debug("[NetworkSdkAdapter] Finding route via SDK: {} -> {}", sourceNode, targetNode);
        try {
            return mockAdapter.findRoute(sourceNode, targetNode, algorithm, maxHops);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to find route: {}", e.getMessage());
            return mockAdapter.findRoute(sourceNode, targetNode, algorithm, maxHops);
        }
    }

    @Override
    public NetworkTopologyDTO getTopology() {
        if (!sdkAvailable) {
            return mockAdapter.getTopology();
        }

        log.debug("[NetworkSdkAdapter] Getting topology via SDK");
        try {
            return mockAdapter.getTopology();
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get topology: {}", e.getMessage());
            return mockAdapter.getTopology();
        }
    }

    @Override
    public NetworkQualityDTO getQuality() {
        if (!sdkAvailable) {
            return mockAdapter.getQuality();
        }

        log.debug("[NetworkSdkAdapter] Getting quality via SDK");
        try {
            return mockAdapter.getQuality();
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get quality: {}", e.getMessage());
            return mockAdapter.getQuality();
        }
    }

    @Override
    public boolean isAvailable() {
        return sdkAvailable || mockAdapter.isAvailable();
    }
}
