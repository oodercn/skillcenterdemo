package net.ooder.skillcenter.dto;

import java.util.Date;

/**
 * 托管实例数据传输对象
 */
public class HostingInstanceDTO {

    private String id;
    private String name;
    private String skillId;
    private String skillName;
    private String status;
    private Date deployedAt;
    private String uptime;
    private String description;
    private String url;
    private int port;
    private String version;

    public HostingInstanceDTO() {}

    public HostingInstanceDTO(String id, String name, String skillId, String status) {
        this.id = id;
        this.name = name;
        this.skillId = skillId;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDeployedAt() { return deployedAt; }
    public void setDeployedAt(Date deployedAt) { this.deployedAt = deployedAt; }

    public String getUptime() { return uptime; }
    public void setUptime(String uptime) { this.uptime = uptime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
