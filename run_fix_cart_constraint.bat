@echo off
echo ========================================
echo    FIX CART ITEMS CONSTRAINT
echo ========================================
echo.
echo Script nay se:
echo 1. Them cot size vao bang cart_items
echo 2. Xoa constraint cu unique_user_product
echo 3. Them constraint moi unique_user_product_size
echo.
echo Vui long nhap thong tin MySQL:
echo.

set /p MYSQL_USER=MySQL Username (mac dinh: root): 
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS=MySQL Password: 

echo.
echo Dang chay script SQL...
echo.

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < fix_cart_size_constraint.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo    SUCCESS! Database da duoc cap nhat
    echo ========================================
) else (
    echo.
    echo ========================================
    echo    ERROR! Co loi xay ra
    echo ========================================
    echo.
    echo Vui long kiem tra:
    echo 1. MySQL da duoc cai dat
    echo 2. Username va password dung
    echo 3. Database 'ecommerce' da duoc tao
    echo.
)

pause

