-- ERP System Database Setup Script
-- This script creates the database and user for the ERP system

-- Create database
CREATE DATABASE IF NOT EXISTS erp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER IF NOT EXISTS 'erp_user'@'localhost' IDENTIFIED BY 'erp_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON erp_db.* TO 'erp_user'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;

-- Use the database
USE erp_db;

-- Note: Tables will be created automatically by the application
-- The DatabaseManager class will create the following tables:
-- - users
-- - customers  
-- - products
-- - orders
-- - order_items

-- Default admin user will be created automatically with:
-- Username: admin
-- Password: admin123
-- Role: ADMIN
