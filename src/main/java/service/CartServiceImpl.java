package service;

import org.springframework.stereotype.Service;

import entity.CartItem;
import entity.Product;
import entity.User;
import repository.CartItemRepository;
import repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements ICartService {
	private final CartItemRepository cartRepo;
	private final UserRepository userRepo;

	public CartServiceImpl(CartItemRepository r, UserRepository userRepo) {
		this.cartRepo = r;
		this.userRepo = userRepo;
	}

	@Override
	public void add(Long userId, Long productId, int qty) {
		add(userId, productId, qty, null);
	}
	
	@Override
	public void add(Long userId, Long productId, int qty, String size) {
		if (qty <= 0)
			qty = 1;
		
		// Normalize size: null hoặc empty string đều được coi là null
		String normalizedSize = (size != null && !size.trim().isEmpty()) ? size.trim() : null;
		
		// Tìm item theo user_id, product_id và size
		Optional<CartItem> existingItem = cartRepo.findByUserIdAndProductIdAndSize(userId, productId, normalizedSize);
		
		if (existingItem.isPresent()) {
			// Nếu đã có item với cùng user, product và size thì cộng số lượng
			CartItem item = existingItem.get();
			item.setQuantity(item.getQuantity() + qty);
			cartRepo.save(item);
		} else {
			// Tạo item mới
			CartItem item = new CartItem();
			User u = new User();
			u.setId(userId);
			item.setUser(u);
			Product p = new Product();
			p.setId(productId);
			item.setProduct(p);
			item.setQuantity(qty);
			item.setSize(normalizedSize);
			cartRepo.save(item);
		}
	}

	@Override
	public List<CartItem> list(Long userId) {
		return cartRepo.findByUserId(userId);
	}

	@Override
	public void remove(Long id) {
		cartRepo.deleteById(id);
	}

	@Override
	public void updateQuantity(Long cartItemId, int quantity) {
		if (quantity <= 0) {
			cartRepo.deleteById(cartItemId);
		} else {
			Optional<CartItem> itemOpt = cartRepo.findById(cartItemId);
			if (itemOpt.isPresent()) {
				CartItem item = itemOpt.get();
				item.setQuantity(quantity);
				cartRepo.save(item);
			}
		}
	}

	@Override
	public void clear(Long userId) {
		cartRepo.deleteByUserId(userId);
	}

	@Override
	public int getCartItemCount(String username) {
		Optional<User> user = userRepo.findByUsername(username);
		if (user.isPresent()) {
			List<CartItem> cartItems = cartRepo.findByUserId(user.get().getId());
			return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
		}
		return 0;
	}
}
