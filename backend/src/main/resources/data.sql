-- BloodBridge Initial Data
-- Create admin user (password: admin123)
INSERT INTO users (name, email, password, role, status, created_at)
SELECT 'Admin', 'admin@bloodbridge.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh9i',
       'ADMIN', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@bloodbridge.com');
