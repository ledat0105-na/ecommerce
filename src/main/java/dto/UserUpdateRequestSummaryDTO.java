package dto;

import java.time.LocalDateTime;

public class UserUpdateRequestSummaryDTO {
    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String adminNote;
    private boolean hasNewAvatar;
    private boolean hasNewFullName;
    private boolean hasNewPhone;
    private boolean hasNewAddress;
    
    public UserUpdateRequestSummaryDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    
    public boolean isHasNewAvatar() { return hasNewAvatar; }
    public void setHasNewAvatar(boolean hasNewAvatar) { this.hasNewAvatar = hasNewAvatar; }
    
    public boolean isHasNewFullName() { return hasNewFullName; }
    public void setHasNewFullName(boolean hasNewFullName) { this.hasNewFullName = hasNewFullName; }
    
    public boolean isHasNewPhone() { return hasNewPhone; }
    public void setHasNewPhone(boolean hasNewPhone) { this.hasNewPhone = hasNewPhone; }
    
    public boolean isHasNewAddress() { return hasNewAddress; }
    public void setHasNewAddress(boolean hasNewAddress) { this.hasNewAddress = hasNewAddress; }
}

