-- Fix password hashes in AuthDB
-- Run this script to update password hashes for default users

USE AuthDB;

-- Update admin password (admin123)
UPDATE users_auth 
SET password_hash = '$2a$10$fEs5Z8d/A72htuXe/hvsEecJOoMbcKVkm686d0ltpETgZLJ5dZNo.' 
WHERE username = 'admin';

-- Update instructor1 password (teach123)
UPDATE users_auth 
SET password_hash = '$2a$10$MaBi2EyUrLDruMXJPK5Auu5IHblM2XjQThWyFtPIMjF2Fdx3kJXRq' 
WHERE username = 'instructor1';

-- Update student1 password (learn123)
UPDATE users_auth 
SET password_hash = '$2a$10$NXpp4V2A16RovBc.8tDNV.tpaWurwhEdtVkoncoCJIUZ2JerLMI.G' 
WHERE username = 'student1';

-- Update student2 password (learn123)
UPDATE users_auth 
SET password_hash = '$2a$10$NXpp4V2A16RovBc.8tDNV.tpaWurwhEdtVkoncoCJIUZ2JerLMI.G' 
WHERE username = 'student2';

-- Verify the updates
SELECT username, role, status, 
       CASE 
         WHEN password_hash IS NULL THEN 'NULL'
         WHEN password_hash = '' THEN 'EMPTY'
         WHEN password_hash LIKE '$2a$%' THEN 'VALID BCrypt'
         ELSE 'INVALID FORMAT'
       END AS hash_status
FROM users_auth;

