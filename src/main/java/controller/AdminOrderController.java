package controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import repository.OrderItemRepository;
import service.IOrderService;
import service.INotificationService;
import entity.Order;
import entity.OrderItem;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    
    private final IOrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final INotificationService notificationService;
    
    public AdminOrderController(IOrderService orderService, OrderItemRepository orderItemRepository, INotificationService notificationService) {
        this.orderService = orderService;
        this.orderItemRepository = orderItemRepository;
        this.notificationService = notificationService;
    }
    
    @GetMapping
    public String orders(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        Model model) {
        // Get count of pending orders (NEW status)
        long pendingCount = orderService.countOrdersByStatus("NEW");
        model.addAttribute("pendingCount", pendingCount);
        
        // Search and filter orders with pagination
        Page<Order> orderPage;
        if ((q != null && !q.trim().isEmpty()) || (status != null && !status.trim().isEmpty() && !status.equals("ALL"))) {
            orderPage = orderService.searchOrdersPageable(q != null ? q.trim() : "", status, page, size);
        } else {
            orderPage = orderService.findAllOrdersPageable(page, size);
        }
        
        // Access user fields to prevent LazyInitializationException
        List<Order> orders = orderPage.getContent();
        orders.forEach(order -> {
            if (order.getUser() != null) {
                order.getUser().getUsername();
                order.getUser().getHoTen();
            }
        });
        
        model.addAttribute("page", orderPage);
        model.addAttribute("orders", orders);
        model.addAttribute("q", q);
        model.addAttribute("status", status != null ? status : "ALL");
        model.addAttribute("size", size);
        return "admin/orders";
    }
    
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        Order order = orderService.findById(orderId).orElseThrow();
        
        // Access user fields to prevent LazyInitializationException
        if (order.getUser() != null) {
            order.getUser().getUsername();
            order.getUser().getHoTen();
            order.getUser().getEmail();
            order.getUser().getSoDienThoai();
            order.getUser().getDiaChi();
        }
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        
        // Ensure all fields are loaded for each order item
        orderItems.forEach(item -> {
            // Access price and quantity to ensure they are loaded
            if (item.getPrice() == null && item.getProduct() != null) {
                item.setPrice(item.getProduct().getPrice());
            }
            item.getQuantity();
            item.getSize(); // Access size to ensure it's loaded
            // Access product fields
            if (item.getProduct() != null) {
                item.getProduct().getName();
            }
        });
        
        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        return "admin/order-detail";
    }
    
    @PostMapping("/{orderId}/approve")
    public String approveOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        Order order = orderService.findById(orderId).orElseThrow();
        orderService.updateOrderStatus(orderId, "APPROVED");
        
        // Gửi thông báo cho khách hàng
        String orderCode = String.format("#HD%05d", order.getId());
        notificationService.createNotification(
            order.getUser(),
            order,
            "Đơn hàng đã được duyệt",
            "Đơn hàng " + orderCode + " của bạn đã được duyệt thành công. Chúng tôi sẽ liên hệ với bạn sớm nhất có thể."
        );
        
        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được duyệt thành công và thông báo đã được gửi đến khách hàng!");
        return "redirect:/admin/orders";
    }
    
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId, 
                             @RequestParam String cancelReason,
                             RedirectAttributes redirectAttributes) {
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do hủy đơn hàng!");
            return "redirect:/admin/orders/" + orderId;
        }
        Order order = orderService.findById(orderId).orElseThrow();
        orderService.cancelOrder(orderId, cancelReason.trim());
        
        // Gửi thông báo cho khách hàng
        String orderCode = String.format("#HD%05d", order.getId());
        notificationService.createNotification(
            order.getUser(),
            order,
            "Đơn hàng đã bị hủy",
            "Đơn hàng " + orderCode + " của bạn đã bị hủy. Lý do: " + cancelReason.trim()
        );
        
        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được hủy thành công và thông báo đã được gửi đến khách hàng!");
        return "redirect:/admin/orders";
    }
    
    @PostMapping("/{orderId}/paid")
    public String markAsPaid(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        Order order = orderService.findById(orderId).orElseThrow();
        orderService.updateOrderStatus(orderId, "PAID");
        
        // Gửi thông báo cho khách hàng
        String orderCode = String.format("#HD%05d", order.getId());
        notificationService.createNotification(
            order.getUser(),
            order,
            "Đơn hàng đã thanh toán",
            "Đơn hàng " + orderCode + " của bạn đã được xác nhận thanh toán thành công. Chúng tôi sẽ chuẩn bị và giao hàng sớm nhất có thể."
        );
        
        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được đánh dấu là đã thanh toán và thông báo đã được gửi đến khách hàng!");
        return "redirect:/admin/orders";
    }
    
    @PostMapping("/{orderId}/complete")
    public String completeOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        Order order = orderService.findById(orderId).orElseThrow();
        orderService.updateOrderStatus(orderId, "COMPLETED");
        
        // Gửi thông báo cho khách hàng
        String orderCode = String.format("#HD%05d", order.getId());
        notificationService.createNotification(
            order.getUser(),
            order,
            "Đơn hàng đã hoàn thành",
            "Đơn hàng " + orderCode + " của bạn đã được giao thành công. Cảm ơn bạn đã mua sắm tại cửa hàng của chúng tôi!"
        );
        
        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được đánh dấu là hoàn thành và thông báo đã được gửi đến khách hàng!");
        return "redirect:/admin/orders";
    }
    
    @PostMapping("/{orderId}/notify")
    public String sendNotification(@PathVariable Long orderId,
                                  @RequestParam String title,
                                  @RequestParam String message,
                                  RedirectAttributes redirectAttributes) {
        if (title == null || title.trim().isEmpty() || message == null || message.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ tiêu đề và nội dung thông báo!");
            return "redirect:/admin/orders/" + orderId;
        }
        
        Order order = orderService.findById(orderId).orElseThrow();
        notificationService.createNotification(
            order.getUser(),
            order,
            title.trim(),
            message.trim()
        );
        
        redirectAttributes.addFlashAttribute("successMessage", "Thông báo đã được gửi đến khách hàng thành công!");
        return "redirect:/admin/orders/" + orderId;
    }
}