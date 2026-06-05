package controller;

import entity.User;
import entity.UserUpdateRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import repository.UserUpdateRequestRepository;
import repository.UserRepository;
import service.ExcelExportService;
import service.PdfExportService;
import service.IUserService;
import service.INotificationService;
import dto.UserUpdateRequestSummaryDTO;
import util.DateUtils;
import java.util.List;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/profile")
public class UserProfileController {
    private final IUserService userService;
    private final UserUpdateRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final INotificationService notificationService;

    public UserProfileController(IUserService userService,
                                 UserUpdateRequestRepository requestRepository, 
                                 UserRepository userRepository,
                                 ExcelExportService excelExportService, 
                                 PdfExportService pdfExportService,
                                 INotificationService notificationService) {
        this.userService = userService;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails ud, Model model) {
        if (ud == null || !ud.isEnabled()) {
            return "redirect:/auth/login?error=NotAuthenticated";
        }
        
        try {
            User u = userService.findByUsername(ud.getUsername()).orElseThrow();
            model.addAttribute("user", u);
            
            List<UserUpdateRequest> requests = requestRepository.findByUserIdOrderByCreatedAtDesc(u.getId());
            List<UserUpdateRequestSummaryDTO> requestDTOs = requests.stream().map(request -> {
                UserUpdateRequestSummaryDTO dto = new UserUpdateRequestSummaryDTO();
                dto.setId(request.getId());
                dto.setStatus(request.getStatus().name());
                dto.setCreatedAt(request.getCreatedAt());
                dto.setProcessedAt(request.getProcessedAt());
                dto.setAdminNote(request.getAdminNote());
                dto.setHasNewAvatar(request.getNewAvatar() != null && request.getNewAvatar().length > 0);
                dto.setHasNewFullName(request.getNewFullName() != null && !request.getNewFullName().trim().isEmpty());
                dto.setHasNewPhone(request.getNewPhone() != null && !request.getNewPhone().trim().isEmpty());
                dto.setHasNewAddress(request.getNewAddress() != null && !request.getNewAddress().trim().isEmpty());
                return dto;
            }).collect(Collectors.toList());
            
            model.addAttribute("requests", requestDTOs);
            model.addAttribute("loginStatus", "Đã đăng nhập");
            model.addAttribute("userRole", u.getRoleFromVaiTro());
            model.addAttribute("isAdmin", "ROLE_ADMIN".equals(u.getRoleFromVaiTro()));
            
            return "user/profile";
        } catch (Exception e) {
            return "redirect:/auth/login?error=UserNotFound";
        }
    }
    
