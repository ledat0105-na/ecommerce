package entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_update_requests")
public class UserUpdateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "new_full_name", length = 100)
    private String newFullName;

    @Column(name = "new_phone", length = 20)
    private String newPhone;

    @Column(name = "new_address", length = 255)
    private String newAddress;

    @Column(name = "new_location_id")
    private Integer newLocationId;

    public enum Status { PENDING, APPROVED, REJECTED, COMPLETED }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "admin_note", length = 255)
    private String adminNote;

    @Lob
    @Column(name = "new_avatar", columnDefinition = "LONGBLOB")
    private byte[] newAvatar;

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

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public byte[] getNewAvatar() { return newAvatar; }
    public void setNewAvatar(byte[] newAvatar) { this.newAvatar = newAvatar; }
}
