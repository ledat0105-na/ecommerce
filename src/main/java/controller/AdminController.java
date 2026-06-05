package controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import entity.Product;
import entity.ProductImage;
import repository.ProductImageRepository;
import service.IProductService;
import service.FileStorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final IProductService productService;
	private final ProductImageRepository productImageRepository;
	private final FileStorageService fileStorageService;

	public AdminController(IProductService s, ProductImageRepository productImageRepository, FileStorageService fileStorageService) {
		this.productService = s;
		this.productImageRepository = productImageRepository;
		this.fileStorageService = fileStorageService;
	}

	@GetMapping("/products")
	public String list(@RequestParam(required = false) String q, 
			@RequestParam(required = false) String category,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, Model m) {
		Page<Product> p;
		if (category != null && !category.isEmpty()) {
			p = productService.findByCategoryOrderByCreatedAtDesc(category, page, size);
		} else {
			p = productService.pageSearchOrderByCreatedAtDesc(q, page, size);
		}
		
		List<Product> products = p.getContent();
		products.forEach(product -> {
			List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
			product.setImages(images);
		});
		
		m.addAttribute("page", p);
		m.addAttribute("products", products);
		m.addAttribute("q", q);
		m.addAttribute("category", category);
		m.addAttribute("size", size);
		return "admin/product-list";
	}

	@GetMapping("/products/new")
	public String formNew(Model m) {
		m.addAttribute("product", new Product());
		return "admin/product-form";
	}

	@GetMapping("/products/edit/{id}")
	public String formEdit(@PathVariable Long id, Model m) {
		Product product = productService.findById(id).orElseThrow();
		List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(id);
		product.setImages(images);
		m.addAttribute("product", product);
		return "admin/product-form";
	}

	@PostMapping("/products")
	public String save(@ModelAttribute Product p,
	                   @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
	                   @RequestParam(value = "imageUrls", required = false) String[] imageUrls,
	                   @RequestParam(value = "existingImageUrls", required = false) String[] existingImageUrls,
	                   RedirectAttributes redirectAttributes) {
		try {
			List<String> allImageUrls = new ArrayList<>();
			
			if (existingImageUrls != null) {
				for (String url : existingImageUrls) {
					if (url != null && !url.trim().isEmpty()) {
						allImageUrls.add(url.trim());
					}
				}
			}
			
			if (imageFiles != null) {
				for (MultipartFile file : imageFiles) {
					if (file != null && !file.isEmpty()) {
						String savedPath = fileStorageService.storeFile(file);
						if (savedPath != null) {
							allImageUrls.add(savedPath);
						}
					}
				}
			}
			
			if (imageUrls != null) {
				for (String url : imageUrls) {
					if (url != null && !url.trim().isEmpty()) {
						allImageUrls.add(url.trim());
					}
				}
			}
			
			if (allImageUrls.size() < 2) {
				redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng cung cấp ít nhất 2 ảnh cho sản phẩm! Hiện tại chỉ có " + allImageUrls.size() + " ảnh.");
				if (p.getId() != null) {
					return "redirect:/admin/products/edit/" + p.getId();
				} else {
					return "redirect:/admin/products/new";
				}
			}
			
			if (allImageUrls.size() > 5) {
				redirectAttributes.addFlashAttribute("errorMessage", "Số lượng ảnh không được vượt quá 5 ảnh! Hiện tại có " + allImageUrls.size() + " ảnh.");
				if (p.getId() != null) {
					return "redirect:/admin/products/edit/" + p.getId();
				} else {
					return "redirect:/admin/products/new";
				}
			}
			
			boolean isUpdate = p.getId() != null;
			
			if (isUpdate) {
				Product existingProduct = productService.findById(p.getId()).orElseThrow();
				
				existingProduct.setName(p.getName() != null ? p.getName().trim() : "");
				existingProduct.setDescription(p.getDescription() != null ? p.getDescription() : "");
				existingProduct.setPrice(p.getPrice() != null ? p.getPrice() : existingProduct.getPrice());
				existingProduct.setStock(p.getStock() != null ? p.getStock() : existingProduct.getStock());
				if (p.getCategory() != null && !p.getCategory().trim().isEmpty()) {
					existingProduct.setCategory(p.getCategory().trim());
				}
				
				existingProduct = productService.save(existingProduct);
				
				List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(existingProduct.getId());
				System.out.println("Deleting " + existingImages.size() + " existing images for product " + existingProduct.getId());
				for (ProductImage image : existingImages) {
					productImageRepository.delete(image);
				}
				productImageRepository.flush();
				
				System.out.println("Adding " + allImageUrls.size() + " new images for product " + existingProduct.getId());
				int order = 0;
				for (String imageUrl : allImageUrls) {
					if (imageUrl != null && !imageUrl.trim().isEmpty()) {
						String trimmedUrl = imageUrl.trim();
						if (trimmedUrl.length() > 1000) {
							trimmedUrl = trimmedUrl.substring(0, 1000);
						}
						
						ProductImage image = new ProductImage();
						image.setProduct(existingProduct);
						image.setImageUrl(trimmedUrl);
						image.setDisplayOrder(order++);
						productImageRepository.save(image);
						System.out.println("Saved image " + order + ": " + trimmedUrl);
					}
				}
				productImageRepository.flush();
				
				redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
				return "redirect:/admin/products?updated=true";
			} else {
				Product savedProduct = productService.save(p);
				
				System.out.println("Creating new product with " + allImageUrls.size() + " images");
				int order = 0;
				for (String imageUrl : allImageUrls) {
					if (imageUrl != null && !imageUrl.trim().isEmpty()) {
						String trimmedUrl = imageUrl.trim();
						if (trimmedUrl.length() > 1000) {
							trimmedUrl = trimmedUrl.substring(0, 1000);
						}
						
						ProductImage image = new ProductImage();
						image.setProduct(savedProduct);
						image.setImageUrl(trimmedUrl);
						image.setDisplayOrder(order++);
						productImageRepository.save(image);
						System.out.println("Saved image " + order + ": " + trimmedUrl);
					}
				}
				productImageRepository.flush();
				
				redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
				return "redirect:/admin/products/detail/" + savedProduct.getId();
			}
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu ảnh: " + e.getMessage());
			if (p.getId() != null) {
				return "redirect:/admin/products/edit/" + p.getId();
			} else {
				return "redirect:/admin/products/new";
			}
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			if (p.getId() != null) {
				return "redirect:/admin/products/edit/" + p.getId();
			} else {
				return "redirect:/admin/products/new";
			}
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
			if (p.getId() != null) {
				return "redirect:/admin/products/edit/" + p.getId();
			} else {
				return "redirect:/admin/products/new";
			}
		}
	}

	@GetMapping("/products/detail/{id}")
	public String detail(@PathVariable Long id, Model m) {
		Product product = productService.findById(id).orElseThrow();
		List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(id);
		product.setImages(images);
		
		System.out.println("Product ID: " + id);
		System.out.println("Number of images: " + images.size());
		for (ProductImage img : images) {
			System.out.println("Image URL: " + img.getImageUrl());
		}
		
		m.addAttribute("product", product);
		return "admin/product-detail";
	}

	@PostMapping("/products/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
		productService.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
		}
		return "redirect:/admin/products";
	}
}