    @GetMapping("/update-request/{requestId}")
    public String viewUpdateRequest(@PathVariable Long requestId, 
                                   @AuthenticationPrincipal UserDetails ud, 
                                   Model model) {
        if (ud == null || !ud.isEnabled()) {
            return "redirect:/auth/login?error=NotAuthenticated";
        }
        
        try {
            User u = userService.findByUsername(ud.getUsername()).orElseThrow();
            UserUpdateRequest request = requestRepository.findById(requestId).orElseThrow();
            
            if (!request.getUserId().equals(u.getId())) {
                model.addAttribute("errorMessage", "Bạn không có quyền xem yêu cầu này!");
                return "redirect:/profile";
            }
            
            model.addAttribute("request", request);
            model.addAttribute("user", u);
            return "user/update-request-detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không tìm thấy yêu cầu!");
            return "redirect:/profile";
        }
    }

    @GetMapping("/update")
    public String updatePage(@AuthenticationPrincipal UserDetails ud, Model model) {
        User u = userService.findByUsername(ud.getUsername()).orElseThrow();
        model.addAttribute("user", u);
        return "user/update";
    }

    @PostMapping("/request-update")
    public String requestUpdate(@AuthenticationPrincipal UserDetails ud,
                                @RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String provinceName,
                                @RequestParam(required = false) String districtName,
                                @RequestParam(required = false) String wardName,
                                @RequestParam(required = false) Integer locationId,
                                @org.springframework.web.bind.annotation.RequestParam(value = "avatar", required = false) org.springframework.web.multipart.MultipartFile avatar,
                                @org.springframework.web.bind.annotation.RequestParam(value = "cv", required = false) org.springframework.web.multipart.MultipartFile cv,
                                RedirectAttributes redirectAttributes) {
        User u = userService.findByUsername(ud.getUsername()).orElseThrow();
        UserUpdateRequest r = new UserUpdateRequest();
        r.setUserId(u.getId());
        r.setNewFullName(fullName);
        r.setNewPhone(phone);
        
        StringBuilder fullAddressBuilder = new StringBuilder();
        if (address != null && !address.trim().isEmpty()) {
            fullAddressBuilder.append(address.trim());
        }
        if (wardName != null && !wardName.trim().isEmpty()) {
            if (fullAddressBuilder.length() > 0) fullAddressBuilder.append(", ");
            fullAddressBuilder.append(wardName.trim());
        }
        if (districtName != null && !districtName.trim().isEmpty()) {
            if (fullAddressBuilder.length() > 0) fullAddressBuilder.append(", ");
            fullAddressBuilder.append(districtName.trim());
        }
        if (provinceName != null && !provinceName.trim().isEmpty()) {
            if (fullAddressBuilder.length() > 0) fullAddressBuilder.append(", ");
            fullAddressBuilder.append(provinceName.trim());
        }
        
        String finalAddress = fullAddressBuilder.length() > 0 ? fullAddressBuilder.toString() : address;
        r.setNewAddress(finalAddress);
        
        try {
            if (avatar != null && !avatar.isEmpty()) {
                if (avatar.getSize() <= 3 * 1024 * 1024) {
                    r.setNewAvatar(avatar.getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
                }
        
        try {
            if (cv != null && !cv.isEmpty()) {
                if (cv.getSize() <= 5 * 1024 * 1024) {
                    u.setCvFileName(cv.getOriginalFilename());
                    u.setCvContentType(cv.getContentType());
                    u.setCvData(cv.getBytes());
                    userService.updateUser(u);
                }
            }
        } catch (Exception ignored) {}
        
        UserUpdateRequest savedRequest = requestRepository.save(r);
        
        try {
            var adminUsers = userRepository.findByVaiTro(User.VaiTro.admin);
            String userName = u.getHoTen() != null && !u.getHoTen().isEmpty() ? u.getHoTen() : u.getUsername();
            String message = "Người dùng " + userName + " (" + u.getUsername() + ") đã gửi yêu cầu cập nhật thông tin cá nhân. Yêu cầu #" + savedRequest.getId() + 
                            " <a href=\"/admin/users/update-requests\" class=\"btn btn-sm btn-link p-0\">Xem chi tiết yêu cầu</a>";
            for (User admin : adminUsers) {
                    notificationService.createNotification(admin, null, "Yêu cầu cập nhật thông tin mới", message);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu cập nhật thành công! Vui lòng chờ admin duyệt.");
        return "redirect:/profile";
    }
    
    @PostMapping("/request-update-ajax")
    @ResponseBody
    public ResponseEntity<?> requestUpdateAjax(@AuthenticationPrincipal UserDetails ud,
                                               @RequestParam(required = false) String fullName,
                                               @RequestParam(required = false) String phone,
                                               @RequestParam(required = false) String address,
                                               @RequestParam(required = false) String provinceName,
                                               @RequestParam(required = false) String districtName,
                                               @RequestParam(required = false) String wardName,
                                               @RequestParam(required = false) Integer locationId,
                                               @org.springframework.web.bind.annotation.RequestParam(value = "avatar", required = false) org.springframework.web.multipart.MultipartFile avatar,
                                               @org.springframework.web.bind.annotation.RequestParam(value = "cv", required = false) org.springframework.web.multipart.MultipartFile cv) {
        try {
            User u = userService.findByUsername(ud.getUsername()).orElseThrow();
            UserUpdateRequest r = new UserUpdateRequest();
            r.setUserId(u.getId());
            r.setNewFullName(fullName);
            r.setNewPhone(phone);
            
            StringBuilder fullAddressBuilder = new StringBuilder();
            if (address != null && !address.trim().isEmpty()) {
                fullAddressBuilder.append(address.trim());
            }
            if (wardName != null && !wardName.trim().isEmpty()) {
                if (fullAddressBuilder.length() > 0) fullAddressBuilder.append(", ");
                fullAddressBuilder.append(wardName.trim());
            }
            if (districtName != null && !districtName.trim().isEmpty()) {
                if (fullAddressBuilder.length() > 0) fullAddressBuilder.append(", ");
                fullAddressBuilder.append(districtName.trim());
            }
            if (provinceName != null && !provinceName.trim().isEmpty()) {
                if (fullAddressBuilder.length() > 0) fullAddressBuilder.append(", ");
                fullAddressBuilder.append(provinceName.trim());
            }
            
            String finalAddress = fullAddressBuilder.length() > 0 ? fullAddressBuilder.toString() : address;
            r.setNewAddress(finalAddress);
            
            try {
                if (avatar != null && !avatar.isEmpty()) {
                    if (avatar.getSize() <= 3 * 1024 * 1024) {
                        r.setNewAvatar(avatar.getBytes());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                if (cv != null && !cv.isEmpty()) {
                    if (cv.getSize() <= 5 * 1024 * 1024) {
                        u.setCvFileName(cv.getOriginalFilename());
                        u.setCvContentType(cv.getContentType());
                        u.setCvData(cv.getBytes());
                        userService.updateUser(u);
                    }
                }
            } catch (Exception ignored) {}
            
            UserUpdateRequest savedRequest = requestRepository.save(r);
            
            try {
                var adminUsers = userRepository.findByVaiTro(User.VaiTro.admin);
                String userName = u.getHoTen() != null && !u.getHoTen().isEmpty() ? u.getHoTen() : u.getUsername();
                String message = "Người dùng " + userName + " (" + u.getUsername() + ") đã gửi yêu cầu cập nhật thông tin cá nhân. Yêu cầu #" + savedRequest.getId() + 
                                " <a href=\"/admin/users/update-requests\" class=\"btn btn-sm btn-link p-0\">Xem chi tiết yêu cầu</a>";
                for (User admin : adminUsers) {
                    notificationService.createNotification(admin, null, "Yêu cầu cập nhật thông tin mới", message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "success", true,
                "message", "Đã gửi yêu cầu cập nhật thành công! Vui lòng chờ admin duyệt."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "Có lỗi xảy ra khi gửi yêu cầu cập nhật. Vui lòng thử lại!";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                "success", false,
                "message", "Lỗi khi gửi yêu cầu: " + errorMessage
            ));
        }
    }
    
    @PostMapping("/update-avatar")
    public String updateAvatar(@AuthenticationPrincipal UserDetails ud,
                               @org.springframework.web.bind.annotation.RequestParam("avatar") org.springframework.web.multipart.MultipartFile avatar,
                               RedirectAttributes redirectAttributes) {
        if (ud == null || !ud.isEnabled()) {
            return "redirect:/auth/login?error=NotAuthenticated";
        }
        
        try {
            User u = userService.findByUsername(ud.getUsername()).orElseThrow();
            if (avatar != null && !avatar.isEmpty()) {
                if (avatar.getSize() > 3 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Ảnh đại diện quá lớn! Tối đa 3MB.");
                    return "redirect:/profile";
                }
                
                UserUpdateRequest r = new UserUpdateRequest();
                r.setUserId(u.getId());
                r.setNewAvatar(avatar.getBytes());
                UserUpdateRequest savedRequest = requestRepository.save(r);
                
                try {
                    var adminUsers = userRepository.findByVaiTro(User.VaiTro.admin);
                    String userName = u.getHoTen() != null && !u.getHoTen().isEmpty() ? u.getHoTen() : u.getUsername();
                    String message = "Người dùng " + userName + " (" + u.getUsername() + ") đã gửi yêu cầu cập nhật ảnh đại diện. Yêu cầu #" + savedRequest.getId() + 
                                    " <a href=\"/admin/users/update-requests\" class=\"btn btn-sm btn-link p-0\">Xem chi tiết yêu cầu</a>";
                    for (User admin : adminUsers) {
                        notificationService.createNotification(admin, null, "Yêu cầu cập nhật ảnh đại diện", message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu cập nhật ảnh đại diện thành công! Vui lòng chờ admin duyệt.");
                return "redirect:/profile";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gửi yêu cầu: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportProfile(@AuthenticationPrincipal UserDetails ud) {
        User u = userService.findByUsername(ud.getUsername()).orElseThrow();
        byte[] data = excelExportService.exportSingleUser(u);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String timestamp = DateUtils.getTimestampForFilename();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profile_" + u.getUsername() + "_" + timestamp + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export-pdf")
    public ResponseEntity<byte[]> exportProfilePdf(@AuthenticationPrincipal UserDetails ud) {
        User u = userService.findByUsername(ud.getUsername()).orElseThrow();
        byte[] data = pdfExportService.exportSingleUser(u);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String timestamp = DateUtils.getTimestampForFilename();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profile_" + u.getUsername() + "_" + timestamp + ".pdf");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export-all")
    public ResponseEntity<byte[]> exportProfileBundle(@AuthenticationPrincipal UserDetails ud) {
        User u = userService.findByUsername(ud.getUsername()).orElseThrow();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String timestamp = DateUtils.getTimestampForFilename();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                byte[] pdf = pdfExportService.exportSingleUser(u);
                ZipEntry pdfEntry = new ZipEntry("profile_" + u.getUsername() + "_" + timestamp + ".pdf");
                zos.putNextEntry(pdfEntry);
                zos.write(pdf);
                zos.closeEntry();

                if (u.getCvData() != null && u.getCvData().length > 0) {
                    String cvName = (u.getCvFileName() != null && !u.getCvFileName().isEmpty())
                            ? u.getCvFileName() : ("cv_" + u.getUsername() + ".bin");
                    ZipEntry cvEntry = new ZipEntry(cvName);
                    zos.putNextEntry(cvEntry);
                    zos.write(u.getCvData());
                    zos.closeEntry();
                }
            }
            byte[] zipBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_" + u.getUsername() + "_bundle_" + timestamp + ".zip");
            return ResponseEntity.ok().headers(headers).body(zipBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/avatar")
    public ResponseEntity<byte[]> avatar(@AuthenticationPrincipal UserDetails ud) {
        User u = userService.findByUsername(ud.getUsername()).orElseThrow();
        byte[] bytes = u.getAvatar();
        if (bytes == null || bytes.length == 0) {
            bytes = new byte[] { (byte)137,80,78,71,13,10,26,10,0,0,0,13,73,72,68,82,0,0,0,1,0,0,0,1,8,6,0,0,0,31,-21,120,-102,0,0,0,1,115,82,71,66,0,-82, -49,30, -5,0,0,0,10,73,68,65,84,120,-38,99,0,1,0,0,5,0,1,-123, -6, -43,  -60,0,0,0,0,73,69,78,68,-82,66,96,-126 };
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes);
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

    @GetMapping("/cv")
    public ResponseEntity<byte[]> downloadCv(@AuthenticationPrincipal UserDetails ud) {
        User u = userService.findByUsername(ud.getUsername()).orElseThrow();
        if (u.getCvData() == null || u.getCvData().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String fileName = (u.getCvFileName() != null && !u.getCvFileName().isEmpty())
                ? u.getCvFileName() : ("cv_" + u.getUsername() + ".pdf");
        MediaType ct = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (u.getCvContentType() != null) {
                ct = MediaType.parseMediaType(u.getCvContentType());
            }
        } catch (Exception ignored) {}
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(ct);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return ResponseEntity.ok().headers(headers).body(u.getCvData());
    }
}
