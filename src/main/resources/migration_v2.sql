-- Migration Script v2: Fix user_id type mismatch and add constraints
-- This script migrates the ERP database to align with Auth database schema

-- WARNING: This will modify existing data. Backup your database before running!
-- Usage: mariadb -u root -p ERPDB < migration_v2.sql

USE ERPDB;

-- Step 1: Backup existing data (optional, but recommended)
-- CREATE TABLE students_backup AS SELECT * FROM students;
-- CREATE TABLE instructors_backup AS SELECT * FROM instructors;

-- Step 2: Drop existing foreign keys if any (likely none, but safe to try)
ALTER TABLE sections DROP FOREIGN KEY IF EXISTS sections_ibfk_1;
ALTER TABLE sections DROP FOREIGN KEY IF EXISTS sections_ibfk_2;

-- Step 3: Modify user_id columns from VARCHAR(50) to INT
-- This assumes user_id values in ERP DB are numeric strings that can be converted to INT
-- If you have non-numeric user_ids, this will fail - you'll need custom migration

ALTER TABLE students MODIFY COLUMN user_id INT NOT NULL;
ALTER TABLE instructors MODIFY COLUMN user_id INT NOT NULL;
ALTER TABLE sections MODIFY COLUMN instructor_id INT NULL;
ALTER TABLE enrollments MODIFY COLUMN student_id INT NOT NULL;

-- Step 4: Add foreign key constraints linking to Auth DB
-- Note: Cross-database foreign keys in MariaDB require explicit database name
ALTER TABLE students 
    ADD CONSTRAINT fk_students_user 
    FOREIGN KEY (user_id) REFERENCES AuthDB.users_auth(user_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

ALTER TABLE instructors 
    ADD CONSTRAINT fk_instructors_user 
    FOREIGN KEY (user_id) REFERENCES AuthDB.users_auth(user_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

-- Step 5: Add unique constraint to prevent duplicate enrollments
-- First, remove any existing duplicates (keeping the first occurrence)
DELETE e1 FROM enrollments e1
INNER JOIN enrollments e2 
WHERE e1.enrollment_id > e2.enrollment_id 
  AND e1.student_id = e2.student_id 
  AND e1.section_id = e2.section_id;

-- Now add the unique constraint
ALTER TABLE enrollments 
    ADD UNIQUE INDEX unique_enrollment (student_id, section_id);

-- Step 6: Add drop_deadline column to sections
ALTER TABLE sections 
    ADD COLUMN drop_deadline DATE NULL AFTER year;

-- Step 7: Re-add foreign keys for sections with proper constraints
ALTER TABLE sections 
    ADD CONSTRAINT fk_sections_course 
    FOREIGN KEY (course_id) REFERENCES courses(code)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

ALTER TABLE sections 
    ADD CONSTRAINT fk_sections_instructor 
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- Step 8: Update enrollments foreign keys with cascade behavior
ALTER TABLE enrollments DROP FOREIGN KEY IF EXISTS enrollments_ibfk_1;
ALTER TABLE enrollments DROP FOREIGN KEY IF EXISTS enrollments_ibfk_2;

ALTER TABLE enrollments 
    ADD CONSTRAINT fk_enrollments_student 
    FOREIGN KEY (student_id) REFERENCES students(user_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

ALTER TABLE enrollments 
    ADD CONSTRAINT fk_enrollments_section 
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

-- Step 9: Update grades foreign key
ALTER TABLE grades DROP FOREIGN KEY IF EXISTS grades_ibfk_1;

ALTER TABLE grades 
    ADD CONSTRAINT fk_grades_enrollment 
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- Step 10: Verify changes
SELECT 'Migration completed successfully!' AS status;

-- Verification queries (commented out - uncomment to run)
-- DESCRIBE students;
-- DESCRIBE instructors;
-- DESCRIBE sections;
-- DESCRIBE enrollments;
-- SHOW CREATE TABLE students;
-- SHOW CREATE TABLE enrollments;
