package net.ooder.skillcenter.sdk;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.nexus.skillcenter.dto.network.NetworkLinkDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkRouteDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkTopologyDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkQualityDTO;
import net.ooder.nexus.skillcenter.dto.network.TopologyNodeDTO;
import net.ooder.nexus.skillcenter.dto.network.TopologyLinkDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NetworkSdkAdapterMockImpl implements NetworkSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(NetworkSdkAdapterMockImpl.class);

    private final Map<String, NetworkLinkDTO> linkStore = new ConcurrentHashMap<>();
    private final Map<String, NetworkRouteDTO> routeStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("[NetworkSdkAdapter] Initializing mock adapter");
        initMockData();
    }

    private void initMockData() {
        String[] statuses = {"活跃", "断开", "活跃"};
        String[] types = {"直接连接", "中继连接", "直接连接"};

        for (int i = 1; i <= 15; i++) {
            NetworkLinkDTO link = new NetworkLinkDTO();
            link.setLinkId("link-" + i);
            link.setSourceNode("node-" + ((i % 5) + 1));
            link.setTargetNode("node-" + ((i % 5) + 2));
            link.setLinkType(types[i % 3]);
            link.setStatus(i <= 12 ? "活跃" : "断开");
            link.setLatency(20 + i * 5);
            link.setBandwidth(100 + i * 50);
            link.setEstablishedAt(System.currentTimeMillis() - i * 3600000L);
            link.setLastActive(System.currentTimeMillis() - i * 60000L);
            linkStore.put(link.getLinkId(), link);
        }

        for (int i = 1; i <= 25; i++) {
            NetworkRouteDTO route = new NetworkRouteDTO();
            route.setRouteId("route-" + i);
            route.setSourceNode("node-1");
            route.setTargetNode("node-" + (i + 1));
            List<String> hops = new ArrayList<>();
            hops.add("node-1");
            if (i % 3 == 0) {
                hops.add("node-relay");
            }
            hops.add("node-" + (i + 1));
            route.setHops(hops);
            route.setTotalLatency(30 + i * 10);
            route.setHopCount(hops.size());
            route.setStatus(i <= 20 ? "有效" : "失效");
            route.setRouteType(i % 3 == 0 ? "中继路由" : "直连路由");
            route.setCreatedAt(new Date(System.currentTimeMillis() - i * 1800000L));
            routeStore.put(route.getRouteId(), route);
        }

        log.info("[NetworkSdkAdapter] Mock data initialized: {} links, {} routes", linkStore.size(), routeStore.size());
    }

    @Override
    public Map<String, Object> getNetworkStatus() {
        log.debug("[NetworkSdkAdapter] Getting network status");
        Map<String, Object> status = new HashMap<>();
        status.put("status", "在线");
        status.put("nodeId", "node-main");
        status.put("nodeType", "SkillCenter");
        status.put("online", true);
        status.put("connectedPeers", 5);
        status.put("localAddress", "192.168.1.100");
        status.put("localPort", 8080);
        status.put("uptime", System.currentTimeMillis() - 3600000L);
        return status;
    }

    @Override
    public Map<String, Object> getNetworkStats() {
        log.debug("[NetworkSdkAdapter] Getting network stats");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNodes", 10);
        stats.put("activeNodes", 8);
        stats.put("totalLinks", linkStore.size());
        stats.put("activeLinks", linkStore.values().stream().filter(l -> "活跃".equals(l.getStatus())).count());
        stats.put("totalRoutes", routeStore.size());
        stats.put("activeRoutes", routeStore.values().stream().filter(r -> "有效".equals(r.getStatus())).count());
        stats.put("avgLatency", 45.5);
        stats.put("avgBandwidth", 1024.0);
        stats.put("totalBytesSent", 1024L * 1024 * 100);
        stats.put("totalBytesReceived", 1024L * 1024 * 150);
        return stats;
    }

    @Override
    public PageResult<NetworkLinkDTO> getLinks(int pageNum, int pageSize) {
        log.debug("[NetworkSdkAdapter] Getting links: page={}, size={}", pageNum, pageSize);
        List<NetworkLinkDTO> all = new ArrayList<>(linkStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public NetworkLinkDTO getLinkById(String linkId) {
        log.debug("[NetworkSdkAdapter] Getting link: {}", linkId);
        return linkStore.get(linkId);
    }

    @Override
    public boolean disconnectLink(String linkId) {
        log.debug("[NetworkSdkAdapter] Disconnecting link: {}", linkId);
        NetworkLinkDTO link = linkStore.get(linkId);
        if (link != null) {
            link.setStatus("断开");
            log.info("[NetworkSdkAdapter] Link disconnected: {}", linkId);
            return true;
        }
        return false;
    }

    @Override
    public boolean reconnectLink(String linkId) {
        log.debug("[NetworkSdkAdapter] Reconnecting link: {}", linkId);
        NetworkLinkDTO link = linkStore.get(linkId);
        if (link != null) {
            link.setStatus("活跃");
            link.setLastActive(System.currentTimeMillis());
            log.info("[NetworkSdkAdapter] Link reconnected: {}", linkId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<NetworkRouteDTO> getRoutes(int pageNum, int pageSize) {
        log.debug("[NetworkSdkAdapter] Getting routes: page={}, size={}", pageNum, pageSize);
        List<NetworkRouteDTO> all = new ArrayList<>(routeStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public NetworkRouteDTO getRouteById(String routeId) {
        log.debug("[NetworkSdkAdapter] Getting route: {}", routeId);
        return routeStore.get(routeId);
    }

    @Override
    public NetworkRouteDTO findRoute(String sourceNode, String targetNode, String algorithm, int maxHops) {
        log.debug("[NetworkSdkAdapter] Finding route: {} -> {}, algorithm={}, maxHops={}", 
            sourceNode, targetNode, algorithm, maxHops);
        
        NetworkRouteDTO route = new NetworkRouteDTO();
        route.setRouteId("route-new-" + System.currentTimeMillis());
        route.setSourceNode(sourceNode);
        route.setTargetNode(targetNode);
        
        List<String> hops = new ArrayList<>();
        hops.add(sourceNode);
        if (maxHops > 2) {
            hops.add("node-relay-1");
        }
        hops.add(targetNode);
        route.setHops(hops);
        
        route.setTotalLatency(45);
        route.setHopCount(hops.size());
        route.setStatus("有效");
        route.setRouteType(hops.size() > 2 ? "中继路由" : "直连路由");
        route.setCreatedAt(new Date(System.currentTimeMillis()));
        
        return route;
    }

    @Override
    public NetworkTopologyDTO getTopology() {
        log.debug("[NetworkSdkAdapter] Getting topology");
        NetworkTopologyDTO topology = new NetworkTopologyDTO();
        
        List<TopologyNodeDTO> nodes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            TopologyNodeDTO node = new TopologyNodeDTO();
            node.setNodeId("node-" + i);
            node.setNodeName("节点 " + i);
            node.setNodeType(i <= 2 ? "核心节点" : "普通节点");
            node.setStatus(i <= 8 ? "在线" : "离线");
            node.setX(100 + (i % 5) * 150);
            node.setY(100 + (i / 5) * 150);
            nodes.add(node);
        }
        topology.setNodes(nodes);

        List<TopologyLinkDTO> links = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            TopologyLinkDTO link = new TopologyLinkDTO();
            link.setLinkId("link-" + i);
            link.setSource("node-" + ((i - 1) % 10 + 1));
            link.setTarget("node-" + (i % 10 + 1));
            link.setStatus(i <= 12 ? "active" : "inactive");
            link.setWeight(1.0 / i);
            links.add(link);
        }
        topology.setLinks(links);

        topology.setTimestamp(System.currentTimeMillis());
        
        return topology;
    }

    @Override
    public NetworkQualityDTO getQuality() {
        log.debug("[NetworkSdkAdapter] Getting network quality");
        NetworkQualityDTO quality = new NetworkQualityDTO();
        quality.setOverallScore(85);
        quality.setLatencyScore(90);
        quality.setBandwidthScore(80);
        quality.setStabilityScore(85);
        quality.setPacketLoss(0.5);
        quality.setJitter(5.2);
        quality.setAvgLatency(45.5);
        quality.setMaxLatency(120);
        quality.setMinLatency(15);
        quality.setTimestamp(System.currentTimeMillis());
        return quality;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private <T> PageResult<T> paginate(List<T> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<T> pageData = start < total ? list.subList(start, end) : new ArrayList<>();
        return new PageResult<>(pageData, total, pageNum, pageSize);
    }
}
