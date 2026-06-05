package entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 150)
	private String name;
	@Column(length = 1000)
	private String description;
	private String imageUrl;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price = BigDecimal.ZERO;
	@Column(nullable = false)
	private Integer stock = 0;
	private String category;
	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("displayOrder ASC")
	private List<ProductImage> images = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String d) {
		this.description = d;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String i) {
		this.imageUrl = i;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal p) {
		this.price = p;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer s) {
		this.stock = s;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String c) {
		this.category = c;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<ProductImage> getImages() {
		return images;
	}

	public void setImages(List<ProductImage> images) {
		this.images = images;
	}
}
