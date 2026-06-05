-- Script to update image_url column length in product_images table
-- Run this script to fix the "Data too long for column 'image_url'" error

ALTER TABLE product_images 
MODIFY COLUMN image_url VARCHAR(1000) NOT NULL COMMENT 'URL hình ảnh';

