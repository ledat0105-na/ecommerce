package controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import service.IUserService;
import service.INotificationService;
import service.ExcelExportService;
import service.PdfExportService;
import repository.UserUpdateRequestRepository;
import entity.UserUpdateRequest;
import entity.User;
import dto.UserUpdateRequestDTO;
import util.DateUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    
    private final IUserService userService;
    private final INotificationService notificationService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final UserUpdateRequestRepository requestRepo;
    
    public AdminUserController(IUserService userService, 
                              INotificationService notificationService,
                              ExcelExportService excelExportService, 
                              PdfExportService pdfExportService, 
                              UserUpdateRequestRepository requestRepo) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
        this.requestRepo = requestRepo;
    }
    
    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] data = excelExportService.exportAllUsers(userService.findAllUsers());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String timestamp = DateUtils.getTimestampForFilename();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_" + timestamp + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export-pdf")
    public ResponseEntity<byte[]> exportUsersPdf() {
        byte[] data = pdfExportService.exportAllUsers(userService.findAllUsers());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String timestamp = DateUtils.getTimestampForFilename();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_" + timestamp + ".pdf");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/update-requests")
    public String viewUpdateRequests(Model model) {
        var pendingRequests = requestRepo.findByStatusOrderByCreatedAtAsc(UserUpdateRequest.Status.PENDING);
        
        // Convert to DTOs
        List<UserUpdateRequestDTO> requestDTOs = pendingRequests.stream().map(request -> {
            UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
            dto.setId(request.getId());
            dto.setUserId(request.getUserId());
            dto.setNewFullName(request.getNewFullName());
            dto.setNewPhone(request.getNewPhone());
            dto.setNewAddress(request.getNewAddress());
            dto.setNewLocationId(request.getNewLocationId());
            dto.setStatus(request.getStatus().name());
            dto.setCreatedAt(request.getCreatedAt());
            dto.setProcessedAt(request.getProcessedAt());
            dto.setAdminNote(request.getAdminNote());
            dto.setHasNewAvatar(request.getNewAvatar() != null && request.getNewAvatar().length > 0);
            
            // Load user information
            userService.findById(request.getUserId()).ifPresent(user -> {
                dto.setUserUsername(user.getUsername());
                dto.setUserEmail(user.getEmail());
                dto.setUserFullName(user.getHoTen());
                dto.setUserPhone(user.getSoDienThoai());
                dto.setUserAddress(user.getDiaChi());
                dto.setUserRole(user.getRoleFromVaiTro());
                dto.setUserEnabled(user.isEnabled());
            });
            
            return dto;
        }).collect(Collectors.toList());
        
        model.addAttribute("pendingRequests", requestDTOs);
        return "admin/update-requests";
    }
    
    @GetMapping("/update-requests/{id}/avatar")
    @ResponseBody
    public ResponseEntity<byte[]> getRequestAvatar(@PathVariable Long id) {
        try {
            UserUpdateRequest request = requestRepo.findById(id).orElseThrow();
            if (request.getNewAvatar() != null && request.getNewAvatar().length > 0) {
                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(request.getNewAvatar());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/update-requests/approve")
    @ResponseBody
    public ResponseEntity<?> approve(@RequestParam Long id, 
                                     @RequestParam(required = false) String note) {
        try {
        UserUpdateRequest r = requestRepo.findById(id).orElseThrow();
        r.setStatus(UserUpdateRequest.Status.APPROVED);
        r.setProcessedAt(LocalDateTime.now());
        r.setAdminNote(note);
        var u = userService.findById(r.getUserId()).orElseThrow();
            String userName = u.getHoTen() != null && !u.getHoTen().isEmpty() ? u.getHoTen() : u.getUsername();
            
            if (r.getNewFullName() != null && !r.getNewFullName().trim().isEmpty()) {
                u.setHoTen(r.getNewFullName().trim());
            }
            if (r.getNewPhone() != null && !r.getNewPhone().trim().isEmpty()) {
                u.setSoDienThoai(r.getNewPhone().trim());
            }
            if (r.getNewAddress() != null && !r.getNewAddress().trim().isEmpty()) {
                u.setDiaChi(r.getNewAddress().trim());
            }
            // Update avatar if provided
            if (r.getNewAvatar() != null && r.getNewAvatar().length > 0) {
                u.setAvatar(r.getNewAvatar());
            }
        userService.updateUser(u);
            
            // Chuyển sang trạng thái COMPLETED sau khi đã cập nhật thành công
            r.setStatus(UserUpdateRequest.Status.COMPLETED);
        requestRepo.save(r);
            
            // Send notification to user with link to view request detail
            String message = "Yêu cầu cập nhật thông tin #" + id + " của bạn đã được duyệt và hoàn thành. Thông tin đã được cập nhật thành công.";
            if (note != null && !note.trim().isEmpty()) {
                message += " Ghi chú: " + note.trim();
            }
            message += " <a href=\"/profile/update-request/" + id + "\" class=\"btn btn-sm btn-link p-0\">Xem chi tiết yêu cầu</a>";
            notificationService.createNotification(u, null, "Yêu cầu cập nhật đã hoàn thành", message);
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "success", true,
                "message", "Đã duyệt và hoàn thành yêu cầu cập nhật thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                "success", false,
                "message", "Lỗi khi duyệt yêu cầu: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/update-requests/reject")
    @ResponseBody
    public ResponseEntity<?> reject(@RequestParam Long id, 
                                    @RequestParam(required = false) String note) {
        try {
        UserUpdateRequest r = requestRepo.findById(id).orElseThrow();
        r.setStatus(UserUpdateRequest.Status.REJECTED);
        r.setProcessedAt(LocalDateTime.now());
        r.setAdminNote(note);
        requestRepo.save(r);
            
            // Send notification to user with link to view request detail
            var u = userService.findById(r.getUserId()).orElseThrow();
            String message = "Yêu cầu cập nhật thông tin #" + id + " của bạn đã bị từ chối.";
            if (note != null && !note.trim().isEmpty()) {
                message += " <br/><strong>Lý do từ chối:</strong> <span style='color: #dc3545; font-weight: bold;'>" + note.trim() + "</span>";
            } else {
                message += " <br/><span style='color: #6c757d; font-style: italic;'>Admin chưa cung cấp lý do cụ thể.</span>";
            }
            message += " <br/><a href=\"/profile/update-request/" + id + "\" class=\"btn btn-sm btn-outline-danger mt-2\"><i class=\"bi bi-file-earmark-text me-1\"></i>Xem chi tiết yêu cầu</a>";
            notificationService.createNotification(u, null, "Yêu cầu cập nhật bị từ chối", message);
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "success", true,
                "message", "Đã từ chối yêu cầu cập nhật!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                "success", false,
                "message", "Lỗi khi từ chối yêu cầu: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/toggle-status")
    public String toggleUserStatus(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/lock")
    public String lockUser(@RequestParam Long userId, 
                          @RequestParam String lockReason,
                          RedirectAttributes redirectAttributes) {
        try {
            if (lockReason == null || lockReason.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do khóa tài khoản!");
                return "redirect:/admin/users";
            }
            userService.lockUser(userId, lockReason.trim());
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi khóa tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/unlock")
    public String unlockUser(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.unlockUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi mở khóa tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}