package controller;

import dto.RegisterForm;
import service.IUserService;
import entity.User;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthController(IUserService s, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userService = s;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
	}

	@GetMapping("/login")
	public String login(@RequestParam(required = false) String error, Model model) {
		if (error != null) {
			model.addAttribute("error", error);
		}
		return "auth/login";
	}
	
	@GetMapping("/account-locked")
	public String accountLocked(@RequestParam(required = false) String username, Model model) {
		if (username != null && !username.isEmpty()) {
			Optional<User> userOpt = userService.findByUsername(username);
			if (userOpt.isPresent()) {
				User user = userOpt.get();
				model.addAttribute("user", user);
				model.addAttribute("lockReason", user.getLockReason());
				model.addAttribute("lockedAt", user.getLockedAt());
			}
		}
		return "auth/account-locked";
	}

    @GetMapping("/debug-check")
    @ResponseBody
    public String debugCheck(@RequestParam String username, @RequestParam String raw) {
        return userService.findByUsername(username)
                .map(u -> {
                    boolean ok = passwordEncoder.matches(raw, u.getPassword());
                    return "stored=" + u.getPassword() + "\nraw=" + raw + "\nmatches=" + ok;
                })
                .orElse("user not found");
    }

    // One-time helper: reset password for a user to a raw value (hash with BCrypt)
    @PostMapping("/reset-password")
    @ResponseBody
    public String resetPassword(@RequestParam String username, @RequestParam String raw) {
        return userService.findByUsername(username)
                .map(u -> {
                    u.setPassword(passwordEncoder.encode(raw));
                    userRepository.save(u);
                    return "updated";
                })
                .orElse("user not found");
    }

    // Convenience GET wrapper (dev-only) to reset password quickly from browser
    @GetMapping("/reset-password")
    @ResponseBody
    public String resetPasswordGet(@RequestParam String username, @RequestParam String raw) {
        return userService.findByUsername(username)
                .map(u -> {
                    u.setPassword(passwordEncoder.encode(raw));
                    userRepository.save(u);
                    return "updated";
                })
                .orElse("user not found");
    }

    // DEBUG: Create test user
    @GetMapping("/create-test-user")
    @ResponseBody
    public String createTestUser(@RequestParam String username, @RequestParam String email, @RequestParam String password) {
        try {
            if (userService.findByUsername(username).isPresent()) {
                return "User already exists";
            }
            User user = userService.registerUser(username, email, password);
            return "Created user: " + user.getUsername() + " with role: " + user.getRoleFromVaiTro();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // DEBUG: Create admin user
    @GetMapping("/create-admin-user")
    @ResponseBody
    public String createAdminUser(@RequestParam String username, @RequestParam String email, @RequestParam String password) {
        try {
            if (userService.findByUsername(username).isPresent()) {
                return "User already exists";
            }
            User user = userService.registerAdmin(username, email, password);
            return "Created admin: " + user.getUsername() + " with role: " + user.getRoleFromVaiTro();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // DEBUG: Test login credentials
    @GetMapping("/test-login")
    @ResponseBody
    public String testLogin(@RequestParam String username, @RequestParam String password) {
        try {
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return "User not found: " + username;
            }
            
            User user = userOpt.get();
            boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
            
            return String.format("Username: %s\nPassword match: %s\nRole: %s\nEnabled: %s", 
                username, passwordMatch, user.getRoleFromVaiTro(), user.isEnabled());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/fix-database")
    @ResponseBody
    public String fixDatabase() {
        try {
            // Xóa tất cả user cũ
            userService.findByUsername("admin").ifPresent(user -> userService.deleteUser(user.getId()));
            userService.findByUsername("testuser").ifPresent(user -> userService.deleteUser(user.getId()));
            userService.findByUsername("newadmin").ifPresent(user -> userService.deleteUser(user.getId()));
            userService.findByUsername("user").ifPresent(user -> userService.deleteUser(user.getId()));
            
            // Tạo lại admin user với password 123456
            User admin = userService.registerAdmin("admin", "admin@test.com", "123456");
            
            // Tạo user thông thường với password 123456
            User testUser = userService.registerUser("testuser", "test@test.com", "123456");
            
            return String.format("Database fixed!\nAdmin: %s (role: %s, password: 123456)\nUser: %s (role: %s, password: 123456)", 
                admin.getUsername(), admin.getRoleFromVaiTro(),
                testUser.getUsername(), testUser.getRoleFromVaiTro());
        } catch (Exception e) {
            return "Error fixing database: " + e.getMessage();
        }
    }

	@GetMapping("/register")
	public String showRegister(Model m) {
		m.addAttribute("form", new RegisterForm());
		return "auth/register";
	}

	@PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm f, BindingResult br, Model model, RedirectAttributes ra,
                           @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                           @RequestParam(value = "cv", required = false) MultipartFile cv,
                           @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                           @RequestParam(value = "province", required = false) String province,
                           @RequestParam(value = "district", required = false) String district,
                           @RequestParam(value = "ward", required = false) String ward) {
		System.out.println("=== DEBUG REGISTER ===");
		System.out.println("Username: " + f.getUsername());
		System.out.println("Email: " + f.getEmail());
		System.out.println("Password: " + (f.getPassword() != null ? "***" : "NULL"));
		System.out.println("Role: " + f.getRole());
		System.out.println("Address: " + f.getAddress());
		System.out.println("Province: " + province);
		System.out.println("District: " + district);
		System.out.println("Ward: " + ward);
		System.out.println("BindingResult errors: " + br.hasErrors());
		
		// Build full address if province/district/ward are provided
		if (province != null || district != null || ward != null) {
			StringBuilder fullAddress = new StringBuilder();
			if (f.getAddress() != null && !f.getAddress().trim().isEmpty()) {
				fullAddress.append(f.getAddress().trim());
			}
			if (ward != null && !ward.trim().isEmpty()) {
				if (fullAddress.length() > 0) fullAddress.append(", ");
				fullAddress.append(ward.trim());
			}
			if (district != null && !district.trim().isEmpty()) {
				if (fullAddress.length() > 0) fullAddress.append(", ");
				fullAddress.append(district.trim());
			}
			if (province != null && !province.trim().isEmpty()) {
				if (fullAddress.length() > 0) fullAddress.append(", ");
				fullAddress.append(province.trim());
			}
			// Update address field with full address
			f.setAddress(fullAddress.toString());
			System.out.println("Full address: " + f.getAddress());
		}
		
        if (br.hasErrors()) {
			System.out.println("Validation errors: " + br.getAllErrors());
			model.addAttribute("form", f);
			return "auth/register";
		}

        if (f.getPassword() == null || !f.getPassword().equals(f.getConfirmPassword())) {
            model.addAttribute("form", f);
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "auth/register";
		}
		
        try {
			if (userService.findByUsername(f.getUsername()).isPresent()) {
				System.out.println("Username đã tồn tại: " + f.getUsername());
				model.addAttribute("form", f);
				model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
				return "auth/register";
			}
			
			if (userService.findByEmail(f.getEmail()).isPresent()) {
				System.out.println("Email đã tồn tại: " + f.getEmail());
				model.addAttribute("form", f);
				model.addAttribute("error", "Email đã được sử dụng!");
				return "auth/register";
			}
			
			User savedUser;
			String role = f.getRole() != null ? f.getRole() : "USER";
            if ("ADMIN".equals(role)) {
                System.out.println("Đăng ký ADMIN user...");
                savedUser = userService.registerAdmin(f.getUsername(), f.getEmail(), f.getPassword());
			} else {
                System.out.println("Đăng ký USER user...");
                byte[] avatarBytes = null;
                if (avatar != null && !avatar.isEmpty()) {
                    if (avatar.getSize() > 3 * 1024 * 1024) { // 3MB
                        model.addAttribute("form", f);
                        model.addAttribute("error", "Ảnh vượt quá 3MB. Vui lòng chọn ảnh khác!");
                        return "auth/register";
                    }
                    avatarBytes = avatar.getBytes();
                }
                String cvFileName = null;
                String cvContentType = null;
                byte[] cvBytes = null;
                if (cv != null && !cv.isEmpty()) {
                    if (cv.getSize() > 5 * 1024 * 1024) { // 5MB
                        model.addAttribute("form", f);
                        model.addAttribute("error", "CV vượt quá 5MB. Vui lòng chọn tệp khác!");
                        return "auth/register";
                    }
                    cvFileName = cv.getOriginalFilename();
                    cvContentType = cv.getContentType();
                    cvBytes = cv.getBytes();
                }
                savedUser = userService.registerUserWithNewProfile(
                        f.getUsername(), f.getEmail(), f.getPassword(),
                        f.getFullName(), f.getPhone(), f.getAddress(), f.getLocation(), avatarBytes,
                        cvFileName, cvContentType, cvBytes,
                        avatarUrl
                );
			}
			
            System.out.println("Đăng ký thành công user: " + savedUser.getUsername() + " với ID: " + savedUser.getId());
            ra.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/auth/login?registered";
		} catch (Exception e) {
			System.out.println("Lỗi khi đăng ký: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("form", f);
			model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
			return "auth/register";
		}
	}

	@GetMapping("/welcome")
	public String welcome(Principal p, Model m) {
		m.addAttribute("username", p.getName());
		return "auth/welcome";
	}

	@PostMapping("/create-admin")
	@ResponseBody
	public String createAdmin(@RequestParam String username, @RequestParam String email, @RequestParam String password) {
		try {
			userService.registerAdmin(username, email, password);
			return "Admin account created successfully!";
		} catch (Exception e) {
			return "Error creating admin: " + e.getMessage();
		}
	}
}
