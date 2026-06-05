package repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
  
  List<Order> findAllByOrderByCreatedAtDesc();
  
  // Pagination methods
  Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
  
  Page<Order> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
  
  List<Order> findByStatusOrderByCreatedAtDesc(String status);
  
  @Query(value = "SELECT o.* FROM orders o " +
                 "LEFT JOIN users u ON o.user_id = u.id " +
                 "WHERE (:status IS NULL OR :status = 'ALL' OR o.status = :status) " +
                 "AND (:keyword IS NULL OR :keyword = '' OR " +
                 "     u.ten_dang_nhap LIKE CONCAT('%', :keyword, '%') OR " +
                 "     u.ho_ten LIKE CONCAT('%', :keyword, '%') OR " +
                 "     CAST(o.id AS CHAR) LIKE CONCAT('%', :keyword, '%')) " +
                 "ORDER BY o.created_at DESC", nativeQuery = true)
  List<Order> searchOrders(@Param("keyword") String keyword, @Param("status") String status);
  
  @Query(value = "SELECT o.* FROM orders o " +
                 "LEFT JOIN users u ON o.user_id = u.id " +
                 "WHERE (:status IS NULL OR :status = 'ALL' OR o.status = :status) " +
                 "AND (:keyword IS NULL OR :keyword = '' OR " +
                 "     u.ten_dang_nhap LIKE CONCAT('%', :keyword, '%') OR " +
                 "     u.ho_ten LIKE CONCAT('%', :keyword, '%') OR " +
                 "     CAST(o.id AS CHAR) LIKE CONCAT('%', :keyword, '%')) " +
                 "ORDER BY o.created_at DESC", 
         countQuery = "SELECT COUNT(*) FROM orders o " +
                      "LEFT JOIN users u ON o.user_id = u.id " +
                      "WHERE (:status IS NULL OR :status = 'ALL' OR o.status = :status) " +
                      "AND (:keyword IS NULL OR :keyword = '' OR " +
                      "     u.ten_dang_nhap LIKE CONCAT('%', :keyword, '%') OR " +
                      "     u.ho_ten LIKE CONCAT('%', :keyword, '%') OR " +
                      "     CAST(o.id AS CHAR) LIKE CONCAT('%', :keyword, '%'))",
         nativeQuery = true)
  Page<Order> searchOrdersPageable(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
  
  long countByStatus(String status);

  @Transactional @Modifying
  @Query(value = """
      INSERT INTO orders(user_id, total_amount, status, created_at)
      SELECT :userId,
             COALESCE((SELECT SUM(p.price * ci.quantity) 
                       FROM cart_items ci 
                       JOIN products p ON p.id = ci.product_id 
                       WHERE ci.user_id = :userId), 0.00),
             'NEW',
             NOW()
  """, nativeQuery = true)
  int createOrderFromCart(@Param("userId") Long userId);

  @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
  Long lastInsertId();
}

