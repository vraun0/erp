-- ERPDB Schema



CREATE TABLE IF NOT EXISTS courses (
    code VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    PRIMARY KEY (code)
);

CREATE TABLE IF NOT EXISTS instructors (
    user_id INT NOT NULL,
    department VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES AuthDB.users_auth(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS students (
    user_id INT NOT NULL,
    roll_no INT NOT NULL,
    program VARCHAR(50),
    year INT,
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES AuthDB.users_auth(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS sections (
    section_id INT NOT NULL AUTO_INCREMENT,
    course_id VARCHAR(20) NOT NULL,
    instructor_id INT,
    day_time VARCHAR(50),
    room VARCHAR(50),
    capacity INT,
    semester INT,
    year INT,
    drop_deadline DATE NULL,
    PRIMARY KEY (section_id),
    FOREIGN KEY (course_id) REFERENCES courses(code) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT NOT NULL AUTO_INCREMENT,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    status VARCHAR(20),
    PRIMARY KEY (enrollment_id),
    UNIQUE INDEX unique_enrollment (student_id, section_id),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS grades (
    enrollment_id INT NOT NULL,
    component INT NOT NULL,
    score INT,
    final_score INT,
    PRIMARY KEY (enrollment_id, component),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS settings (
    id INT PRIMARY KEY DEFAULT 1,
    maintenance BOOLEAN DEFAULT FALSE,
    CONSTRAINT check_single_row CHECK (id = 1)
);

-- Insert default settings if not exists
INSERT IGNORE INTO settings (id, maintenance) VALUES (1, FALSE);

CREATE TABLE IF NOT EXISTS fees (
    fee_id INT NOT NULL AUTO_INCREMENT,
    student_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255),
    due_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    PRIMARY KEY (fee_id),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT NOT NULL AUTO_INCREMENT,
    enrollment_id INT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL, -- PRESENT, ABSENT, LATE, EXCUSED
    PRIMARY KEY (attendance_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id),
    UNIQUE KEY unique_attendance (enrollment_id, date)
);
