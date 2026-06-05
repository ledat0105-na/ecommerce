package service;

import entity.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.util.Units;
import org.springframework.stereotype.Service;
import util.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {
    public byte[] exportAllUsers(List<User> users) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Users");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Avatar");
            header.createCell(1).setCellValue("Username");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Full Name");
            header.createCell(4).setCellValue("Phone");
            header.createCell(5).setCellValue("Address");
            header.createCell(6).setCellValue("Ngày đăng ký");
            header.createCell(7).setCellValue("Thời gian sử dụng");
            
            // Style header
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i <= 7; i++) {
                Cell cell = header.getCell(i);
                if (cell != null) {
                    cell.setCellStyle(headerStyle);
                }
            }
            
            int r = 1;
            for (User u : users) {
                Row row = sheet.createRow(r++);
                
                // Avatar column (small image in bordered cell)
                Cell avatarCell = row.createCell(0);
                if (u.getAvatar() != null && u.getAvatar().length > 0) {
                    avatarCell.setCellValue("Đã đính kèm");
                    
                    // Add small avatar image
                    int pictureType = Workbook.PICTURE_TYPE_JPEG;
                    int pictureIdx = wb.addPicture(u.getAvatar(), pictureType);
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                    anchor.setCol1(0); // Column A
                    anchor.setRow1(r - 1); // Current row
                    anchor.setCol2(1); // End at column B
                    anchor.setRow2(r); // End at next row (small height)
                    
                    // Resize image to fit in small cell
                    Picture picture = drawing.createPicture(anchor, pictureIdx);
                    picture.resize(0.5); // Make image smaller
                    
                    // Add border to avatar cell
                    CellStyle avatarStyle = wb.createCellStyle();
                    avatarStyle.setBorderTop(BorderStyle.THIN);
                    avatarStyle.setBorderBottom(BorderStyle.THIN);
                    avatarStyle.setBorderLeft(BorderStyle.THIN);
                    avatarStyle.setBorderRight(BorderStyle.THIN);
                    avatarStyle.setAlignment(HorizontalAlignment.CENTER);
                    avatarStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    avatarCell.setCellStyle(avatarStyle);
                } else {
                    avatarCell.setCellValue("Không có");
                }
                
                // Data columns
                row.createCell(1).setCellValue(u.getUsername() != null ? u.getUsername() : "");
                row.createCell(2).setCellValue(u.getEmail() != null ? u.getEmail() : "");
                row.createCell(3).setCellValue(u.getHoTen() != null ? u.getHoTen() : "");
                row.createCell(4).setCellValue(u.getSoDienThoai() != null ? u.getSoDienThoai() : "");
                row.createCell(5).setCellValue(u.getDiaChi() != null ? u.getDiaChi() : "");
                
                // Ngày đăng ký
                if (u.getNgayTao() != null) {
                    Cell dateCell = row.createCell(6);
                    dateCell.setCellValue(u.getNgayTao().toString());
                    // Format date cell
                    CellStyle dateStyle = wb.createCellStyle();
                    CreationHelper createHelper = wb.getCreationHelper();
                    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    row.createCell(6).setCellValue("Chưa có");
                }
                
                // Thời gian sử dụng
                if (u.getNgayTao() != null) {
                    row.createCell(7).setCellValue(util.DateUtils.getTimeSinceRegistration(u.getNgayTao()));
                } else {
                    row.createCell(7).setCellValue("Chưa có");
                }
            }
            
            // Auto-size columns (except avatar column)
            for (int i = 1; i <= 7; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Set fixed width for avatar column
            sheet.setColumnWidth(0, 15 * 256); // 15 characters wide
            
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] exportSingleUser(User u) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Profile");
            
            // Ẩn ID và Role, chỉ hiển thị thông tin cần thiết
            createRow(sheet, 0, "Username", u.getUsername());
            createRow(sheet, 1, "Email", u.getEmail());
            createRow(sheet, 2, "Full Name", u.getHoTen());
            createRow(sheet, 3, "Phone", u.getSoDienThoai());
            createRow(sheet, 4, "Address", u.getDiaChi());
            
            // Thời gian đăng ký
            if (u.getNgayTao() != null) {
                String registrationDate = String.format("%02d/%02d/%04d %02d:%02d",
                    u.getNgayTao().getDayOfMonth(),
                    u.getNgayTao().getMonthValue(),
                    u.getNgayTao().getYear(),
                    u.getNgayTao().getHour(),
                    u.getNgayTao().getMinute());
                createRow(sheet, 5, "Ngày đăng ký", registrationDate);
                createRow(sheet, 6, "Thời gian sử dụng", DateUtils.getTimeSinceRegistration(u.getNgayTao()));
            } else {
                createRow(sheet, 5, "Ngày đăng ký", "Chưa có");
                createRow(sheet, 6, "Thời gian sử dụng", "Chưa có");
            }
            
            // Embed avatar if present - với kích thước nhỏ hơn và có khung
            int avatarRowIndex = 7; // Start avatar after registration info
            if (u.getAvatar() != null && u.getAvatar().length > 0) {
                Row avatarLabel = sheet.createRow(avatarRowIndex);
                avatarLabel.createCell(0).setCellValue("Avatar");
                avatarLabel.createCell(1).setCellValue("Đã đính kèm");
                
                // Add small avatar image with border
                int pictureType = Workbook.PICTURE_TYPE_JPEG;
                int pictureIdx = wb.addPicture(u.getAvatar(), pictureType);
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                anchor.setCol1(2); // place at column C
                anchor.setRow1(avatarRowIndex); // at avatar row
                anchor.setCol2(4); // end at column E (smaller width)
                anchor.setRow2(avatarRowIndex + 5); // end at row (smaller height)
                
                Picture picture = drawing.createPicture(anchor, pictureIdx);
                picture.resize(0.3); // Make image much smaller
                
                // Add border around avatar area
                for (int row = avatarRowIndex; row <= avatarRowIndex + 5; row++) {
                    for (int col = 2; col <= 4; col++) {
                        Row borderRow = sheet.getRow(row);
                        if (borderRow == null) borderRow = sheet.createRow(row);
                        Cell borderCell = borderRow.getCell(col);
                        if (borderCell == null) borderCell = borderRow.createCell(col);
                        
                        CellStyle borderStyle = wb.createCellStyle();
                        borderStyle.setBorderTop(BorderStyle.THIN);
                        borderStyle.setBorderBottom(BorderStyle.THIN);
                        borderStyle.setBorderLeft(BorderStyle.THIN);
                        borderStyle.setBorderRight(BorderStyle.THIN);
                        borderStyle.setAlignment(HorizontalAlignment.CENTER);
                        borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                        borderCell.setCellStyle(borderStyle);
                    }
                }
            }
            
            for (int i = 0; i <= 2; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRow(Sheet sheet, int index, String k, String v) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(k);
        row.createCell(1).setCellValue(v != null ? v : "");
    }
}
