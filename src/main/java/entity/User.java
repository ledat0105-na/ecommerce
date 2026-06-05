package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(name = "ten_dang_nhap", nullable = false, unique = true, length = 50)
    private String username;

    @Email
    @Column(name = "email", unique = true, length = 100)
    private String email;

    @NotBlank
    @Column(name = "mat_khau", nullable = false, length = 255)
    private String password;

    @Column(name = "ho_ten", length = 100)
    private String hoTen;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    public enum VaiTro { admin, customer }

    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro")
    private VaiTro vaiTro = VaiTro.customer;

    @CreationTimestamp
    @Column(name = "ngay_tao")
    private java.time.LocalDateTime ngayTao;

    @Transient
    private String role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    
    @Column(name = "lock_reason", length = 500)
    private String lockReason;
    
    @Column(name = "locked_at")
    private java.time.LocalDateTime lockedAt;

    @Lob
    @Column(name = "avatar", columnDefinition = "LONGBLOB")
    private byte[] avatar;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "cv_file_name", length = 255)
    private String cvFileName;

    @Column(name = "cv_content_type", length = 100)
    private String cvContentType;

    @Lob
    @Column(name = "cv_data", columnDefinition = "LONGBLOB")
    private byte[] cvData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String u) {
		this.username = u;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String e) {
		this.email = e;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String p) {
		this.password = p;
	}

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro v) { this.vaiTro = v; }

    public java.time.LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(java.time.LocalDateTime t) { this.ngayTao = t; }

    // no getters/setters for removed field

    // removed filename/contentType/data split fields

    public String getRole() {
        if (role != null) return role;
        return (vaiTro == VaiTro.admin) ? "ROLE_ADMIN" : "ROLE_USER";
    }

    public void setRole(String r) {
        this.role = r;
        if (r != null) {
            this.vaiTro = r.equalsIgnoreCase("ROLE_ADMIN") || r.equalsIgnoreCase("ADMIN")
                    ? VaiTro.admin : VaiTro.customer;
        }
    }
    
    // Method to get role directly from database field for authentication
    public String getRoleFromVaiTro() {
        return (vaiTro == VaiTro.admin) ? "ROLE_ADMIN" : "ROLE_USER";
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { this.enabled = e; }
    
    public String getLockReason() { return lockReason; }
    public void setLockReason(String lockReason) { this.lockReason = lockReason; }
    
    public java.time.LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(java.time.LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public byte[] getAvatar() { return avatar; }
    public void setAvatar(byte[] avatar) { this.avatar = avatar; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCvFileName() { return cvFileName; }
    public void setCvFileName(String cvFileName) { this.cvFileName = cvFileName; }

    public String getCvContentType() { return cvContentType; }
    public void setCvContentType(String cvContentType) { this.cvContentType = cvContentType; }

    public byte[] getCvData() { return cvData; }
    public void setCvData(byte[] cvData) { this.cvData = cvData; }
}
