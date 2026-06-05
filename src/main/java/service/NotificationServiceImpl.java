package service;

import entity.Notification;
import entity.Order;
import entity.User;
import repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

	private final NotificationRepository notificationRepository;

	public NotificationServiceImpl(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@Override
	@Transactional
	public Notification createNotification(User user, Order order, String title, String message) {
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setOrder(order);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setIsRead(false);
		return notificationRepository.save(notification);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> getUserNotifications(Long userId) {
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> getUnreadNotifications(Long userId) {
		return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public long getUnreadCount(Long userId) {
		return notificationRepository.countByUserIdAndIsReadFalse(userId);
	}

	@Override
	@Transactional
	public void markAsRead(Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new RuntimeException("Notification not found"));
		notification.setIsRead(true);
		notificationRepository.save(notification);
	}

	@Override
	@Transactional
	public void markAllAsRead(Long userId) {
		List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
		notifications.forEach(notification -> notification.setIsRead(true));
		notificationRepository.saveAll(notifications);
	}

	@Override
	@Transactional
	public void deleteNotification(Long notificationId) {
		notificationRepository.deleteById(notificationId);
	}
}

