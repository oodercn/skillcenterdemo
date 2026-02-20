package net.ooder.skillcenter.sdk;

import net.ooder.sdk.api.scene.SceneManager;
import net.ooder.sdk.api.scene.SceneDefinition;
import net.ooder.sdk.api.scene.SceneSnapshot;
import net.ooder.sdk.api.skill.Capability;
import net.ooder.skillcenter.config.SdkConfig;
import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.scene.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
public class SceneSdkAdapterImpl implements SceneSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(SceneSdkAdapterImpl.class);

    @Autowired
    private SdkConfig sdkConfig;

    @Autowired
    private SceneSdkAdapterMockImpl mockAdapter;

    @Autowired
    private AgentSDKWrapper sdkWrapper;

    private SceneManager sceneManager;
    private boolean sdkAvailable = false;

    @PostConstruct
    public void init() {
        if (sdkConfig.isMockMode()) {
            log.info("[SceneSdkAdapter] Running in mock mode");
            return;
        }

        log.info("[SceneSdkAdapter] Checking SDK availability...");
        sdkAvailable = checkSdkAvailability();

        if (sdkAvailable) {
            log.info("[SceneSdkAdapter] SDK is available, using real implementation");
        } else {
            log.warn("[SceneSdkAdapter] SDK scene APIs not available, falling back to mock");
        }
    }

    private boolean checkSdkAvailability() {
        if (sdkWrapper != null && sdkWrapper.isInitialized()) {
            sceneManager = sdkWrapper.getSceneManager();
            return sceneManager != null;
        }
        return false;
    }

    @Override
    public SceneDefinitionDTO createScene(SceneDefinitionDTO definition) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.createScene(definition);
        }

        log.debug("[SceneSdkAdapter] Creating scene via SDK: {}", definition.getName());
        try {
            SceneDefinition sdkDef = new SceneDefinition();
            sdkDef.setName(definition.getName());
            sdkDef.setDescription(definition.getDescription());
            
            SceneDefinition created = sceneManager.create(sdkDef).get();
            SceneDefinitionDTO result = new SceneDefinitionDTO();
            result.setName(created.getName());
            result.setDescription(created.getDescription());
            return result;
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to create scene: {}", e.getMessage());
            return mockAdapter.createScene(definition);
        }
    }

    @Override
    public boolean deleteScene(String sceneId) {
        return mockAdapter.deleteScene(sceneId);
    }

    @Override
    public SceneDefinitionDTO getScene(String sceneId) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.getScene(sceneId);
        }

        log.debug("[SceneSdkAdapter] Getting scene via SDK: {}", sceneId);
        try {
            SceneDefinition scene = sceneManager.get(sceneId).get();
            if (scene == null) return null;
            SceneDefinitionDTO result = new SceneDefinitionDTO();
            result.setName(scene.getName());
            result.setDescription(scene.getDescription());
            return result;
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to get scene: {}", e.getMessage());
            return mockAdapter.getScene(sceneId);
        }
    }

    @Override
    public PageResult<SceneDefinitionDTO> listScenes(int pageNum, int pageSize) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.listScenes(pageNum, pageSize);
        }

        log.debug("[SceneSdkAdapter] Listing scenes via SDK: page={}, size={}", pageNum, pageSize);
        try {
            List<SceneDefinition> scenes = sceneManager.listAll().get();
            List<SceneDefinitionDTO> dtos = scenes.stream()
                .map(scene -> {
                    SceneDefinitionDTO dto = new SceneDefinitionDTO();
                    dto.setName(scene.getName());
                    dto.setDescription(scene.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
            return paginate(dtos, pageNum, pageSize);
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to list scenes: {}", e.getMessage());
            return mockAdapter.listScenes(pageNum, pageSize);
        }
    }

    @Override
    public boolean activateScene(String sceneId) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.activateScene(sceneId);
        }

        log.debug("[SceneSdkAdapter] Activating scene via SDK: {}", sceneId);
        try {
            sceneManager.activate(sceneId).get();
            log.info("[SceneSdkAdapter] Scene activated: {}", sceneId);
            return true;
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to activate scene: {}", e.getMessage());
            return mockAdapter.activateScene(sceneId);
        }
    }

    @Override
    public boolean deactivateScene(String sceneId) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.deactivateScene(sceneId);
        }

        log.debug("[SceneSdkAdapter] Deactivating scene via SDK: {}", sceneId);
        try {
            sceneManager.deactivate(sceneId).get();
            log.info("[SceneSdkAdapter] Scene deactivated: {}", sceneId);
            return true;
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to deactivate scene: {}", e.getMessage());
            return mockAdapter.deactivateScene(sceneId);
        }
    }

    @Override
    public SceneStateDTO getSceneState(String sceneId) {
        return mockAdapter.getSceneState(sceneId);
    }

    @Override
    public boolean addCapability(String sceneId, CapabilityDTO capability) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.addCapability(sceneId, capability);
        }

        log.debug("[SceneSdkAdapter] Adding capability via SDK: {} to scene {}", capability.getCapId(), sceneId);
        try {
            Capability sdkCap = new Capability();
            sdkCap.setCapId(capability.getCapId());
            sdkCap.setName(capability.getName());
            sceneManager.addCapability(sceneId, sdkCap).get();
            return true;
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to add capability: {}", e.getMessage());
            return mockAdapter.addCapability(sceneId, capability);
        }
    }

    @Override
    public boolean removeCapability(String sceneId, String capabilityId) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.removeCapability(sceneId, capabilityId);
        }

        log.debug("[SceneSdkAdapter] Removing capability via SDK: {} from scene {}", capabilityId, sceneId);
        try {
            sceneManager.removeCapability(sceneId, capabilityId).get();
            return true;
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to remove capability: {}", e.getMessage());
            return mockAdapter.removeCapability(sceneId, capabilityId);
        }
    }

    @Override
    public PageResult<CapabilityDTO> listCapabilities(String sceneId, int pageNum, int pageSize) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.listCapabilities(sceneId, pageNum, pageSize);
        }

        log.debug("[SceneSdkAdapter] Listing capabilities via SDK for scene: {}", sceneId);
        try {
            List<Capability> capabilities = sceneManager.listCapabilities(sceneId).get();
            List<CapabilityDTO> dtos = capabilities.stream()
                .map(cap -> {
                    CapabilityDTO dto = new CapabilityDTO();
                    dto.setCapId(cap.getCapId());
                    dto.setName(cap.getName());
                    dto.setDescription(cap.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
            return paginate(dtos, pageNum, pageSize);
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to list capabilities: {}", e.getMessage());
            return mockAdapter.listCapabilities(sceneId, pageNum, pageSize);
        }
    }

    @Override
    public CapabilityDTO getCapability(String sceneId, String capabilityId) {
        if (!sdkAvailable || sceneManager == null) {
            return mockAdapter.getCapability(sceneId, capabilityId);
        }

        log.debug("[SceneSdkAdapter] Getting capability via SDK: {} from scene {}", capabilityId, sceneId);
        try {
            Capability cap = sceneManager.getCapability(sceneId, capabilityId).get();
            if (cap == null) return null;
            CapabilityDTO dto = new CapabilityDTO();
            dto.setCapId(cap.getCapId());
            dto.setName(cap.getName());
            dto.setDescription(cap.getDescription());
            return dto;
        } catch (Exception e) {
            log.error("[SceneSdkAdapter] Failed to get capability: {}", e.getMessage());
            return mockAdapter.getCapability(sceneId, capabilityId);
        }
    }

    @Override
    public boolean addRole(String sceneId, SceneRoleDTO role) {
        return mockAdapter.addRole(sceneId, role);
    }

    @Override
    public boolean removeRole(String sceneId, String roleId) {
        return mockAdapter.removeRole(sceneId, roleId);
    }

    @Override
    public PageResult<SceneRoleDTO> listRoles(String sceneId, int pageNum, int pageSize) {
        return mockAdapter.listRoles(sceneId, pageNum, pageSize);
    }

    @Override
    public SceneSnapshotDTO createSnapshot(String sceneId) {
        return mockAdapter.createSnapshot(sceneId);
    }

    @Override
    public boolean restoreSnapshot(String sceneId, String snapshotId) {
        return mockAdapter.restoreSnapshot(sceneId, snapshotId);
    }

    @Override
    public PageResult<SceneSnapshotDTO> listSnapshots(String sceneId, int pageNum, int pageSize) {
        return mockAdapter.listSnapshots(sceneId, pageNum, pageSize);
    }

    @Override
    public boolean deleteSnapshot(String sceneId, String snapshotId) {
        return mockAdapter.deleteSnapshot(sceneId, snapshotId);
    }

    @Override
    public boolean isAvailable() {
        return sdkAvailable || mockAdapter.isAvailable();
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
