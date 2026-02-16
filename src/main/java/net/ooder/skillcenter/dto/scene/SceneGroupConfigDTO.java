package net.ooder.skillcenter.dto.scene;

import java.util.Map;

public class SceneGroupConfigDTO {
    
    private String sceneId;
    private int minMembers;
    private int maxMembers;
    private int heartbeatInterval;
    private int heartbeatTimeout;
    private int keyThreshold;
    private Map<String, Object> properties;

    public String getSceneId() { return sceneId; }
    public void setSceneId(String sceneId) { this.sceneId = sceneId; }
    public int getMinMembers() { return minMembers; }
    public void setMinMembers(int minMembers) { this.minMembers = minMembers; }
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
    public int getHeartbeatInterval() { return heartbeatInterval; }
    public void setHeartbeatInterval(int heartbeatInterval) { this.heartbeatInterval = heartbeatInterval; }
    public int getHeartbeatTimeout() { return heartbeatTimeout; }
    public void setHeartbeatTimeout(int heartbeatTimeout) { this.heartbeatTimeout = heartbeatTimeout; }
    public int getKeyThreshold() { return keyThreshold; }
    public void setKeyThreshold(int keyThreshold) { this.keyThreshold = keyThreshold; }
    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }
}
