package service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import entity.Product;
import repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements IProductService {
	private final ProductRepository repo;

	public ProductServiceImpl(ProductRepository repo) {
		this.repo = repo;
	}

	@Override
	public List<Product> findAll() {
		return repo.findAll();
	}

	@Override
	public List<Product> search(String k) {
		return (k == null || k.isBlank()) ? repo.findAll() : repo.findByNameContainingIgnoreCase(k);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Product> findById(Long id) {
		Optional<Product> product = repo.findById(id);
		// Load images to prevent LazyInitializationException
		product.ifPresent(p -> {
			if (p.getImages() != null) {
				p.getImages().size(); // Trigger lazy loading
			}
		});
		return product;
	}

	@Override
	public Product save1(Product p) {
		return repo.save(p);
	}

	@Override
	public void deleteById(Long id) {
		repo.deleteById(id);
	}

	@Override
	@Transactional
	public Product save(Product p) {
		return repo.save(p);
	}
	
	@Override
    public Page<Product> pageAll(int page, int size) {
        return repo.findAll(PageRequest.of(Math.max(page,0), Math.max(size,1)));
    }

    @Override
    public Page<Product> pageSearch(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) return pageAll(page, size);
        return repo.findByNameContainingIgnoreCase(keyword, PageRequest.of(Math.max(page,0), Math.max(size,1)));
    }
    
    @Override
    public List<Product> findTopSellingProducts(int limit) {
        return repo.findTopSellingProducts(PageRequest.of(0, Math.max(limit, 1)));
    }
    
    @Override
    public List<String> findAllCategories() {
        return repo.findDistinctCategories();
    }
    
    @Override
    public Page<Product> findByCategory(String category, int page, int size) {
        return repo.findByCategory(category, PageRequest.of(Math.max(page,0), Math.max(size,1)));
    }
    
    @Override
    public Page<Product> findAllOrderByCreatedAtDesc(int page, int size) {
        return repo.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page,0), Math.max(size,1)));
    }
    
    @Override
    public Page<Product> pageSearchOrderByCreatedAtDesc(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) return findAllOrderByCreatedAtDesc(page, size);
        return repo.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword, PageRequest.of(Math.max(page,0), Math.max(size,1)));
    }
    
    @Override
    public Page<Product> findByCategoryOrderByCreatedAtDesc(String category, int page, int size) {
        return repo.findByCategoryOrderByCreatedAtDesc(category, PageRequest.of(Math.max(page,0), Math.max(size,1)));
    }

}
