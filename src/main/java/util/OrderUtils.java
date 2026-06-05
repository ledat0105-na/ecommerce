package util;

public class OrderUtils {
    /**
     * Format order ID thành dạng #HD00001
     * @param orderId ID đơn hàng
     * @return String đã format, ví dụ: #HD00001
     */
    public static String formatOrderId(Long orderId) {
        if (orderId == null) {
            return "#HD00000";
        }
        return String.format("#HD%05d", orderId);
    }
}

