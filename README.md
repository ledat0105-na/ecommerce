# 👟 Ecommerce Website - Bán Giày và Dép

Website bán hàng trực tuyến chuyên về **giày và dép** được xây dựng bằng Spring Boot, Thymeleaf và Bootstrap.

## 🚀 Tính năng

### Cho khách hàng:
- ✅ Xem danh sách giày và dép
- ✅ Tìm kiếm sản phẩm theo hãng
- ✅ Xem chi tiết sản phẩm
- ✅ Thêm sản phẩm vào giỏ hàng
- ✅ Quản lý giỏ hàng
- ✅ Đăng ký/Đăng nhập
- ✅ Đặt hàng

### Cho admin:
- ✅ Dashboard thống kê
- ✅ Quản lý sản phẩm (CRUD)
- ✅ Quản lý người dùng
- ✅ Quản lý đơn hàng
- ✅ Giao diện responsive với Bootstrap

## 🛠️ Công nghệ sử dụng

- **Backend**: Spring Boot 3.5.6, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5.3.0, Bootstrap Icons
- **Database**: MySQL 8.0
- **Build Tool**: Maven

## 📋 Yêu cầu hệ thống

- Java 17+
- Maven 3.6+
- MySQL 8.0+

## 🚀 Cách chạy

### 1. Cài đặt database

```bash
# Chạy script setup database hoàn chỉnh
setup-database.bat

# Hoặc chạy trực tiếp SQL
mysql -u root -p < ecommerce.sql
```

### 2. Cấu hình database

Sửa file `src/main/resources/application.properties`:

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Chạy ứng dụng

#### Cách 1: Sử dụng script (Windows)
```bash
# Setup database
setup-database.bat

# Chạy ứng dụng
run-app.bat
```

#### Cách 2: Sử dụng Maven
```bash
# Compile
./mvnw compile

# Chạy ứng dụng
./mvnw spring-boot:run
```

### 4. Truy cập ứng dụng

- **Trang chủ**: http://localhost:8080
- **Admin Dashboard**: http://localhost:8080/admin

## 👥 Tài khoản mẫu

### Admin
- Username: `admin`
- Password: `password123`
- Email: `admin@example.com`

### User
- Username: `user01`
- Password: `password123`
- Email: `user01@example.com`

## 👟 Sản phẩm

### 🏷️ Các hãng giày:
- **Nike**: Air Force 1, Air Max 270, React Infinity Run, Mercurial Vapor, LeBron 18
- **Adidas**: Ultraboost 22, Stan Smith, Predator Edge, Harden Vol. 6, NMD R1
- **Converse**: Chuck Taylor, One Star, Jack Purcell
- **Vans**: Old Skool, Sk8-Hi, Authentic
- **Puma**: Suede Classic, RS-X, Future Z
- **New Balance**: 990v5, 327, Fresh Foam
- **Jordan**: Air Jordan 1, 4, 11

### 👡 Các loại dép:
- **Dép Lê**: Nam da thật, Nữ da PU, Unisex canvas
- **Dép Tông**: Nam Adidas, Nữ Nike, Unisex Puma
- **Dép Quai Hậu**: Nam da, Nữ, Unisex
- **Crocs**: Classic Clog, LiteRide, Bistro

### 👠 Các loại giày khác:
- **Giày Cao Gót**: 6cm, 8cm, 10cm
- **Giày Lười**: Nam da, Nữ, Unisex
- **Giày Boot**: Nam da, Nữ, Unisex

## 💰 Khoảng giá

- **Dép**: 280,000 - 1,200,000 VNĐ
- **Giày Sneaker**: 1,500,000 - 5,000,000 VNĐ
- **Giày Thể Thao**: 2,200,000 - 4,500,000 VNĐ
- **Giày Chạy Bộ**: 3,000,000 - 3,800,000 VNĐ
- **Giày Bóng Đá**: 3,500,000 - 4,200,000 VNĐ
- **Giày Bóng Rổ**: 3,800,000 - 4,500,000 VNĐ

## 📁 Cấu trúc dự án

```
src/
├── main/
│   ├── java/
│   │   ├── controller/          # Controllers
│   │   ├── entity/             # JPA Entities
│   │   ├── repository/         # JPA Repositories
│   │   ├── service/            # Business Logic
│   │   └── config/             # Configuration
│   └── resources/
│       ├── static/css/         # CSS files
│       └── templates/          # Thymeleaf templates
│           ├── admin/          # Admin templates
│           ├── auth/           # Auth templates
│           ├── cart/           # Cart templates
│           ├── order/          # Order templates
│           └── product/        # Product templates
```

## 🎨 Giao diện

- **Responsive Design**: Hoạt động tốt trên mọi thiết bị
- **Bootstrap 5.3.0**: UI framework hiện đại
- **Bootstrap Icons**: Icon set đẹp mắt
- **Custom CSS**: Tùy chỉnh giao diện

## 🔧 Cấu hình

### Security
- Spring Security với form-based authentication
- Password encoding với BCrypt
- Role-based access control (USER/ADMIN)

### Database
- JPA/Hibernate cho ORM
- MySQL cho production
- 50 sản phẩm giày và dép mẫu

## 📝 API Endpoints

### Public
- `GET /` - Trang chủ
- `GET /product/list` - Danh sách sản phẩm
- `GET /product/detail/{id}` - Chi tiết sản phẩm
- `GET /auth/login` - Đăng nhập
- `GET /auth/register` - Đăng ký

### User (cần đăng nhập)
- `GET /cart` - Xem giỏ hàng
- `POST /cart/add/{productId}` - Thêm vào giỏ hàng
- `POST /cart/remove/{id}` - Xóa khỏi giỏ hàng

### Admin (cần role ADMIN)
- `GET /admin` - Dashboard
- `GET /admin/products` - Quản lý sản phẩm
- `GET /admin/users` - Quản lý người dùng
- `GET /admin/orders` - Quản lý đơn hàng

## 🐛 Troubleshooting

### Lỗi kết nối database
- Kiểm tra MySQL đã chạy chưa
- Kiểm tra thông tin kết nối trong application.properties
- Kiểm tra database 'ecommerce' đã tạo chưa

### Lỗi compile
- Kiểm tra Java version (cần Java 17+)
- Chạy `./mvnw clean compile`

### Lỗi template
- Kiểm tra file template có tồn tại không
- Kiểm tra cú pháp Thymeleaf

## 📞 Hỗ trợ

Nếu gặp vấn đề, vui lòng kiểm tra:
1. Logs trong console
2. Database connection
3. File cấu hình
4. Dependencies trong pom.xml

## 🎯 Roadmap

- [ ] Payment integration
- [ ] Email notifications
- [ ] Advanced search & filters
- [ ] Product reviews & ratings
- [ ] Order tracking
- [ ] Mobile app

---

**Chúc bạn sử dụng vui vẻ! 👟✨**