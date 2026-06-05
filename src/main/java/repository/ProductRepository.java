package repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByNameContainingIgnoreCase(String keyword);

	Page<Product> findByNameContainingIgnoreCase(String keyword, PageRequest pageRequest);
	
	// Lấy top sản phẩm bán chạy dựa trên tổng số lượng đã bán
	@Query("SELECT p FROM Product p LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
		   "GROUP BY p.id ORDER BY COALESCE(SUM(oi.quantity), 0) DESC")
	List<Product> findTopSellingProducts(PageRequest pageRequest);
	
	// Lấy danh sách categories duy nhất
	@Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.category != '' ORDER BY p.category")
	List<String> findDistinctCategories();
	
	// Lấy sản phẩm theo category
	Page<Product> findByCategory(String category, PageRequest pageRequest);
	
	// Lấy tất cả sản phẩm sắp xếp theo thời gian tạo mới nhất
	Page<Product> findAllByOrderByCreatedAtDesc(PageRequest pageRequest);
	
	// Tìm kiếm sản phẩm sắp xếp theo thời gian tạo mới nhất
	Page<Product> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, PageRequest pageRequest);
	
	// Lấy sản phẩm theo category sắp xếp theo thời gian tạo mới nhất
	Page<Product> findByCategoryOrderByCreatedAtDesc(String category, PageRequest pageRequest);
}
