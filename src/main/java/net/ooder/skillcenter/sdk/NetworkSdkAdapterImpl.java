package net.ooder.skillcenter.sdk;

import net.ooder.scene.provider.*;
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

    private NetworkProvider networkProvider;
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
            networkProvider = sceneEngineAdapter.getNetworkProvider();
            if (networkProvider != null) {
                log.info("[NetworkSdkAdapter] NetworkProvider available, using real implementation");
            } else {
                sdkAvailable = false;
                log.warn("[NetworkSdkAdapter] NetworkProvider not available, falling back to mock");
            }
        } else {
            log.warn("[NetworkSdkAdapter] SDK not available, falling back to mock");
        }
    }

    @Override
    public Map<String, Object> getNetworkStatus() {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.getNetworkStatus();
        }

        try {
            NetworkStatus status = networkProvider.getStatus();
            Map<String, Object> result = new HashMap<>();
            result.put("status", status.getStatus());
            result.put("nodeId", status.getNodeId());
            result.put("nodeType", status.getNodeType());
            result.put("online", status.isOnline());
            result.put("connectedPeers", status.getConnectedPeers());
            result.put("localAddress", status.getLocalAddress());
            result.put("localPort", status.getLocalPort());
            result.put("uptime", status.getUptime());
            return result;
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get network status: {}", e.getMessage());
            return mockAdapter.getNetworkStatus();
        }
    }

    @Override
    public Map<String, Object> getNetworkStats() {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.getNetworkStats();
        }

        try {
            NetworkStats stats = networkProvider.getStats();
            Map<String, Object> result = new HashMap<>();
            result.put("totalLinks", stats.getTotalLinks());
            result.put("activeLinks", stats.getActiveLinks());
            result.put("totalRoutes", stats.getTotalRoutes());
            result.put("activeRoutes", stats.getActiveRoutes());
            result.put("bytesSent", stats.getBytesSent());
            result.put("bytesReceived", stats.getBytesReceived());
            result.put("messagesSent", stats.getMessagesSent());
            result.put("messagesReceived", stats.getMessagesReceived());
            result.put("averageLatency", stats.getAverageLatency());
            return result;
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get network stats: {}", e.getMessage());
            return mockAdapter.getNetworkStats();
        }
    }

    @Override
    public PageResult<NetworkLinkDTO> getLinks(int pageNum, int pageSize) {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.getLinks(pageNum, pageSize);
        }

        try {
            net.ooder.scene.core.PageResult<NetworkLink> result = networkProvider.listLinks(pageNum, pageSize);
            List<NetworkLinkDTO> dtoList = new ArrayList<>();
            for (NetworkLink link : result.getList()) {
                dtoList.add(convertLinkToDTO(link));
            }
            return new PageResult<>(dtoList, result.getTotal(), result.getPageNum(), result.getPageSize());
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get links: {}", e.getMessage());
            return mockAdapter.getLinks(pageNum, pageSize);
        }
    }

    @Override
    public NetworkLinkDTO getLinkById(String linkId) {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.getLinkById(linkId);
        }

        try {
            NetworkLink link = networkProvider.getLink(linkId);
            return link != null ? convertLinkToDTO(link) : null;
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get link: {}", e.getMessage());
            return mockAdapter.getLinkById(linkId);
        }
    }

    @Override
    public boolean disconnectLink(String linkId) {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.disconnectLink(linkId);
        }

        try {
            return networkProvider.disconnectLink(linkId);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to disconnect link: {}", e.getMessage());
            return mockAdapter.disconnectLink(linkId);
        }
    }

    @Override
    public boolean reconnectLink(String linkId) {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.reconnectLink(linkId);
        }

        try {
            return networkProvider.reconnectLink(linkId);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to reconnect link: {}", e.getMessage());
            return mockAdapter.reconnectLink(linkId);
        }
    }

    @Override
    public PageResult<NetworkRouteDTO> getRoutes(int pageNum, int pageSize) {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.getRoutes(pageNum, pageSize);
        }

        try {
            net.ooder.scene.core.PageResult<NetworkRoute> result = networkProvider.listRoutes(pageNum, pageSize);
            List<NetworkRouteDTO> dtoList = new ArrayList<>();
            for (NetworkRoute route : result.getList()) {
                dtoList.add(convertRouteToDTO(route));
            }
            return new PageResult<>(dtoList, result.getTotal(), result.getPageNum(), result.getPageSize());
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get routes: {}", e.getMessage());
            return mockAdapter.getRoutes(pageNum, pageSize);
        }
    }

    @Override
    public NetworkRouteDTO getRouteById(String routeId) {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.getRouteById(routeId);
        }

        try {
            NetworkRoute route = networkProvider.getRoute(routeId);
            return route != null ? convertRouteToDTO(route) : null;
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get route: {}", e.getMessage());
            return mockAdapter.getRouteById(routeId);
        }
    }

    @Override
    public NetworkRouteDTO findRoute(String sourceNode, String targetNode, String algorithm, int maxHops) {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.findRoute(sourceNode, targetNode, algorithm, maxHops);
        }

        try {
            NetworkRoute route = networkProvider.findRoute(sourceNode, targetNode, algorithm, maxHops);
            return route != null ? convertRouteToDTO(route) : null;
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to find route: {}", e.getMessage());
            return mockAdapter.findRoute(sourceNode, targetNode, algorithm, maxHops);
        }
    }

    @Override
    public NetworkTopologyDTO getTopology() {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.getTopology();
        }

        try {
            NetworkTopology topology = networkProvider.getTopology();
            return convertTopologyToDTO(topology);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get topology: {}", e.getMessage());
            return mockAdapter.getTopology();
        }
    }

    @Override
    public NetworkQualityDTO getQuality() {
        if (!sdkAvailable || networkProvider == null) {
            return mockAdapter.getQuality();
        }

        try {
            NetworkQuality quality = networkProvider.getQuality();
            return convertQualityToDTO(quality);
        } catch (Exception e) {
            log.error("[NetworkSdkAdapter] Failed to get quality: {}", e.getMessage());
            return mockAdapter.getQuality();
        }
    }

    @Override
    public boolean isAvailable() {
        return sdkAvailable || mockAdapter.isAvailable();
    }

    private NetworkLinkDTO convertLinkToDTO(NetworkLink link) {
        NetworkLinkDTO dto = new NetworkLinkDTO();
        dto.setLinkId(link.getLinkId());
        dto.setSourceNode(link.getSourceNode());
        dto.setTargetNode(link.getTargetNode());
        dto.setLinkType(link.getLinkType());
        dto.setStatus(link.getStatus());
        dto.setLatency(link.getLatency());
        dto.setBandwidth(link.getBandwidth());
        dto.setEstablishedAt(link.getEstablishedAt());
        dto.setLastActive(link.getLastActive());
        return dto;
    }

    private NetworkRouteDTO convertRouteToDTO(NetworkRoute route) {
        NetworkRouteDTO dto = new NetworkRouteDTO();
        dto.setRouteId(route.getRouteId());
        dto.setSourceNode(route.getSourceNode());
        dto.setTargetNode(route.getTargetNode());
        dto.setHops(route.getHops());
        dto.setTotalLatency(route.getTotalLatency());
        dto.setHopCount(route.getHopCount());
        dto.setStatus(route.getStatus());
        dto.setRouteType(route.getRouteType());
        return dto;
    }

    private NetworkTopologyDTO convertTopologyToDTO(NetworkTopology topology) {
        NetworkTopologyDTO dto = new NetworkTopologyDTO();
        dto.setNodes(topology.getNodes());
        dto.setEdges(topology.getEdges());
        dto.setUpdatedAt(topology.getUpdatedAt());
        return dto;
    }

    private NetworkQualityDTO convertQualityToDTO(NetworkQuality quality) {
        NetworkQualityDTO dto = new NetworkQualityDTO();
        dto.setOverallScore(quality.getOverallScore());
        dto.setLatencyScore(quality.getLatencyScore());
        dto.setBandwidthScore(quality.getBandwidthScore());
        dto.setStabilityScore(quality.getStabilityScore());
        dto.setPacketLoss(quality.getPacketLoss());
        dto.setJitter(quality.getJitter());
        return dto;
    }
}
