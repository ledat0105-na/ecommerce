package repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	List<CartItem> findByUserId(Long userId);

	Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
	
	// Query tùy chỉnh để xử lý NULL size đúng cách (sử dụng native query)
	@Query(value = "SELECT * FROM cart_items WHERE user_id = :userId AND product_id = :productId AND " +
	       "((:size IS NULL AND size IS NULL) OR (:size IS NOT NULL AND size = :size))", 
	       nativeQuery = true)
	Optional<CartItem> findByUserIdAndProductIdAndSize(@Param("userId") Long userId, 
	                                                     @Param("productId") Long productId, 
	                                                     @Param("size") String size);

	@Transactional
	@Modifying
	void deleteByUserId(Long userId);

	// upsert cộng số lượng ngay tại DB (MySQL/H2 MODE=MySQL)
	@Transactional
	@Modifying
	@Query(value = """
			    INSERT INTO cart_items(user_id, product_id, quantity)
			    VALUES (:userId, :productId, :qty)
			    ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)
			""", nativeQuery = true)
	int upsertAddQuantity(@Param("userId") Long userId, @Param("productId") Long productId, @Param("qty") int qty);
}
