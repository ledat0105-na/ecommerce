package controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import service.INotificationService;
import service.IUserService;
import entity.Notification;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
	
	private final INotificationService notificationService;
	private final IUserService userService;
	
	public NotificationController(INotificationService notificationService, IUserService userService) {
		this.notificationService = notificationService;
		this.userService = userService;
	}
	
	@GetMapping
	public String notifications(Authentication auth, Model m) {
		if (auth == null || !auth.isAuthenticated()) {
			return "redirect:/auth/login";
		}
		
		Long userId = userService.findByUsername(auth.getName()).orElseThrow().getId();
		List<Notification> notifications = notificationService.getUserNotifications(userId);
		
		// Access order fields to prevent LazyInitializationException
		notifications.forEach(notification -> {
			if (notification.getOrder() != null) {
				notification.getOrder().getId();
			}
		});
		
		m.addAttribute("notifications", notifications);
		m.addAttribute("unreadCount", notificationService.getUnreadCount(userId));
		return "notifications/list";
	}
	
	@PostMapping("/{notificationId}/read")
	public String markAsRead(@PathVariable Long notificationId, Authentication auth, RedirectAttributes redirectAttributes) {
		if (auth == null || !auth.isAuthenticated()) {
			return "redirect:/auth/login";
		}
		
		notificationService.markAsRead(notificationId);
		redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu thông báo là đã đọc!");
		return "redirect:/notifications";
	}
	
	@PostMapping("/read-all")
	public String markAllAsRead(Authentication auth, RedirectAttributes redirectAttributes) {
		if (auth == null || !auth.isAuthenticated()) {
			return "redirect:/auth/login";
		}
		
		Long userId = userService.findByUsername(auth.getName()).orElseThrow().getId();
		notificationService.markAllAsRead(userId);
		redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu tất cả thông báo là đã đọc!");
		return "redirect:/notifications";
	}
	
	@PostMapping("/{notificationId}/delete")
	public String deleteNotification(@PathVariable Long notificationId, Authentication auth, RedirectAttributes redirectAttributes) {
		if (auth == null || !auth.isAuthenticated()) {
			return "redirect:/auth/login";
		}
		
		notificationService.deleteNotification(notificationId);
		redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thông báo!");
		return "redirect:/notifications";
	}
	
	@GetMapping("/unread-count")
	@ResponseBody
	public Long getUnreadCount(Authentication auth) {
		if (auth == null || !auth.isAuthenticated()) {
			return 0L;
		}
		try {
			Long userId = userService.findByUsername(auth.getName()).orElseThrow().getId();
			return notificationService.getUnreadCount(userId);
		} catch (Exception e) {
			return 0L;
		}
	}
	
	@GetMapping("/latest")
	@ResponseBody
	public java.util.Map<String, Object> getLatestNotification(Authentication auth) {
		java.util.Map<String, Object> result = new java.util.HashMap<>();
		if (auth == null || !auth.isAuthenticated()) {
			result.put("hasNew", false);
			return result;
		}
		try {
			Long userId = userService.findByUsername(auth.getName()).orElseThrow().getId();
			List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);
			if (!unreadNotifications.isEmpty()) {
				Notification latest = unreadNotifications.get(0);
				result.put("hasNew", true);
				result.put("title", latest.getTitle());
				result.put("message", latest.getMessage());
				result.put("count", unreadNotifications.size());
			} else {
				result.put("hasNew", false);
			}
		} catch (Exception e) {
			result.put("hasNew", false);
		}
		return result;
	}
}

