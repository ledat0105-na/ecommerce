package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    
    public static String getTimestampForFilename() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
    
    public static String getTimestampForFilename(LocalDateTime dateTime) {
        if (dateTime == null) {
            return getTimestampForFilename();
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    public static String getTimeSinceRegistration(LocalDateTime registrationDate) {
        if (registrationDate == null) {
            return "Chưa có thông tin";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        long years = ChronoUnit.YEARS.between(registrationDate, now);
        if (years > 0) {
            return years + (years == 1 ? " năm trước" : " năm trước");
        }
        
        long months = ChronoUnit.MONTHS.between(registrationDate, now);
        if (months > 0) {
            return months + (months == 1 ? " tháng trước" : " tháng trước");
        }
        
        long weeks = ChronoUnit.WEEKS.between(registrationDate, now);
        if (weeks > 0) {
            return weeks + (weeks == 1 ? " tuần trước" : " tuần trước");
        }
        
        long days = ChronoUnit.DAYS.between(registrationDate, now);
        if (days > 0) {
            return days + (days == 1 ? " ngày trước" : " ngày trước");
        }
        
        long hours = ChronoUnit.HOURS.between(registrationDate, now);
        if (hours > 0) {
            return hours + (hours == 1 ? " giờ trước" : " giờ trước");
        }
        
        long minutes = ChronoUnit.MINUTES.between(registrationDate, now);
        if (minutes > 0) {
            return minutes + (minutes == 1 ? " phút trước" : " phút trước");
        }
        
        return "Vừa mới đăng ký";
    }
    
    public static String getTimeSince(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Chưa có thông tin";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        long years = ChronoUnit.YEARS.between(dateTime, now);
        if (years > 0) {
            return years + (years == 1 ? " năm trước" : " năm trước");
        }
        
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months > 0) {
            return months + (months == 1 ? " tháng trước" : " tháng trước");
        }
        
        long weeks = ChronoUnit.WEEKS.between(dateTime, now);
        if (weeks > 0) {
            return weeks + (weeks == 1 ? " tuần trước" : " tuần trước");
        }
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days > 0) {
            return days + (days == 1 ? " ngày trước" : " ngày trước");
        }
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours > 0) {
            return hours + (hours == 1 ? " giờ trước" : " giờ trước");
        }
        
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes > 0) {
            return minutes + (minutes == 1 ? " phút trước" : " phút trước");
        }
        
        return "Vừa mới";
    }
    
    public static long getDaysSinceRegistration(LocalDateTime registrationDate) {
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDateTime.now());
    }
    
    public static String getFormattedRegistrationInfo(LocalDateTime registrationDate) {
        if (registrationDate == null) {
            return "Chưa có thông tin";
        }
        
        String timeAgo = getTimeSinceRegistration(registrationDate);
        String dateFormatted = String.format("%02d/%02d/%04d", 
            registrationDate.getDayOfMonth(),
            registrationDate.getMonthValue(),
            registrationDate.getYear());
        
        return "Đã đăng ký " + timeAgo + " (" + dateFormatted + ")";
    }
}

