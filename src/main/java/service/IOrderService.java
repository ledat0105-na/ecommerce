package service;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

import entity.Order;

public interface IOrderService {
	Order placeOrder(Long userId);
	Order placeOrder(Long userId, String paymentMethod);

	List<Order> userOrders(Long userId);
	
	// Admin methods
	List<Order> findAllOrders();
	
	List<Order> findOrdersByStatus(String status);
	
	List<Order> searchOrders(String keyword, String status);
	
	// Pagination methods
	Page<Order> findAllOrdersPageable(int page, int size);
	
	Page<Order> findOrdersByStatusPageable(String status, int page, int size);
	
	Page<Order> searchOrdersPageable(String keyword, String status, int page, int size);
	
	long countOrdersByStatus(String status);
	
	Optional<Order> findById(Long id);
	
	void updateOrderStatus(Long orderId, String status);
	
	void cancelOrder(Long orderId, String reason);
	
	void deleteOrder(Long orderId);
}
