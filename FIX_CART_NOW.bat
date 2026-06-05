@echo off
echo ========================================
echo Script SỬA LỖI CART - Chạy SQL tự động
echo ========================================
echo.
echo Bạn cần nhập password MySQL của bạn
echo.
echo Đang chạy script SQL để sửa lỗi constraint...
echo.

mysql -u root -p ecommerce < fix_cart_constraint_simple.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Script đã chạy thành công!
    echo ========================================
    echo.
    echo QUAN TRỌNG: Bạn PHẢI RESTART ứng dụng Spring Boot!
    echo.
) else (
    echo.
    echo ========================================
    echo CÓ LỖI XẢY RA!
    echo ========================================
    echo.
    echo Vui lòng chạy script SQL thủ công trong MySQL Workbench
    echo hoặc MySQL Command Line.
    echo.
    echo Xem file: fix_cart_constraint_simple.sql
    echo.
)

pause

