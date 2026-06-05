package repository;

import entity.UserUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserUpdateRequestRepository extends JpaRepository<UserUpdateRequest, Long> {
    List<UserUpdateRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<UserUpdateRequest> findByStatusOrderByCreatedAtAsc(UserUpdateRequest.Status status);
}
