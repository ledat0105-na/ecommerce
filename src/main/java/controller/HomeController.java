package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import entity.Product;
import repository.ProductImageRepository;
import service.IProductService;
import service.ICartService;

import java.util.List;

@Controller
public class HomeController {

	private final IProductService productService;
	private final ICartService cartService;
	private final ProductImageRepository productImageRepository;

	public HomeController(IProductService productService, ICartService cartService, ProductImageRepository productImageRepository) {
		this.productService = productService;
		this.cartService = cartService;
		this.productImageRepository = productImageRepository;
	}

	@GetMapping({ "/", "/home" })
	public String home(Model model, Authentication auth) {
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
			if (auth.getAuthorities().stream()
					.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
				return "redirect:/admin";
			}
		}
		
		List<Product> products = productService.findAll().stream().limit(20).toList();
		List<Product> topProducts = productService.findTopSellingProducts(3);
		
		products.forEach(product -> {
			List<entity.ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
			product.setImages(images);
		});
		
		topProducts.forEach(product -> {
			List<entity.ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
			product.setImages(images);
		});
		
		model.addAttribute("products", products);
		model.addAttribute("topProducts", topProducts);
		
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
		
		return "index";
	}

	@GetMapping("/favicon.ico")
	public ResponseEntity<Void> favicon() {
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
