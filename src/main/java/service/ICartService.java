package service;

import java.util.List;

import entity.CartItem;

public interface ICartService {
	void add(Long userId, Long productId, int qty);
	
	void add(Long userId, Long productId, int qty, String size);

	List<CartItem> list(Long userId);

	void remove(Long cartItemId);
	
	void updateQuantity(Long cartItemId, int quantity);

	void clear(Long userId);
	
	int getCartItemCount(String username);
}
