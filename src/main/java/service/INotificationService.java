package service;

import entity.Notification;
import entity.Order;
import entity.User;

import java.util.List;

public interface INotificationService {
	Notification createNotification(User user, Order order, String title, String message);
	
	List<Notification> getUserNotifications(Long userId);
	
	List<Notification> getUnreadNotifications(Long userId);
	
	long getUnreadCount(Long userId);
	
	void markAsRead(Long notificationId);
	
	void markAllAsRead(Long userId);
	
	void deleteNotification(Long notificationId);
}

