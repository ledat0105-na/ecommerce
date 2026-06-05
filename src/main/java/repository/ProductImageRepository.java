package repository;

import entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);
	
	@Modifying
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Query("DELETE FROM ProductImage p WHERE p.product.id = :productId")
	void deleteByProductId(@Param("productId") Long productId);
}

