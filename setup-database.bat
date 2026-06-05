@echo off
echo ========================================
echo    SETUP ECOMMERCE DATABASE
echo ========================================
echo.

echo Dang tao database va du lieu mau...
echo.

REM Thay doi duong dan MySQL neu can
mysql -u root -p < ecommerce.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo    THANH CONG! Database da duoc tao
    echo ========================================
    echo.
    echo Thong tin dang nhap:
    echo - Admin: admin / password123
    echo - User: user01 / password123
    echo.
    echo Ban co the chay ung dung Spring Boot:
    echo ./mvnw spring-boot:run
    echo.
) else (
    echo.
    echo ========================================
    echo    LOI! Khong the tao database
    echo ========================================
    echo.
    echo Vui long kiem tra:
    echo 1. MySQL da chay chua?
    echo 2. Duong dan den MySQL dung chua?
    echo 3. Quyen truy cap database
    echo.
)

pause
