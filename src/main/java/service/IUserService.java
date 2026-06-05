package service;

import java.util.List;
import java.util.Optional;

import entity.User;

public interface IUserService {
	User registerUser(String username, String email, String rawPassword);

	User registerAdmin(String username, String email, String rawPassword);

	Optional<User> findByUsername(String username);
	
	Optional<User> findByEmail(String email);

	List<User> findAllUsers();
	
	Optional<User> findById(Long id);
	
	void updateUser(User user);
	
	void toggleUserStatus(Long userId);
	
	void lockUser(Long userId, String lockReason);
	
	void unlockUser(Long userId);
	
	void deleteUser(Long userId);

	User registerUserWithProfile(String username, String email, String password, String fullName, Integer birthYear,
            String gender);

    User registerUserWithNewProfile(String username, String email, String password,
                                    String fullName, String phone, String address, Integer locationId,
                                    byte[] avatarBytes,
                                    String cvFileName, String cvContentType, byte[] cvBytes,
                                    String avatarUrl);
}
