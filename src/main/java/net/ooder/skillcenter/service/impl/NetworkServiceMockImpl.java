package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.service.NetworkService;
import net.ooder.nexus.skillcenter.dto.network.NetworkLinkDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkRouteDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkTopologyDTO;
import net.ooder.nexus.skillcenter.dto.network.NetworkQualityDTO;
import net.ooder.nexus.skillcenter.dto.network.TopologyNodeDTO;
import net.ooder.nexus.skillcenter.dto.network.TopologyLinkDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class NetworkServiceMockImpl implements NetworkService {

    private static final Logger log = LoggerFactory.getLogger(NetworkServiceMockImpl.class);

    private final Map<String, NetworkLinkDTO> linkStore = new ConcurrentHashMap<>();
    private final Map<String, NetworkRouteDTO> routeStore = new ConcurrentHashMap<>();
    private boolean firewallEnabled = true;

    @PostConstruct
    public void init() {
        log.info("[NetworkServiceMockImpl] Initialized with Mock mode");
        initMockData();
    }

    private void initMockData() {
        String[] statuses = {"active", "idle", "busy"};
        String[] types = {"P2P", "Relay", "Direct"};

        for (int i = 1; i <= 10; i++) {
            NetworkLinkDTO link = new NetworkLinkDTO();
            link.setId("link-" + i);
            link.setLinkId("link-" + i);
            link.setSourceNode("node-" + ((i % 5) + 1));
            link.setTargetNode("node-" + ((i % 5) + 2));
            link.setLinkType(types[i % 3]);
            link.setStatus(statuses[i % 3]);
            link.setLatency(20 + i * 5);
            link.setBandwidth(100 + i * 10);
            link.setCreatedAt(new Date(System.currentTimeMillis() - i * 3600000L));
            linkStore.put(link.getLinkId(), link);
        }

        for (int i = 1; i <= 5; i++) {
            NetworkRouteDTO route = new NetworkRouteDTO();
            route.setId("route-" + i);
            route.setRouteId("route-" + i);
            route.setSourceNode("node-" + i);
            route.setTargetNode("node-" + ((i % 5) + 1));
            route.setHopCount(i);
            route.setTotalLatency(50 + i * 20);
            route.setStatus("active");
            route.setRouteType("optimal");
            route.setHops(Arrays.asList("node-1", "node-2", "node-3"));
            route.setCreatedAt(new Date(System.currentTimeMillis() - i * 1800000L));
            routeStore.put(route.getRouteId(), route);
        }
    }

    @Override
    public Map<String, Object> getNetworkStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "online");
        status.put("nodeCount", 5);
        status.put("activeLinks", linkStore.values().stream()
            .filter(l -> "active".equals(l.getStatus())).count());
        status.put("totalBandwidth", 500);
        status.put("avgLatency", 45);
        return status;
    }

    @Override
    public Map<String, Object> getNetworkStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNodes", 5);
        stats.put("activeLinks", linkStore.values().stream()
            .filter(l -> "active".equals(l.getStatus())).count());
        stats.put("totalLinks", linkStore.size());
        stats.put("totalRoutes", routeStore.size());
        stats.put("avgLatency", 45);
        stats.put("totalBandwidth", 500);
        stats.put("uptime", "24h 30m");
        return stats;
    }

    @Override
    public PageResult<NetworkLinkDTO> getLinks(int pageNum, int pageSize) {
        List<NetworkLinkDTO> all = new ArrayList<>(linkStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public NetworkLinkDTO getLinkById(String linkId) {
        return linkStore.get(linkId);
    }

    @Override
    public boolean disconnectLink(String linkId) {
        NetworkLinkDTO link = linkStore.get(linkId);
        if (link != null) {
            link.setStatus("disconnected");
            log.info("[NetworkServiceMockImpl] Link disconnected: {}", linkId);
            return true;
        }
        return false;
    }

    @Override
    public boolean reconnectLink(String linkId) {
        NetworkLinkDTO link = linkStore.get(linkId);
        if (link != null) {
            link.setStatus("active");
            log.info("[NetworkServiceMockImpl] Link reconnected: {}", linkId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<NetworkRouteDTO> getRoutes(int pageNum, int pageSize) {
        List<NetworkRouteDTO> all = new ArrayList<>(routeStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public NetworkRouteDTO getRouteById(String routeId) {
        return routeStore.get(routeId);
    }

    @Override
    public NetworkRouteDTO findRoute(String sourceNode, String targetNode) {
        return routeStore.values().stream()
            .filter(r -> sourceNode.equals(r.getSourceNode()) && targetNode.equals(r.getTargetNode()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public NetworkTopologyDTO getTopology() {
        NetworkTopologyDTO topology = new NetworkTopologyDTO();
        
        List<TopologyNodeDTO> nodes = new ArrayList<>();
        nodes.add(createNode("node-1", "节点1", "agent", "active", 100, 100));
        nodes.add(createNode("node-2", "节点2", "agent", "active", 200, 100));
        nodes.add(createNode("node-3", "节点3", "agent", "active", 150, 200));
        nodes.add(createNode("node-4", "节点4", "relay", "idle", 300, 150));
        nodes.add(createNode("node-5", "节点5", "agent", "active", 250, 250));
        
        List<TopologyLinkDTO> links = new ArrayList<>();
        int linkIndex = 1;
        for (NetworkLinkDTO link : linkStore.values()) {
            TopologyLinkDTO topoLink = new TopologyLinkDTO();
            topoLink.setLinkId(link.getLinkId());
            topoLink.setSource(link.getSourceNode());
            topoLink.setTarget(link.getTargetNode());
            topoLink.setStatus(link.getStatus());
            topoLink.setWeight(1.0);
            links.add(topoLink);
        }
        
        topology.setNodes(nodes);
        topology.setLinks(links);
        topology.setTimestamp(System.currentTimeMillis());
        return topology;
    }

    private TopologyNodeDTO createNode(String id, String name, String type, String status, int x, int y) {
        TopologyNodeDTO node = new TopologyNodeDTO();
        node.setNodeId(id);
        node.setNodeName(name);
        node.setNodeType(type);
        node.setStatus(status);
        node.setX(x);
        node.setY(y);
        return node;
    }

    @Override
    public NetworkQualityDTO getQuality() {
        NetworkQualityDTO quality = new NetworkQualityDTO();
        quality.setOverallScore(85);
        quality.setLatencyScore(90);
        quality.setBandwidthScore(80);
        quality.setStabilityScore(85);
        quality.setPacketLoss(0.5);
        quality.setJitter(5);
        return quality;
    }

    private <T> PageResult<T> paginate(List<T> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            return PageResult.empty();
        }

        List<T> pageList = list.subList(start, end);
        return PageResult.of(pageList, total, pageNum, pageSize);
    }
}
