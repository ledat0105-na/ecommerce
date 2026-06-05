package service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import entity.Order;
import entity.User;
import entity.CartItem;
import repository.CartItemRepository;
import repository.OrderItemRepository;
import repository.OrderRepository;
import repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements IOrderService {

	private final OrderRepository orderRepo;
	private final OrderItemRepository oiRepo;
	private final CartItemRepository cartRepo;
	private final UserRepository userRepo;

	public OrderServiceImpl(OrderRepository o, OrderItemRepository oi, CartItemRepository c, UserRepository userRepo) {
		this.orderRepo = o;
		this.oiRepo = oi;
		this.cartRepo = c;
		this.userRepo = userRepo;
	}

	@Override
	@Transactional
	public Order placeOrder(Long userId) {
		return placeOrder(userId, null);
	}

	@Override
	@Transactional
	public Order placeOrder(Long userId, String paymentMethod) {
		// Get user
		User user = userRepo.findById(userId).orElseThrow();
		
		// Get cart items
		List<CartItem> cartItems = cartRepo.findByUserId(userId);
		
		// Check if cart is empty
		if (cartItems.isEmpty()) {
			throw new IllegalStateException("Cannot place order with empty cart");
		}
		
		// Calculate total amount
		BigDecimal totalAmount = cartItems.stream()
			.map(item -> {
				BigDecimal price = item.getProduct().getPrice();
				if (price == null) {
					price = BigDecimal.ZERO;
				}
				return price.multiply(BigDecimal.valueOf(item.getQuantity()));
			})
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		// Ensure totalAmount is not null and positive
		if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
			totalAmount = BigDecimal.ZERO;
		}
		
		// Create order using JPA entity (not native query)
		Order order = new Order();
		order.setUser(user);
		order.setTotalAmount(totalAmount); // Set totalAmount explicitly
		order.setStatus("NEW");
		order.setPaymentMethod(paymentMethod);
		order.setCreatedAt(LocalDateTime.now());
		
		// Double-check: Ensure totalAmount is set (should never be null after above)
		if (order.getTotalAmount() == null) {
			order.setTotalAmount(BigDecimal.ZERO);
		}
		
		// Debug: Log before save
		System.out.println("Creating order with totalAmount: " + order.getTotalAmount());
		System.out.println("Order status: " + order.getStatus());
		System.out.println("Order paymentMethod: " + order.getPaymentMethod());
		System.out.println("Order createdAt: " + order.getCreatedAt());
		System.out.println("Order user: " + order.getUser().getId());
		
		order = orderRepo.save(order);
		
		// Create order items from cart items
		oiRepo.insertFromCart(order.getId(), userId);

		// Clear cart
		cartRepo.deleteByUserId(userId);

		return order;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> userOrders(Long userId) {
		return orderRepo.findByUserIdOrderByCreatedAtDesc(userId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Order> findAllOrders() {
		return orderRepo.findAllByOrderByCreatedAtDesc();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Order> findOrdersByStatus(String status) {
		return orderRepo.findByStatusOrderByCreatedAtDesc(status);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Order> searchOrders(String keyword, String status) {
		String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
		String filterStatus = (status != null && !status.trim().isEmpty() && !status.equals("ALL")) ? status : null;
		return orderRepo.searchOrders(searchKeyword, filterStatus);
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countOrdersByStatus(String status) {
		return orderRepo.countByStatus(status);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<Order> findAllOrdersPageable(int page, int size) {
		return orderRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), Math.max(size, 1)));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<Order> findOrdersByStatusPageable(String status, int page, int size) {
		return orderRepo.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(Math.max(page, 0), Math.max(size, 1)));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<Order> searchOrdersPageable(String keyword, String status, int page, int size) {
		String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
		String filterStatus = (status != null && !status.trim().isEmpty() && !status.equals("ALL")) ? status : null;
		return orderRepo.searchOrdersPageable(searchKeyword, filterStatus, PageRequest.of(Math.max(page, 0), Math.max(size, 1)));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<Order> findById(Long id) {
		return orderRepo.findById(id);
	}
	
	@Override
	@Transactional
	public void updateOrderStatus(Long orderId, String status) {
		Order order = orderRepo.findById(orderId).orElseThrow();
		order.setStatus(status);
		orderRepo.save(order);
	}
	
	@Override
	@Transactional
	public void cancelOrder(Long orderId, String reason) {
		Order order = orderRepo.findById(orderId).orElseThrow();
		order.setStatus("CANCELLED");
		order.setCancelReason(reason);
		orderRepo.save(order);
	}
	
	@Override
	@Transactional
	public void deleteOrder(Long orderId) {
		orderRepo.deleteById(orderId);
	}
}
