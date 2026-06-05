package controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import service.IOrderService;
import service.IProductService;
import service.IUserService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    
    private final IProductService productService;
    private final IOrderService orderService;
    private final IUserService userService;
    
    public AdminDashboardController(IProductService productService, IOrderService orderService, IUserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }
    
    @GetMapping
    public String dashboard(Model model) {
        try {
            // Thống kê tổng quan
            long totalProducts = productService.findAll().size();
            long totalOrders = orderService.findAllOrders().size();
            long totalUsers = userService.findAllUsers().size();
            
            // Thống kê đơn hàng theo trạng thái
            long newOrders = orderService.findAllOrders().stream()
                .filter(order -> "NEW".equals(order.getStatus()))
                .count();
            long approvedOrders = orderService.findAllOrders().stream()
                .filter(order -> "APPROVED".equals(order.getStatus()))
                .count();
            long paidOrders = orderService.findAllOrders().stream()
                .filter(order -> "PAID".equals(order.getStatus()))
                .count();
            long cancelledOrders = orderService.findAllOrders().stream()
                .filter(order -> "CANCELLED".equals(order.getStatus()))
                .count();
            
            // Thống kê người dùng theo vai trò
            long totalAdmins = userService.findAllUsers().stream()
                .filter(user -> "ROLE_ADMIN".equals(user.getRole()))
                .count();
            long totalCustomers = userService.findAllUsers().stream()
                .filter(user -> "ROLE_USER".equals(user.getRole()))
                .count();
        
        // Tính tổng doanh thu
        double totalRevenue = 0.0;
        try {
            totalRevenue = orderService.findAllOrders().stream()
                .filter(order -> "PAID".equals(order.getStatus()))
                .mapToDouble(order -> order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                .sum();
        } catch (Exception e) {
            totalRevenue = 0.0;
        }
        
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("newOrders", newOrders);
        model.addAttribute("approvedOrders", approvedOrders);
        model.addAttribute("paidOrders", paidOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalRevenue", totalRevenue);
        
        } catch (Exception e) {
            // Nếu có lỗi, set các giá trị mặc định
            model.addAttribute("totalProducts", 0);
            model.addAttribute("totalOrders", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("newOrders", 0);
            model.addAttribute("approvedOrders", 0);
            model.addAttribute("paidOrders", 0);
            model.addAttribute("cancelledOrders", 0);
            model.addAttribute("totalAdmins", 0);
            model.addAttribute("totalCustomers", 0);
            model.addAttribute("totalRevenue", 0.0);
        }
        
        return "admin/dashboardadmin";
    }
}
