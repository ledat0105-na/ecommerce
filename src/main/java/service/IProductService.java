package service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import entity.Product;

public interface IProductService {
	List<Product> findAll();

	List<Product> search(String keyword);

	Optional<Product> findById(Long id);

	Product save(Product p);

	void deleteById(Long id);

	Product save1(Product p);

	Page<Product> pageAll(int page, int size);

	Page<Product> pageSearch(String keyword, int page, int size);
	
	// Lấy top sản phẩm bán chạy
	List<Product> findTopSellingProducts(int limit);
	
	// Lấy danh sách categories
	List<String> findAllCategories();
	
	// Lấy sản phẩm theo category
	Page<Product> findByCategory(String category, int page, int size);
	
	// Lấy sản phẩm sắp xếp theo thời gian tạo mới nhất
	Page<Product> findAllOrderByCreatedAtDesc(int page, int size);
	
	// Tìm kiếm sản phẩm sắp xếp theo thời gian tạo mới nhất
	Page<Product> pageSearchOrderByCreatedAtDesc(String keyword, int page, int size);
	
	// Lấy sản phẩm theo category sắp xếp theo thời gian tạo mới nhất
	Page<Product> findByCategoryOrderByCreatedAtDesc(String category, int page, int size);
}
