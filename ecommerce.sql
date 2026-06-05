-- =====================================================
-- ECOMMERCE DATABASE - COMPLETE SQL SCRIPT
-- Website bán hàng trực tuyến
-- Tạo database
CREATE DATABASE IF NOT EXISTS ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;

-- =====================================================
-- XÓA CÁC BẢNG CŨ (NẾU CÓ)
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS user_update_requests;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- TẠO CÁC BẢNG
-- =====================================================

-- Bảng Users (Người dùng)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) UNIQUE NOT NULL COMMENT 'Tên đăng nhập',
    mat_khau VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã mã hóa',
    email VARCHAR(100) UNIQUE COMMENT 'Email',
    ho_ten VARCHAR(100) COMMENT 'Họ và tên',
    so_dien_thoai VARCHAR(20) COMMENT 'Số điện thoại',
    dia_chi VARCHAR(255) COMMENT 'Địa chỉ',
    avatar LONGBLOB NULL COMMENT 'Ảnh đại diện (binary)',
    avatar_url VARCHAR(500) NULL COMMENT 'URL ảnh đại diện',
    cv_file_name VARCHAR(255) NULL COMMENT 'Tên file CV',
    cv_content_type VARCHAR(100) NULL COMMENT 'Loại file CV',
    cv_data LONGBLOB NULL COMMENT 'Dữ liệu CV (binary)',
    vai_tro ENUM('admin','customer') DEFAULT 'customer' COMMENT 'Vai trò: admin hoặc customer',
    enabled BOOLEAN DEFAULT TRUE COMMENT 'Trạng thái: TRUE = hoạt động, FALSE = bị khóa',
    lock_reason VARCHAR(500) NULL COMMENT 'Lý do khóa tài khoản',
    locked_at DATETIME NULL COMMENT 'Thời gian khóa tài khoản',
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo tài khoản',
    INDEX idx_username (ten_dang_nhap),
    INDEX idx_email (email),
    INDEX idx_vai_tro (vai_tro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Categories (Danh mục sản phẩm)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Tên danh mục',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Products (Sản phẩm)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL COMMENT 'Tên sản phẩm',
    description TEXT COMMENT 'Mô tả sản phẩm',
    image_url VARCHAR(500) COMMENT 'URL hình ảnh sản phẩm (deprecated - dùng product_images)',
    price DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Giá sản phẩm',
    stock INT NOT NULL DEFAULT 0 COMMENT 'Số lượng tồn kho',
    category VARCHAR(100) COMMENT 'Danh mục sản phẩm',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
    INDEX idx_category (category),
    INDEX idx_price (price),
    INDEX idx_stock (stock),
    INDEX idx_created_at (created_at),
    FULLTEXT idx_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Product Images (Hình ảnh sản phẩm)
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT 'ID sản phẩm',
    image_url VARCHAR(500) NOT NULL COMMENT 'URL hình ảnh',
    display_order INT NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Cart Items (Giỏ hàng)
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'ID người dùng',
    product_id BIGINT NOT NULL COMMENT 'ID sản phẩm',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Số lượng',
    size VARCHAR(10) NULL COMMENT 'Kích cỡ (nếu có)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày thêm vào giỏ',
    UNIQUE KEY unique_user_product_size (user_id, product_id, size),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Orders (Đơn hàng)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'ID người dùng đặt hàng',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Tổng tiền đơn hàng',
    status VARCHAR(20) NOT NULL DEFAULT 'NEW' COMMENT 'Trạng thái: NEW, APPROVED, PAID, CANCELLED, COMPLETED',
    cancel_reason VARCHAR(500) NULL COMMENT 'Lý do hủy đơn hàng',
    payment_method VARCHAR(50) NULL COMMENT 'Hình thức thanh toán: COD, BANK_TRANSFER, CREDIT_CARD, etc.',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo đơn hàng',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Order Items (Chi tiết đơn hàng)
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT 'ID đơn hàng',
    product_id BIGINT NOT NULL COMMENT 'ID sản phẩm',
    quantity INT NOT NULL COMMENT 'Số lượng',
    price DECIMAL(12,2) NOT NULL COMMENT 'Giá tại thời điểm đặt hàng',
    size VARCHAR(10) NULL COMMENT 'Kích thước sản phẩm',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Notifications (Thông báo)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'ID người dùng nhận thông báo',
    order_id BIGINT NULL COMMENT 'ID đơn hàng liên quan (nếu có)',
    title VARCHAR(255) NOT NULL COMMENT 'Tiêu đề thông báo',
    message VARCHAR(1000) NOT NULL COMMENT 'Nội dung thông báo',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Đã đọc chưa',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo thông báo',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng User Update Requests (Yêu cầu cập nhật thông tin)
CREATE TABLE IF NOT EXISTS user_update_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'ID người dùng',
    new_full_name VARCHAR(100) COMMENT 'Họ tên mới',
    new_phone VARCHAR(20) COMMENT 'Số điện thoại mới',
    new_address VARCHAR(255) COMMENT 'Địa chỉ mới',
    new_location_id INT COMMENT 'ID vị trí mới',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái: PENDING, APPROVED, REJECTED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo yêu cầu',
    processed_at DATETIME NULL COMMENT 'Ngày xử lý',
    admin_note VARCHAR(255) NULL COMMENT 'Ghi chú của admin',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERT DỮ LIỆU MẪU
-- =====================================================

-- Thêm người dùng mẫu
-- Mật khẩu: password (đã mã hóa bằng BCrypt)
INSERT INTO users (ten_dang_nhap, mat_khau, email, ho_ten, so_dien_thoai, dia_chi, vai_tro) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin@shopgiaydep.com', 'Quản trị viên', '0123456789', '123 Đường ABC, Quận 1, TP.HCM', 'admin'),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'user@shopgiaydep.com', 'Người dùng mẫu', '0987654321', '456 Đường XYZ, Quận 2, TP.HCM', 'customer'),
('ledat1234', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ledat1234@example.com', 'Lê Đạt', '0901234567', '789 Đường DEF, Quận 3, TP.HCM', 'customer');

-- Thêm danh mục sản phẩm
INSERT INTO categories (name) VALUES
('Giày Sneaker'),
('Giày Thể Thao'),
('Giày Chạy Bộ'),
('Giày Bóng Đá'),
('Dép Sandal'),
('Dép Lê'),
('Dép Tông'),
('Giày Cao Gót'),
('Giày Boots'),
('Giày Loafer');

-- Thêm sản phẩm mẫu
INSERT INTO products (name, description, image_url, price, stock, category) VALUES
('Nike Air Force 1', 'Giày sneaker cổ điển với thiết kế đơn giản và thoải mái. Phù hợp cho mọi hoạt động hàng ngày.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 2500000, 50, 'Giày Sneaker'),
('Adidas Stan Smith', 'Giày tennis tối giản, dễ phối đồ. Thiết kế cổ điển với logo Adidas nổi tiếng.', 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=800&q=80', 2200000, 45, 'Giày Sneaker'),
('Converse Chuck Taylor', 'Giày canvas cổ điển phù hợp mọi phong cách. Biểu tượng của thời trang đường phố.', 'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80', 1800000, 60, 'Giày Sneaker'),
('Nike Air Max 270', 'Giày thể thao với đế Air Max êm ái. Công nghệ đệm khí tiên tiến cho trải nghiệm tuyệt vời.', 'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80', 3200000, 35, 'Giày Thể Thao'),
('Adidas Ultraboost 22', 'Giày chạy bộ với đệm Boost nổi tiếng. Hiệu suất cao cho các vận động viên chuyên nghiệp.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 4500000, 25, 'Giày Thể Thao'),
('Nike React Infinity Run', 'Giày chạy bộ công nghệ React mới. Giảm chấn thương và tăng hiệu suất chạy.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 3800000, 20, 'Giày Chạy Bộ'),
('Nike Mercurial Vapor', 'Giày bóng đá với thiết kế nhẹ và linh hoạt. Dành cho các cầu thủ chuyên nghiệp.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 5500000, 15, 'Giày Bóng Đá'),
('Adidas Predator', 'Giày bóng đá với công nghệ grip cao cấp. Kiểm soát bóng tốt hơn trên mọi mặt sân.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 5200000, 18, 'Giày Bóng Đá'),
('Dép Sandal Quai Ngang', 'Dép sandal thoáng mát, phù hợp đi biển và mùa hè. Chất liệu cao su bền đẹp.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 350000, 100, 'Dép Sandal'),
('Dép Lê Thể Thao', 'Dép lê tiện lợi cho mọi hoạt động. Thiết kế đơn giản, dễ dàng mang vào tháo ra.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 250000, 120, 'Dép Lê'),
('Dép Tông Nam', 'Dép tông nam cổ điển, thoải mái. Phù hợp cho mọi lứa tuổi.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 200000, 150, 'Dép Tông'),
('Giày Cao Gót 7cm', 'Giày cao gót nữ thanh lịch. Thiết kế sang trọng phù hợp công sở và dự tiệc.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 1200000, 30, 'Giày Cao Gót'),
('Giày Boots Da Thật', 'Giày boots nam phong cách. Chất liệu da thật bền đẹp, phù hợp mùa đông.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 2800000, 25, 'Giày Boots'),
('Giày Loafer Da Bò', 'Giày loafer nam công sở. Thiết kế tối giản, phù hợp mặc vest và quần âu.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 2100000, 40, 'Giày Loafer');

-- Thêm đơn hàng mẫu
INSERT INTO orders (user_id, total_amount, status, created_at) VALUES
(2, 4300000, 'NEW', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2800000, 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 2500000, 'PAID', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 1800000, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 5500000, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Thêm chi tiết đơn hàng mẫu
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
-- Đơn hàng 1 (user 2): 4300000
(1, 1, 1, 2500000),  -- Nike Air Force 1
(1, 3, 1, 1800000),  -- Converse Chuck Taylor

-- Đơn hàng 2 (user 2): 2800000
(2, 2, 1, 2200000),  -- Adidas Stan Smith
(2, 9, 2, 600000),   -- Dép Sandal (2 đôi)

-- Đơn hàng 3 (user 3): 2500000
(3, 1, 1, 2500000),  -- Nike Air Force 1

-- Đơn hàng 4 (user 3): 1800000
(4, 3, 1, 1800000),  -- Converse Chuck Taylor

-- Đơn hàng 5 (user 2): 5500000 (đã hủy)
(5, 7, 1, 5500000);  -- Nike Mercurial Vapor

-- =====================================================
-- KIỂM TRA DỮ LIỆU
-- =====================================================

-- Hiển thị số lượng bản ghi trong mỗi bảng
SELECT 'Users' AS TableName, COUNT(*) AS RecordCount FROM users
UNION ALL
SELECT 'Categories', COUNT(*) FROM categories
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items
UNION ALL
SELECT 'Cart Items', COUNT(*) FROM cart_items;

-- Hiển thị thông tin đơn hàng mẫu
SELECT 
    o.id AS OrderID,
    CONCAT('#HD', LPAD(o.id, 5, '0')) AS OrderCode,
    u.ten_dang_nhap AS Username,
    u.ho_ten AS CustomerName,
    o.total_amount AS TotalAmount,
    o.status AS Status,
    o.created_at AS OrderDate,
    COUNT(oi.id) AS ItemCount
FROM orders o
JOIN users u ON o.user_id = u.id
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, u.ten_dang_nhap, u.ho_ten, o.total_amount, o.status, o.created_at
ORDER BY o.created_at DESC;

-- =====================================================
-- HOÀN TẤT
-- =====================================================
SELECT 'Database ecommerce đã được tạo thành công!' AS Message;
SELECT 'Tổng số bảng: 7' AS Info;
SELECT 'Bảng: users, categories, products, cart_items, orders, order_items, user_update_requests' AS Tables;
ALTER TABLE product_images 
MODIFY COLUMN image_url VARCHAR(1000) NOT NULL COMMENT 'URL hình ảnh';