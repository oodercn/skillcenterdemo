package net.ooder.skillcenter.sdk;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SceneSdkAdapterMockImpl implements SceneSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(SceneSdkAdapterMockImpl.class);

    private final Map<String, SceneDefinitionDTO> sceneStore = new ConcurrentHashMap<>();
    private final Map<String, Map<String, CapabilityDTO>> capabilityStore = new ConcurrentHashMap<>();
    private final Map<String, Map<String, SceneRoleDTO>> roleStore = new ConcurrentHashMap<>();
    private final Map<String, Map<String, SceneSnapshotDTO>> snapshotStore = new ConcurrentHashMap<>();
    private final Map<String, String> sceneStatus = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("[SceneSdkAdapter] Initializing mock adapter");
        initMockData();
    }

    private void initMockData() {
        String[] types = {"primary", "collaborative"};

        for (int i = 1; i <= 10; i++) {
            SceneDefinitionDTO scene = new SceneDefinitionDTO();
            scene.setSceneId("scene-" + i);
            scene.setName("场景 " + i);
            scene.setDescription("这是场景 " + i + " 的描述信息");
            scene.setType(types[i % 2]);
            scene.setVersion("1.0." + (i % 10));
            scene.setScenePrefix("prefix-" + i);
            scene.setCreateTime(System.currentTimeMillis() - i * 86400000L);
            scene.setUpdateTime(System.currentTimeMillis() - i * 3600000L);
            sceneStore.put(scene.getSceneId(), scene);
            sceneStatus.put(scene.getSceneId(), i <= 7 ? "active" : "inactive");

            capabilityStore.put(scene.getSceneId(), new ConcurrentHashMap<>());
            roleStore.put(scene.getSceneId(), new ConcurrentHashMap<>());
            snapshotStore.put(scene.getSceneId(), new ConcurrentHashMap<>());

            for (int j = 1; j <= 3; j++) {
                CapabilityDTO cap = new CapabilityDTO();
                cap.setCapId("cap-" + i + "-" + j);
                cap.setName("能力 " + j);
                cap.setType(j % 2 == 0 ? "core" : "extension");
                capabilityStore.get(scene.getSceneId()).put(cap.getCapId(), cap);
            }

            for (int j = 1; j <= 2; j++) {
                SceneRoleDTO role = new SceneRoleDTO();
                role.setRoleId("role-" + i + "-" + j);
                role.setName(j == 1 ? "主节点" : "备份节点");
                role.setRequired(j == 1);
                roleStore.get(scene.getSceneId()).put(role.getRoleId(), role);
            }
        }

        log.info("[SceneSdkAdapter] Mock data initialized: {} scenes", sceneStore.size());
    }

    @Override
    public SceneDefinitionDTO createScene(SceneDefinitionDTO definition) {
        log.debug("[SceneSdkAdapter] Creating scene: {}", definition.getName());
        String id = definition.getSceneId() != null ? definition.getSceneId() : 
            "scene-" + UUID.randomUUID().toString().substring(0, 8);
        definition.setSceneId(id);
        definition.setCreateTime(System.currentTimeMillis());
        definition.setUpdateTime(System.currentTimeMillis());
        sceneStore.put(id, definition);
        sceneStatus.put(id, "inactive");
        capabilityStore.put(id, new ConcurrentHashMap<>());
        roleStore.put(id, new ConcurrentHashMap<>());
        snapshotStore.put(id, new ConcurrentHashMap<>());
        log.info("[SceneSdkAdapter] Scene created: {}", id);
        return definition;
    }

    @Override
    public boolean deleteScene(String sceneId) {
        log.debug("[SceneSdkAdapter] Deleting scene: {}", sceneId);
        SceneDefinitionDTO removed = sceneStore.remove(sceneId);
        if (removed != null) {
            sceneStatus.remove(sceneId);
            capabilityStore.remove(sceneId);
            roleStore.remove(sceneId);
            snapshotStore.remove(sceneId);
            log.info("[SceneSdkAdapter] Scene deleted: {}", sceneId);
            return true;
        }
        return false;
    }

    @Override
    public SceneDefinitionDTO getScene(String sceneId) {
        log.debug("[SceneSdkAdapter] Getting scene: {}", sceneId);
        return sceneStore.get(sceneId);
    }

    @Override
    public PageResult<SceneDefinitionDTO> listScenes(int pageNum, int pageSize) {
        log.debug("[SceneSdkAdapter] Listing scenes: page={}, size={}", pageNum, pageSize);
        List<SceneDefinitionDTO> all = new ArrayList<>(sceneStore.values());
        all.sort(Comparator.comparingLong(SceneDefinitionDTO::getCreateTime).reversed());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public boolean activateScene(String sceneId) {
        log.debug("[SceneSdkAdapter] Activating scene: {}", sceneId);
        SceneDefinitionDTO scene = sceneStore.get(sceneId);
        if (scene != null) {
            sceneStatus.put(sceneId, "active");
            scene.setUpdateTime(System.currentTimeMillis());
            log.info("[SceneSdkAdapter] Scene activated: {}", sceneId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateScene(String sceneId) {
        log.debug("[SceneSdkAdapter] Deactivating scene: {}", sceneId);
        SceneDefinitionDTO scene = sceneStore.get(sceneId);
        if (scene != null) {
            sceneStatus.put(sceneId, "inactive");
            scene.setUpdateTime(System.currentTimeMillis());
            log.info("[SceneSdkAdapter] Scene deactivated: {}", sceneId);
            return true;
        }
        return false;
    }

    @Override
    public SceneStateDTO getSceneState(String sceneId) {
        log.debug("[SceneSdkAdapter] Getting scene state: {}", sceneId);
        SceneDefinitionDTO scene = sceneStore.get(sceneId);
        if (scene != null) {
            SceneStateDTO state = new SceneStateDTO();
            state.setSceneId(sceneId);
            state.setActive("active".equals(sceneStatus.get(sceneId)));
            state.setMemberCount(roleStore.getOrDefault(sceneId, new ConcurrentHashMap<>()).size());
            state.setCreateTime(scene.getCreateTime());
            state.setLastUpdateTime(scene.getUpdateTime());
            return state;
        }
        return null;
    }

    @Override
    public boolean addCapability(String sceneId, CapabilityDTO capability) {
        log.debug("[SceneSdkAdapter] Adding capability: {} to scene {}", capability.getCapId(), sceneId);
        Map<String, CapabilityDTO> caps = capabilityStore.get(sceneId);
        if (caps != null) {
            caps.put(capability.getCapId(), capability);
            log.info("[SceneSdkAdapter] Capability added: {}", capability.getCapId());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeCapability(String sceneId, String capabilityId) {
        log.debug("[SceneSdkAdapter] Removing capability: {} from scene {}", capabilityId, sceneId);
        Map<String, CapabilityDTO> caps = capabilityStore.get(sceneId);
        if (caps != null && caps.remove(capabilityId) != null) {
            log.info("[SceneSdkAdapter] Capability removed: {}", capabilityId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<CapabilityDTO> listCapabilities(String sceneId, int pageNum, int pageSize) {
        log.debug("[SceneSdkAdapter] Listing capabilities for scene: {}", sceneId);
        Map<String, CapabilityDTO> caps = capabilityStore.getOrDefault(sceneId, new ConcurrentHashMap<>());
        List<CapabilityDTO> all = new ArrayList<>(caps.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public CapabilityDTO getCapability(String sceneId, String capabilityId) {
        log.debug("[SceneSdkAdapter] Getting capability: {} from scene {}", capabilityId, sceneId);
        Map<String, CapabilityDTO> caps = capabilityStore.get(sceneId);
        return caps != null ? caps.get(capabilityId) : null;
    }

    @Override
    public boolean addRole(String sceneId, SceneRoleDTO role) {
        log.debug("[SceneSdkAdapter] Adding role: {} to scene {}", role.getRoleId(), sceneId);
        Map<String, SceneRoleDTO> roles = roleStore.get(sceneId);
        if (roles != null) {
            roles.put(role.getRoleId(), role);
            log.info("[SceneSdkAdapter] Role added: {}", role.getRoleId());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeRole(String sceneId, String roleId) {
        log.debug("[SceneSdkAdapter] Removing role: {} from scene {}", roleId, sceneId);
        Map<String, SceneRoleDTO> roles = roleStore.get(sceneId);
        if (roles != null && roles.remove(roleId) != null) {
            log.info("[SceneSdkAdapter] Role removed: {}", roleId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<SceneRoleDTO> listRoles(String sceneId, int pageNum, int pageSize) {
        log.debug("[SceneSdkAdapter] Listing roles for scene: {}", sceneId);
        Map<String, SceneRoleDTO> roles = roleStore.getOrDefault(sceneId, new ConcurrentHashMap<>());
        List<SceneRoleDTO> all = new ArrayList<>(roles.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public SceneSnapshotDTO createSnapshot(String sceneId) {
        log.debug("[SceneSdkAdapter] Creating snapshot for scene: {}", sceneId);
        SceneDefinitionDTO scene = sceneStore.get(sceneId);
        if (scene != null) {
            SceneSnapshotDTO snapshot = new SceneSnapshotDTO();
            snapshot.setSnapshotId("snapshot-" + System.currentTimeMillis());
            snapshot.setSceneId(sceneId);
            snapshot.setCreateTime(System.currentTimeMillis());
            snapshot.setVersion(scene.getVersion());
            Map<String, SceneSnapshotDTO> snapshots = snapshotStore.get(sceneId);
            if (snapshots != null) {
                snapshots.put(snapshot.getSnapshotId(), snapshot);
            }
            log.info("[SceneSdkAdapter] Snapshot created: {}", snapshot.getSnapshotId());
            return snapshot;
        }
        return null;
    }

    @Override
    public boolean restoreSnapshot(String sceneId, String snapshotId) {
        log.debug("[SceneSdkAdapter] Restoring snapshot: {} for scene: {}", snapshotId, sceneId);
        Map<String, SceneSnapshotDTO> snapshots = snapshotStore.get(sceneId);
        if (snapshots != null && snapshots.containsKey(snapshotId)) {
            log.info("[SceneSdkAdapter] Snapshot restored: {}", snapshotId);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<SceneSnapshotDTO> listSnapshots(String sceneId, int pageNum, int pageSize) {
        log.debug("[SceneSdkAdapter] Listing snapshots for scene: {}", sceneId);
        Map<String, SceneSnapshotDTO> snapshots = snapshotStore.getOrDefault(sceneId, new ConcurrentHashMap<>());
        List<SceneSnapshotDTO> all = new ArrayList<>(snapshots.values());
        all.sort(Comparator.comparingLong(SceneSnapshotDTO::getCreateTime).reversed());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public boolean deleteSnapshot(String sceneId, String snapshotId) {
        log.debug("[SceneSdkAdapter] Deleting snapshot: {} from scene {}", snapshotId, sceneId);
        Map<String, SceneSnapshotDTO> snapshots = snapshotStore.get(sceneId);
        if (snapshots != null && snapshots.remove(snapshotId) != null) {
            log.info("[SceneSdkAdapter] Snapshot deleted: {}", snapshotId);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return true;
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
