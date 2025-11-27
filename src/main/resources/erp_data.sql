-- Seed Data for ERP System

-- Courses
INSERT IGNORE INTO courses (code, title, credits) VALUES 
('CS101', 'Introduction to Computer Science', 4),
('CS102', 'Data Structures and Algorithms', 4),
('MATH101', 'Calculus I', 3),
('PHY101', 'Physics I', 4),
('HIST101', 'World History', 3),
('ENG101', 'English Composition', 3);

-- Sections (Assuming Instructor ID 1002 exists)
-- Semester 1, 2025
INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year, drop_deadline) VALUES 
('CS101', 1002, 'Mon/Wed 09:00-10:30', 'Room 101', 30, 1, 2025, '2025-09-30'),
('CS102', 1002, 'Tue/Thu 11:00-12:30', 'Room 102', 30, 1, 2025, '2025-09-30'),
('MATH101', 1002, 'Mon/Wed 14:00-15:30', 'Room 201', 40, 1, 2025, '2025-09-30'),
('PHY101', 1002, 'Tue/Thu 09:00-10:30', 'Lab 301', 25, 1, 2025, '2025-09-30'),
('HIST101', 1002, 'Fri 10:00-13:00', 'Hall A', 60, 1, 2025, '2025-09-30');

-- Enrollments for Student 1001 (Assuming Student ID 1001 exists)
-- Enroll in CS101 (Section 1) and MATH101 (Section 3)
INSERT INTO enrollments (student_id, section_id, status) VALUES 
(1001, 1, 'ENROLLED'),
(1001, 3, 'ENROLLED');

-- Grades for Student 1001 in CS101 (Section 1)
-- Components: 1=Midterm, 2=Final, 3=Assignment (Example mapping)
INSERT INTO grades (enrollment_id, component, score, final_score) VALUES 
(1, 1, 85, NULL), -- Midterm
(1, 3, 90, NULL); -- Assignment

-- Fees for Student 1001
INSERT INTO fees (student_id, amount, description, due_date, status) VALUES 
(1001, 5000.00, 'Tuition Fee - Sem 1 2025', '2025-08-15', 'PENDING'),
(1001, 200.00, 'Library Fee', '2025-08-15', 'PAID');
