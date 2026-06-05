# 🔧 Hướng dẫn SỬA LỖI "Duplicate entry for key 'cart_items.unique_user_product'"

## ❌ Vấn đề

Lỗi xảy ra vì database vẫn sử dụng constraint cũ `unique_user_product` (chỉ kiểm tra `user_id` và `product_id`) thay vì constraint mới `unique_user_product_size` (kiểm tra `user_id`, `product_id` và `size`).

**Lỗi hiển thị:**
```
could not execute statement [Duplicate entry '3-1' for key 'cart_items.unique_user_product']
```

## ✅ Giải pháp

### Cách 1: Chạy script SQL đơn giản (KHUYẾN NGHỊ)

1. **Mở MySQL Command Line hoặc MySQL Workbench**
2. **Kết nối đến database `ecommerce`**
3. **Chạy từng lệnh sau (copy và paste):**

```sql
USE ecommerce;

-- Bước 1: Thêm cột size (nếu chưa có)
-- Nếu báo lỗi "Duplicate column name 'size'", bỏ qua và chạy bước 2
ALTER TABLE cart_items ADD COLUMN size VARCHAR(10) NULL AFTER quantity;

-- Bước 2: Xóa constraint cũ (QUAN TRỌNG)
-- Nếu báo lỗi "Unknown key 'unique_user_product'", bỏ qua và chạy bước 3
ALTER TABLE cart_items DROP INDEX unique_user_product;

-- Bước 3: Xóa constraint mới nếu đã tồn tại
-- Nếu báo lỗi "Unknown key 'unique_user_product_size'", bỏ qua và chạy bước 4
ALTER TABLE cart_items DROP INDEX unique_user_product_size;

-- Bước 4: Thêm constraint mới (QUAN TRỌNG - phải chạy được)
ALTER TABLE cart_items ADD UNIQUE KEY unique_user_product_size (user_id, product_id, size);

-- Bước 5: Kiểm tra kết quả
SHOW CREATE TABLE cart_items;
```

4. **Kiểm tra kết quả:**
   - Chạy lệnh `SHOW CREATE TABLE cart_items;`
   - Bạn sẽ thấy: `UNIQUE KEY \`unique_user_product_size\` (\`user_id\`,\`product_id\`,\`size\`)`
   - Nếu thấy dòng này, có nghĩa là đã fix thành công!

### Cách 2: Sử dụng file SQL

1. Mở file `fix_cart_constraint_simple.sql` hoặc `fix_cart_constraint_safe.sql`
2. Copy toàn bộ nội dung
3. Paste vào MySQL Command Line hoặc MySQL Workbench
4. Chạy script (Execute)

### Cách 3: Sử dụng MySQL Workbench

1. Mở MySQL Workbench
2. Kết nối đến database
3. File → Open SQL Script → Chọn file `fix_cart_constraint_simple.sql`
4. Click nút "Execute" (⚡)
5. Kiểm tra kết quả trong tab "Output"

### Cách 4: Sử dụng Command Line (Windows)

```bash
mysql -u root -p ecommerce < fix_cart_constraint_simple.sql
```

## ⚠️ Lưu ý

1. **Nếu gặp lỗi "Duplicate column name 'size'"**:
   - Cột `size` đã tồn tại, bỏ qua bước 1 và tiếp tục bước 2

2. **Nếu gặp lỗi "Unknown key 'unique_user_product'"**:
   - Constraint cũ đã bị xóa, bỏ qua bước 2 và tiếp tục bước 3

3. **Nếu gặp lỗi "Duplicate key name 'unique_user_product_size'"**:
   - Constraint mới đã tồn tại, có thể database đã được fix rồi
   - Kiểm tra lại bằng lệnh `SHOW CREATE TABLE cart_items;`

4. **Sau khi chạy script**:
   - **BẮT BUỘC phải restart ứng dụng Spring Boot** để áp dụng thay đổi
   - Nếu không restart, ứng dụng vẫn sẽ dùng cache cũ

## ✅ Sau khi fix

1. **Restart ứng dụng Spring Boot**
2. **Thử thêm sản phẩm vào giỏ hàng** với size khác nhau
3. **Lỗi sẽ không còn xảy ra nữa**

## 🔍 Kiểm tra nhanh

Sau khi chạy script, chạy lệnh này để kiểm tra:

```sql
SHOW CREATE TABLE cart_items;
```

Tìm dòng có chứa:
```
UNIQUE KEY `unique_user_product_size` (`user_id`,`product_id`,`size`)
```

Nếu thấy dòng này, bạn đã fix thành công! 🎉

## 📝 Giải thích

- **Constraint cũ**: `unique_user_product` chỉ kiểm tra `user_id` và `product_id`
  - Không cho phép thêm cùng một sản phẩm 2 lần, ngay cả khi size khác nhau
  - Đây là lý do gây ra lỗi

- **Constraint mới**: `unique_user_product_size` kiểm tra `user_id`, `product_id` và `size`
  - Cho phép thêm cùng một sản phẩm với size khác nhau
  - Ví dụ: User có thể có Nike Air Force 1 size 40 và size 41 trong cùng giỏ hàng

