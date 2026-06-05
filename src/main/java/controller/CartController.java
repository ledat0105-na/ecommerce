package controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import service.ICartService;
import service.IUserService;

@Controller
@RequestMapping("/cart")
public class CartController {
	private final ICartService cartService;
	private final IUserService userService;

	public CartController(ICartService cartService, IUserService userService) {
		this.cartService = cartService;
		this.userService = userService;
	}

	@GetMapping
	public String view(Authentication auth, Model m) {
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
			return "redirect:/auth/login";
		}
		
		try {
			Long userId = userService.findByUsername(auth.getName())
				.orElseThrow(() -> new RuntimeException("User not found: " + auth.getName()))
				.getId();
			m.addAttribute("items", cartService.list(userId));
			return "cart/view";
		} catch (Exception e) {
			return "redirect:/auth/login?error=UserNotFound";
		}
	}

	@PostMapping("/add/{productId}")
	public String add(Authentication auth, @PathVariable Long productId, 
	                  @RequestParam(defaultValue = "1") int qty,
	                  @RequestParam(required = false) String size) {
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
			return "redirect:/auth/login";
		}
		
		try {
			Long userId = userService.findByUsername(auth.getName())
				.orElseThrow(() -> new RuntimeException("User not found: " + auth.getName()))
				.getId();
			cartService.add(userId, productId, qty, size);
			return "redirect:/cart";
		} catch (Exception e) {
			return "redirect:/auth/login?error=UserNotFound";
		}
	}
	
	@PostMapping("/add-ajax/{productId}")
	@ResponseBody
	public java.util.Map<String, Object> addAjax(Authentication auth, @PathVariable Long productId, 
	                  @RequestParam(defaultValue = "1") int qty,
	                  @RequestParam(required = false) String size) {
		java.util.Map<String, Object> response = new java.util.HashMap<>();
		
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
			response.put("success", false);
			response.put("message", "Vui lòng đăng nhập hoặc đăng ký tài khoản trước khi thêm vào giỏ hàng");
			return response;
		}
		
		try {
			var userOpt = userService.findByUsername(auth.getName());
			if (userOpt.isEmpty()) {
				response.put("success", false);
				response.put("message", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
				return response;
			}
			
			Long userId = userOpt.get().getId();
			cartService.add(userId, productId, qty, size);
			int cartCount = cartService.getCartItemCount(auth.getName());
			response.put("success", true);
			response.put("message", "Đã thêm vào giỏ hàng thành công!");
			response.put("cartCount", cartCount);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			String errorMessage = e.getMessage();
			if (errorMessage == null || errorMessage.isEmpty()) {
				errorMessage = "Có lỗi xảy ra khi thêm vào giỏ hàng. Vui lòng thử lại!";
			}
			response.put("message", errorMessage);
			return response;
		}
	}

	@PostMapping("/remove/{id}")
	public String remove(Authentication auth, @PathVariable Long id) {
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
			return "redirect:/auth/login";
		}
		
		try {
			// Verify user owns this cart item
			var userOpt = userService.findByUsername(auth.getName());
			if (userOpt.isPresent()) {
				cartService.remove(id);
			}
			return "redirect:/cart";
		} catch (Exception e) {
			return "redirect:/cart?error=RemoveFailed";
		}
	}
	
	@PostMapping("/remove-ajax/{id}")
	@ResponseBody
	public java.util.Map<String, Object> removeAjax(Authentication auth, @PathVariable Long id) {
		java.util.Map<String, Object> response = new java.util.HashMap<>();
		
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
			response.put("success", false);
			response.put("message", "Vui lòng đăng nhập để thực hiện thao tác này");
			return response;
		}
		
		try {
			// Verify user owns this cart item
			var userOpt = userService.findByUsername(auth.getName());
			if (userOpt.isPresent()) {
				cartService.remove(id);
				int cartCount = cartService.getCartItemCount(auth.getName());
				response.put("success", true);
				response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng thành công!");
				response.put("cartCount", cartCount);
			} else {
				response.put("success", false);
				response.put("message", "Không tìm thấy thông tin người dùng");
			}
			return response;
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Có lỗi xảy ra: " + e.getMessage());
			return response;
		}
	}
	
	@PostMapping("/update/{id}")
	@ResponseBody
	public String updateQuantity(Authentication auth, @PathVariable Long id, @RequestParam int quantity) {
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
			return "ERROR: Not authenticated";
		}
		
		try {
			if (quantity <= 0) {
				cartService.remove(id);
				return "REMOVED";
			}
			
			cartService.updateQuantity(id, quantity);
			return "SUCCESS";
		} catch (Exception e) {
			return "ERROR: " + e.getMessage();
		}
	}
}
