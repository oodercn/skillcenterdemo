package net.ooder.skillcenter.dto.scene;

import java.util.List;

public class VfsPermissionDTO {
    private String agentId;
    private String sceneGroupId;
    private List<String> readablePaths;
    private List<String> writablePaths;
    private boolean fullAccess;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getSceneGroupId() { return sceneGroupId; }
    public void setSceneGroupId(String sceneGroupId) { this.sceneGroupId = sceneGroupId; }
    public List<String> getReadablePaths() { return readablePaths; }
    public void setReadablePaths(List<String> readablePaths) { this.readablePaths = readablePaths; }
    public List<String> getWritablePaths() { return writablePaths; }
    public void setWritablePaths(List<String> writablePaths) { this.writablePaths = writablePaths; }
    public boolean isFullAccess() { return fullAccess; }
    public void setFullAccess(boolean fullAccess) { this.fullAccess = fullAccess; }
}
