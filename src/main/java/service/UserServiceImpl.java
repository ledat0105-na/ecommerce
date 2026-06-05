package service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import entity.User;
import repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public User registerUser(String username, String email, String rawPassword) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setVaiTro(entity.User.VaiTro.customer); 
        u.setEnabled(true);
        return repo.save(u);
    }

    @Override
    public User registerAdmin(String username, String email, String rawPassword) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setVaiTro(entity.User.VaiTro.admin); // Set directly to database field
        u.setEnabled(true);
        return repo.save(u);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }
    
    @Override
    public List<User> findAllUsers() {
        return repo.findAll();
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }
    
    @Override
    @Transactional
    public void updateUser(User user) {
        User existingUser = repo.findById(user.getId()).orElseThrow();
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setVaiTro(user.getVaiTro());
        existingUser.setEnabled(user.isEnabled());
        existingUser.setHoTen(user.getHoTen());
        existingUser.setSoDienThoai(user.getSoDienThoai());
        existingUser.setDiaChi(user.getDiaChi());
        // removed location update
        repo.save(existingUser);
    }
    
    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = repo.findById(userId).orElseThrow();
        user.setEnabled(!user.isEnabled());
        if (!user.isEnabled()) {
            user.setLockedAt(java.time.LocalDateTime.now());
        } else {
            user.setLockReason(null);
            user.setLockedAt(null);
        }
        repo.save(user);
    }
    
    @Override
    @Transactional
    public void lockUser(Long userId, String lockReason) {
        User user = repo.findById(userId).orElseThrow();
        user.setEnabled(false);
        user.setLockReason(lockReason);
        user.setLockedAt(java.time.LocalDateTime.now());
        repo.save(user);
    }
    
    @Override
    @Transactional
    public void unlockUser(Long userId) {
        User user = repo.findById(userId).orElseThrow();
        user.setEnabled(true);
        user.setLockReason(null);
        user.setLockedAt(null);
        repo.save(user);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        repo.deleteById(userId);
    }

    @Override
    public User registerUserWithProfile(String username, String email, String password, String fullName, Integer birthYear,
            String gender) {
        return registerUserWithNewProfile(username, email, password, fullName, null, null, null, null, null, null, null, null);
    }

    @Override
    public User registerUserWithNewProfile(String username, String email, String password,
                                           String fullName, String phone, String address, Integer locationId,
                                           byte[] avatarBytes,
                                           String cvFileName, String cvContentType, byte[] cvBytes,
                                           String avatarUrl) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        // Set role directly to database field
        u.setVaiTro(entity.User.VaiTro.customer);
        u.setEnabled(true);
        u.setHoTen(fullName);
        u.setSoDienThoai(phone);
        u.setDiaChi(address);
        // ignore locationId since vi_tri removed
        if (avatarBytes != null && avatarBytes.length > 0) {
            u.setAvatar(avatarBytes);
        }
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            u.setAvatarUrl(avatarUrl);
        }
        if (cvBytes != null && cvBytes.length > 0) {
            u.setCvFileName(cvFileName);
            u.setCvContentType(cvContentType);
            u.setCvData(cvBytes);
        }
        return repo.save(u);
    }
}
