package controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import service.IOrderService;
import service.ICartService;
import service.IUserService;
import repository.OrderItemRepository;
import repository.ProductImageRepository;

import java.math.BigDecimal;
import java.util.List;
import entity.CartItem;
import entity.OrderItem;
import entity.ProductImage;

@Controller
@RequestMapping("/order")
public class OrderController {
	private final IOrderService orderService;
	private final ICartService cartService;
	private final IUserService userService;
	private final OrderItemRepository orderItemRepository;
	private final ProductImageRepository productImageRepository;

	public OrderController(IOrderService s, ICartService cartService, IUserService userService, OrderItemRepository orderItemRepository, ProductImageRepository productImageRepository) {
		this.orderService = s;
		this.cartService = cartService;
		this.userService = userService;
		this.orderItemRepository = orderItemRepository;
		this.productImageRepository = productImageRepository;
	}

	@GetMapping("/checkout")
	public String showCheckout(Authentication auth, Model m) {
		if (auth == null || !auth.isAuthenticated()) {
			return "redirect:/auth/login";
		}
		
		Long userId = userService.findByUsername(auth.getName()).orElseThrow().getId();
		List<CartItem> cartItems = cartService.list(userId);
		
		BigDecimal cartTotal = cartItems.stream()
			.map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		m.addAttribute("items", cartItems);
		m.addAttribute("cartTotal", cartTotal);
		
		if (cartItems.isEmpty()) {
			return "redirect:/cart";
		}
		
		return "order/checkout";
	}

	@PostMapping("/checkout")
	public String checkout(
			Authentication auth,
			@RequestParam String fullName,
			@RequestParam String phone,
			@RequestParam String address,
			@RequestParam(required = false) String province,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String district,
			@RequestParam(required = false) String districtName,
			@RequestParam(required = false) String ward,
			@RequestParam(required = false) String notes,
			@RequestParam String paymentMethod,
			Model m) {
		
		if (auth == null || !auth.isAuthenticated()) {
			return "redirect:/auth/login";
		}
		
		Long userId = userService.findByUsername(auth.getName()).orElseThrow().getId();
		
		List<CartItem> cartItems = cartService.list(userId);
		if (cartItems.isEmpty()) {
			return "redirect:/cart";
		}
		
		System.out.println("Checkout address info:");
		System.out.println("  Address: " + address);
		System.out.println("  Province: " + province);
		System.out.println("  City: " + city);
		System.out.println("  District: " + (districtName != null ? districtName : district));
		System.out.println("  Ward: " + ward);
		
		var order = orderService.placeOrder(userId, paymentMethod);
		
		return "redirect:/order/" + order.getId() + "/success?new=true";
	}
	
	@GetMapping("/history")
	public String orderHistory(Authentication auth, Model m) {
		if (auth == null || !auth.isAuthenticated()) {
			return "redirect:/auth/login";
		}
		
		Long userId = userService.findByUsername(auth.getName()).orElseThrow().getId();
		var orders = orderService.userOrders(userId);
		orders.forEach(order -> {
			if (order.getCancelReason() != null) {
				order.getCancelReason();
			}
		});
		m.addAttribute("orders", orders);
		return "order/history";
	}
	
	@GetMapping("/{orderId}/success")
	public String orderSuccess(@PathVariable Long orderId, 
	                          @RequestParam(required = false) Boolean newOrder,
	                          Authentication auth, Model m) {
		if (auth == null || !auth.isAuthenticated()) {
			return "redirect:/auth/login";
		}
		
		Long userId = userService.findByUsername(auth.getName()).orElseThrow().getId();
		var order = orderService.findById(orderId).orElseThrow();
		
		if (!order.getUser().getId().equals(userId)) {
			return "redirect:/";
		}
		
		var user = order.getUser();
		if (user != null) {
			user.getHoTen();
			user.getEmail();
			user.getSoDienThoai();
			user.getDiaChi();
		}
		
		if (order.getCancelReason() != null) {
			order.getCancelReason();
		}
		
		List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
		
		System.out.println("Loaded " + orderItems.size() + " order items for order " + orderId);
		orderItems.forEach(item -> {
			System.out.println("OrderItem ID: " + item.getId() + ", Price: " + item.getPrice() + ", Quantity: " + item.getQuantity());
		});
		
		orderItems.forEach(item -> {
			BigDecimal price = item.getPrice();
			
			if (price == null && item.getProduct() != null) {
				price = item.getProduct().getPrice();
				if (price != null) {
					item.setPrice(price);
					System.out.println("Warning: OrderItem " + item.getId() + " had null price, using product price: " + price);
				}
			}
			
			// Ensure quantity is accessed
			item.getQuantity();
			
			if (item.getProduct() != null) {
				// Touch product fields to ensure they are loaded
				item.getProduct().getName();
				item.getProduct().getImageUrl();
				// Load product images
				List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(item.getProduct().getId());
				item.getProduct().setImages(images);
			}
		});
		
		// Access totalAmount and createdAt to ensure they are loaded
		BigDecimal totalAmount = order.getTotalAmount();
		order.getCreatedAt();
		
		// If totalAmount is null, zero, or seems incorrect, recalculate from order items
		if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
			BigDecimal calculatedTotal = orderItems.stream()
				.map(item -> {
					BigDecimal price = item.getPrice();
					if (price == null) {
						price = BigDecimal.ZERO;
					}
					return price.multiply(BigDecimal.valueOf(item.getQuantity()));
				})
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			if (calculatedTotal.compareTo(BigDecimal.ZERO) > 0) {
				order.setTotalAmount(calculatedTotal);
			}
		}
		
		// Access paymentMethod to ensure it's loaded
		if (order.getPaymentMethod() != null) {
			order.getPaymentMethod();
		}
		
		m.addAttribute("order", order);
		m.addAttribute("orderItems", orderItems);
		// Only show countdown notification if this is a new order (redirected from checkout) and not cancelled
		m.addAttribute("showCountdown", newOrder != null && newOrder && !"CANCELLED".equals(order.getStatus()));
		return "order/success";
	}
}
