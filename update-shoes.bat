@echo off
echo ========================================
echo    CAP NHAT DATABASE - CHI CON GIAY VA DEP
echo ========================================
echo.
echo Dang cap nhat database voi 30 san pham giay va dep...
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < ecommerce.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo    CAP NHAT THANH CONG!
    echo ========================================
    echo.
    echo Database da duoc cap nhat voi:
    echo - 30 san pham giay va dep
    echo - Cac hang: Nike, Adidas, Converse, Vans, Puma
    echo - Cac loai: Sneaker, The Thao, Chay Bo, Bong Da, Bong Ro
    echo - Dep: Dep Le, Dep Tong, Crocs
    echo - Giay dac biet: Cao Got, Luoi, Boot
    echo.
    echo Ban co the khoi dong ung dung ngay bay gio!
) else (
    echo.
    echo ========================================
    echo    LOI! Khong the cap nhat database
    echo ========================================
    echo.
    echo Vui long kiem tra:
    echo 1. MySQL da chay chua?
    echo 2. Mat khau MySQL dung chua?
    echo 3. Quyen truy cap database
)

pause
