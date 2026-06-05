package controller;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import entity.Product;
import repository.ProductImageRepository;
import service.IProductService;
import service.ICartService;

import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

	private final IProductService service;
	private final ICartService cartService;
	private final ProductImageRepository productImageRepository;

	public ProductController(IProductService service, ICartService cartService, ProductImageRepository productImageRepository) {
		this.service = service;
		this.cartService = cartService;
		this.productImageRepository = productImageRepository;
	}

	@GetMapping("/list")
	public String list(@RequestParam(required = false) String q, 
			@RequestParam(required = false) String category,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "8") int size, Model model, Authentication auth) {
		Page<Product> p;
		if (category != null && !category.isEmpty()) {
			p = service.findByCategory(category, page, size);
		} else {
			p = service.pageSearch(q, page, size);
		}
		
		List<Product> products = p.getContent();
		products.forEach(product -> {
			List<entity.ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
			product.setImages(images);
		});
		
		model.addAttribute("page", p);
		model.addAttribute("products", products);
		model.addAttribute("q", q);
		model.addAttribute("category", category);
		model.addAttribute("size", size);
		model.addAttribute("categories", service.findAllCategories());
		
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
			try {
				int cartItemCount = cartService.getCartItemCount(auth.getName());
				model.addAttribute("cartItemCount", cartItemCount);
			} catch (Exception e) {
				model.addAttribute("cartItemCount", 0);
			}
		} else {
			model.addAttribute("cartItemCount", 0);
		}
		
		return "product/list";
	}

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable Long id, Model model, Authentication auth) {
		Optional<Product> productOpt = service.findById(id);
		if (productOpt.isEmpty()) {
			return "redirect:/product/list";
		}
		Product product = productOpt.get();
		
		List<entity.ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(id);
		product.setImages(images);
		
		model.addAttribute("product", product);
		
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
			try {
				int cartItemCount = cartService.getCartItemCount(auth.getName());
				model.addAttribute("cartItemCount", cartItemCount);
			} catch (Exception e) {
				model.addAttribute("cartItemCount", 0);
			}
		} else {
			model.addAttribute("cartItemCount", 0);
		}
		
		return "product/detail";
	}
}
