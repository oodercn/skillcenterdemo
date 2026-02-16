package net.ooder.skillcenter.dto.scene;

import java.util.List;

public class CapabilityDTO {
    private String capId;
    private String name;
    private String description;
    private String type;
    private List<ParameterDTO> parameters;
    private String returnType;
    private String sceneId;
    private String status;

    public String getCapId() { return capId; }
    public void setCapId(String capId) { this.capId = capId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<ParameterDTO> getParameters() { return parameters; }
    public void setParameters(List<ParameterDTO> parameters) { this.parameters = parameters; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    public String getSceneId() { return sceneId; }
    public void setSceneId(String sceneId) { this.sceneId = sceneId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
