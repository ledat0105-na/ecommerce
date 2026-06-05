package repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import entity.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.product WHERE oi.order.id = :orderId")
  List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

  // đổ item từ cart sang order_items (snapshot giá)
  @Transactional @Modifying
  @Query(value = """
      INSERT INTO order_items(order_id, product_id, quantity, price, size)
      SELECT :orderId, ci.product_id, ci.quantity, p.price, ci.size
      FROM cart_items ci
      JOIN products p ON p.id = ci.product_id
      WHERE ci.user_id = :userId
  """, nativeQuery = true)
  int insertFromCart(@Param("orderId") Long orderId, @Param("userId") Long userId);
}
