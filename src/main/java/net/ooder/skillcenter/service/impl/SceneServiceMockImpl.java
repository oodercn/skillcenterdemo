package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import net.ooder.skillcenter.service.SceneService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SceneServiceMockImpl implements SceneService {

    private final Map<String, SceneDefinitionDTO> sceneStore = new ConcurrentHashMap<>();
    private final Map<String, SceneStateDTO> stateStore = new ConcurrentHashMap<>();
    private final Map<String, List<CapabilityDTO>> capabilityStore = new ConcurrentHashMap<>();
    private final Map<String, List<String>> collaborativeStore = new ConcurrentHashMap<>();
    private final Map<String, List<SceneSnapshotDTO>> snapshotStore = new ConcurrentHashMap<>();

    public SceneServiceMockImpl() {
        initMockData();
    }

    private void initMockData() {
        SceneDefinitionDTO scene1 = new SceneDefinitionDTO();
        scene1.setSceneId("scene-001");
        scene1.setName("数据处理场景");
        scene1.setDescription("用于数据处理的场景");
        scene1.setVersion("1.0.0");
        scene1.setType("primary");
        scene1.setScenePrefix("data");
        scene1.setCreateTime(System.currentTimeMillis());
        scene1.setUpdateTime(System.currentTimeMillis());
        sceneStore.put(scene1.getSceneId(), scene1);

        SceneStateDTO state1 = new SceneStateDTO();
        state1.setSceneId("scene-001");
        state1.setActive(true);
        state1.setMemberCount(3);
        state1.setInstalledSkills(Arrays.asList("skill-001", "skill-002"));
        state1.setCreateTime(System.currentTimeMillis());
        state1.setLastUpdateTime(System.currentTimeMillis());
        stateStore.put(scene1.getSceneId(), state1);

        capabilityStore.put("scene-001", new ArrayList<>());
        collaborativeStore.put("scene-001", new ArrayList<>());
        snapshotStore.put("scene-001", new ArrayList<>());
    }

    @Override
    public SceneDefinitionDTO create(SceneDefinitionDTO definition) {
        String sceneId = definition.getSceneId() != null ? definition.getSceneId() : "scene-" + UUID.randomUUID().toString().substring(0, 8);
        definition.setSceneId(sceneId);
        definition.setCreateTime(System.currentTimeMillis());
        definition.setUpdateTime(System.currentTimeMillis());
        sceneStore.put(sceneId, definition);

        SceneStateDTO state = new SceneStateDTO();
        state.setSceneId(sceneId);
        state.setActive(false);
        state.setMemberCount(0);
        state.setInstalledSkills(new ArrayList<>());
        state.setCreateTime(System.currentTimeMillis());
        state.setLastUpdateTime(System.currentTimeMillis());
        stateStore.put(sceneId, state);

        capabilityStore.put(sceneId, new ArrayList<>());
        collaborativeStore.put(sceneId, new ArrayList<>());
        snapshotStore.put(sceneId, new ArrayList<>());

        return definition;
    }

    @Override
    public boolean delete(String sceneId) {
        sceneStore.remove(sceneId);
        stateStore.remove(sceneId);
        capabilityStore.remove(sceneId);
        collaborativeStore.remove(sceneId);
        snapshotStore.remove(sceneId);
        return true;
    }

    @Override
    public SceneDefinitionDTO get(String sceneId) {
        return sceneStore.get(sceneId);
    }

    @Override
    public PageResult<SceneDefinitionDTO> listAll(int pageNum, int pageSize) {
        List<SceneDefinitionDTO> all = new ArrayList<>(sceneStore.values());
        return paginate(all, pageNum, pageSize);
    }

    @Override
    public boolean activate(String sceneId) {
        SceneStateDTO state = stateStore.get(sceneId);
        if (state != null) {
            state.setActive(true);
            state.setLastUpdateTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivate(String sceneId) {
        SceneStateDTO state = stateStore.get(sceneId);
        if (state != null) {
            state.setActive(false);
            state.setLastUpdateTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    public SceneStateDTO getState(String sceneId) {
        return stateStore.get(sceneId);
    }

    @Override
    public boolean addCapability(String sceneId, CapabilityDTO capability) {
        List<CapabilityDTO> capabilities = capabilityStore.get(sceneId);
        if (capabilities != null) {
            capability.setSceneId(sceneId);
            capabilities.add(capability);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeCapability(String sceneId, String capId) {
        List<CapabilityDTO> capabilities = capabilityStore.get(sceneId);
        if (capabilities != null) {
            return capabilities.removeIf(c -> capId.equals(c.getCapId()));
        }
        return false;
    }

    @Override
    public PageResult<CapabilityDTO> listCapabilities(String sceneId, int pageNum, int pageSize) {
        List<CapabilityDTO> capabilities = capabilityStore.get(sceneId);
        if (capabilities == null) return PageResult.empty();
        return paginate(capabilities, pageNum, pageSize);
    }

    @Override
    public CapabilityDTO getCapability(String sceneId, String capId) {
        List<CapabilityDTO> capabilities = capabilityStore.get(sceneId);
        if (capabilities == null) return null;
        return capabilities.stream()
            .filter(c -> capId.equals(c.getCapId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean addCollaborativeScene(String sceneId, String collaborativeSceneId) {
        List<String> collaborative = collaborativeStore.get(sceneId);
        if (collaborative != null && !collaborative.contains(collaborativeSceneId)) {
            collaborative.add(collaborativeSceneId);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeCollaborativeScene(String sceneId, String collaborativeSceneId) {
        List<String> collaborative = collaborativeStore.get(sceneId);
        if (collaborative != null) {
            return collaborative.remove(collaborativeSceneId);
        }
        return false;
    }

    @Override
    public PageResult<String> listCollaborativeScenes(String sceneId, int pageNum, int pageSize) {
        List<String> collaborative = collaborativeStore.get(sceneId);
        if (collaborative == null) return PageResult.empty();
        return paginate(collaborative, pageNum, pageSize);
    }

    @Override
    public SceneSnapshotDTO createSnapshot(String sceneId) {
        SceneDefinitionDTO scene = sceneStore.get(sceneId);
        if (scene == null) return null;

        SceneSnapshotDTO snapshot = new SceneSnapshotDTO();
        snapshot.setSnapshotId("snap-" + UUID.randomUUID().toString().substring(0, 8));
        snapshot.setSceneId(sceneId);
        snapshot.setVersion(scene.getVersion());
        snapshot.setCreateTime(System.currentTimeMillis());
        snapshot.setDescription("Auto snapshot");

        Map<String, Object> data = new HashMap<>();
        data.put("name", scene.getName());
        data.put("type", scene.getType());
        data.put("config", scene.getConfig());
        snapshot.setData(data);

        List<SceneSnapshotDTO> snapshots = snapshotStore.get(sceneId);
        if (snapshots != null) {
            snapshots.add(snapshot);
        }

        return snapshot;
    }

    @Override
    public boolean restoreSnapshot(String sceneId, SceneSnapshotDTO snapshot) {
        SceneDefinitionDTO scene = sceneStore.get(sceneId);
        if (scene == null || snapshot == null) return false;

        Map<String, Object> data = snapshot.getData();
        if (data != null) {
            if (data.containsKey("name")) scene.setName((String) data.get("name"));
            if (data.containsKey("type")) scene.setType((String) data.get("type"));
            if (data.containsKey("config")) scene.setConfig((Map<String, Object>) data.get("config"));
            scene.setUpdateTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    private <T> PageResult<T> paginate(List<T> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        if (start >= total) return PageResult.empty();
        return PageResult.of(list.subList(start, end), total, pageNum, pageSize);
    }
}
