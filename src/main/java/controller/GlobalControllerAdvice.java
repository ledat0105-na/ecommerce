package controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import service.INotificationService;
import service.IUserService;

@ControllerAdvice
public class GlobalControllerAdvice {
	
	private final INotificationService notificationService;
	private final IUserService userService;
	
	public GlobalControllerAdvice(INotificationService notificationService, IUserService userService) {
		this.notificationService = notificationService;
		this.userService = userService;
	}
	
	@ModelAttribute("unreadCount")
	public Long getUnreadCount(Authentication auth) {
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
			try {
				return userService.findByUsername(auth.getName())
					.map(user -> notificationService.getUnreadCount(user.getId()))
					.orElse(0L);
			} catch (Exception e) {
				// Ignore errors
			}
		}
		return 0L;
	}
}

