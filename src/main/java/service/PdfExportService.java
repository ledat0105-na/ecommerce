package service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import entity.User;
import org.springframework.stereotype.Service;
import util.DateUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] exportSingleUser(User u) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font labelFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            Paragraph title = new Paragraph("Thông tin người dùng", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            doc.add(title);

            // Prepare avatar image (if any)
            Image avatarImage = null;
            try {
                if (u.getAvatar() != null && u.getAvatar().length > 0) {
                    avatarImage = Image.getInstance(u.getAvatar());
                } else if (u.getAvatarUrl() != null && !u.getAvatarUrl().isBlank()) {
                    avatarImage = Image.getInstance(u.getAvatarUrl());
                }
            } catch (Exception ignored) {}

            // Build info table (right side)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);

            addRow(infoTable, "Tên đăng nhập", safe(u.getUsername()), labelFont, valueFont);
            addRow(infoTable, "Email", safe(u.getEmail()), labelFont, valueFont);
            addRow(infoTable, "Họ tên", safe(u.getHoTen()), labelFont, valueFont);
            addRow(infoTable, "SĐT", safe(u.getSoDienThoai()), labelFont, valueFont);
            addRow(infoTable, "Địa chỉ", safe(u.getDiaChi()), labelFont, valueFont);
            addRow(infoTable, "Vai trò", u.getRoleFromVaiTro(), labelFont, valueFont);
            
            // Thời gian đăng ký
            if (u.getNgayTao() != null) {
                String registrationDate = u.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                addRow(infoTable, "Ngày đăng ký", registrationDate, labelFont, valueFont);
                addRow(infoTable, "Thời gian sử dụng", DateUtils.getTimeSinceRegistration(u.getNgayTao()), labelFont, valueFont);
            } else {
                addRow(infoTable, "Ngày đăng ký", "Chưa có", labelFont, valueFont);
                addRow(infoTable, "Thời gian sử dụng", "Chưa có", labelFont, valueFont);
            }

            String cvInfo = (u.getCvFileName() != null ? u.getCvFileName() : "Không có");
            addRow(infoTable, "File upload", cvInfo, labelFont, valueFont);

            if (u.getAvatarUrl() != null && !u.getAvatarUrl().isBlank()) {
                addRow(infoTable, "Avatar URL", u.getAvatarUrl(), labelFont, valueFont);
            }

            String avatarInfo;
            if (u.getAvatar() != null && u.getAvatar().length > 0) {
                avatarInfo = "Đã tải lên";
            } else if (u.getAvatarUrl() != null && !u.getAvatarUrl().isBlank()) {
                String url = u.getAvatarUrl();
                int idx = url.lastIndexOf('/') + 1;
                avatarInfo = idx > 0 && idx < url.length() ? url.substring(idx) : url;
            } else {
                avatarInfo = "Không có";
            }
            addRow(infoTable, "Ảnh đại diện", avatarInfo, labelFont, valueFont);

            // Layout table with avatar on the left, info on the right under the title
            PdfPTable layout = new PdfPTable(new float[] {1f, 3f});
            layout.setWidthPercentage(100);

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            if (avatarImage != null) {
                avatarImage.scaleToFit(120, 120);
                left.addElement(avatarImage);
            }
            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            right.addElement(infoTable);

            layout.addCell(left);
            layout.addCell(right);
            doc.add(layout);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return ("Lỗi tạo PDF: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    public byte[] exportAllUsers(List<User> users) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Danh sách người dùng", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            doc.add(title);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            addHeader(table, "Username");
            addHeader(table, "Email");
            addHeader(table, "Họ tên");
            addHeader(table, "SĐT");
            addHeader(table, "Vai trò");
            addHeader(table, "Ngày đăng ký");
            addHeader(table, "Thời gian sử dụng");
            addHeader(table, "CV");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (User u : users) {
                table.addCell(safe(u.getUsername()));
                table.addCell(safe(u.getEmail()));
                table.addCell(safe(u.getHoTen()));
                table.addCell(safe(u.getSoDienThoai()));
                table.addCell(u.getRoleFromVaiTro());
                
                // Ngày đăng ký
                if (u.getNgayTao() != null) {
                    table.addCell(u.getNgayTao().format(dateFormatter));
                } else {
                    table.addCell("Chưa có");
                }
                
                // Thời gian sử dụng
                if (u.getNgayTao() != null) {
                    table.addCell(DateUtils.getTimeSinceRegistration(u.getNgayTao()));
                } else {
                    table.addCell("Chưa có");
                }
                
                table.addCell(u.getCvFileName() != null ? u.getCvFileName() : "");
            }

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return ("Lỗi tạo PDF: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private void addRow(PdfPTable t, String label, String value, Font lf, Font vf) {
        PdfPCell l = new PdfPCell(new Phrase(label, lf));
        PdfPCell v = new PdfPCell(new Phrase(value, vf));
        t.addCell(l);
        t.addCell(v);
    }

    private void addHeader(PdfPTable t, String label) {
        Font hf = new Font(Font.HELVETICA, 12, Font.BOLD);
        PdfPCell h = new PdfPCell(new Phrase(label, hf));
        t.addCell(h);
    }

    private String safe(String s) { return s == null ? "" : s; }
}


