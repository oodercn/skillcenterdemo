package net.ooder.skillcenter.dto;

import java.util.Date;

/**
 * 技能数据传输对象
 */
public class SkillDTO {
    
    private String id;
    private String name;
    private String description;
    private String category;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String version;
    private String author;
    private boolean available;
    private int downloadCount;
    private double rating;
    
    public SkillDTO() {}
    
    public SkillDTO(String id, String name, String description, String category, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    
    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
