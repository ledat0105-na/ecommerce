package dto;

import java.time.LocalDateTime;

public class UserUpdateRequestDTO {
    private Long id;
    private Long userId;
    private String newFullName;
    private String newPhone;
    private String newAddress;
    private Integer newLocationId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String adminNote;
    private boolean hasNewAvatar;
    
    // User information
    private String userUsername;
    private String userEmail;
    private String userFullName;
    private String userPhone;
    private String userAddress;
    private String userRole;
    private boolean userEnabled;
    
    public UserUpdateRequestDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getNewFullName() { return newFullName; }
    public void setNewFullName(String newFullName) { this.newFullName = newFullName; }
    
    public String getNewPhone() { return newPhone; }
    public void setNewPhone(String newPhone) { this.newPhone = newPhone; }
    
    public String getNewAddress() { return newAddress; }
    public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
    
    public Integer getNewLocationId() { return newLocationId; }
    public void setNewLocationId(Integer newLocationId) { this.newLocationId = newLocationId; }
    
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
    
    public String getUserUsername() { return userUsername; }
    public void setUserUsername(String userUsername) { this.userUsername = userUsername; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    
    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }
    
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    
    public boolean isUserEnabled() { return userEnabled; }
    public void setUserEnabled(boolean userEnabled) { this.userEnabled = userEnabled; }
}

