create database edusmart;

use edusmart;

create table users(id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL UNIQUE, username VARCHAR(50) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL, enabled BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);

select * from users;

create table courses(id BIGINT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) NOT NULL, description TEXT, teacher_id BIGINT NOT NULL, student_id BIGINT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, FOREIGN KEY (teacher_id) REFERENCES users(id), FOREIGN KEY (student_id) REFERENCES users(id));

select * from courses;

update courses set student_id = 7 where id = 9;

create table assignments(id BIGINT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) NOT NULL, description TEXT, teacher_id BIGINT NOT NULL, student_id BIGINT NOT NULL, course_id BIGINT NOT NULL, status VARCHAR(20) DEFAULT 'PENDING', created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, CONSTRAINT fk_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE, CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE, CONSTRAINT fk_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE);

select * from assignments;

create table quiz(id BIGINT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) NOT NULL, description TEXT, course_id BIGINT NOT NULL, teacher_id BIGINT, total_marks INT DEFAULT 0, duration_minutes INT DEFAULT 30, start_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (course_id) REFERENCES courses(id), FOREIGN KEY (teacher_id) REFERENCES users(id));

select * from quiz;

create table questions(id BIGINT AUTO_INCREMENT PRIMARY KEY, quiz_id BIGINT NOT NULL, question_text TEXT NOT NULL, option_a VARCHAR(255) NOT NULL, option_b VARCHAR(255) NOT NULL, option_c VARCHAR(255) NOT NULL, option_d VARCHAR(255) NOT NULL, correct_option CHAR(1) NOT NULL CHECK (correct_option IN ('A','B','C','D')), marks INT DEFAULT 1, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE);

select * from questions;

create table quiz_attempts(id BIGINT AUTO_INCREMENT PRIMARY KEY, quiz_id BIGINT NOT NULL, student_id BIGINT NOT NULL, score INT DEFAULT 0, total_marks INT DEFAULT 0, completed BOOLEAN DEFAULT FALSE, started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, completed_at TIMESTAMP NULL, FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE, FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE);

select * from quiz_attempts;

create table quiz_answers(id BIGINT AUTO_INCREMENT PRIMARY KEY, attempt_id BIGINT NOT NULL, question_id BIGINT NOT NULL, selected_option CHAR(1) CHECK (selected_option IN ('A','B','C','D')), is_correct BOOLEAN, FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE, FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE);

select * from quiz_answers;

create table quiz_submission(id BIGINT AUTO_INCREMENT PRIMARY KEY, quiz_id BIGINT NOT NULL, student_id BIGINT NOT NULL, score INT, status VARCHAR(50), submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE, CONSTRAINT fk_quiz_submission_student FOREIGN KEY (student_id) REFERENCES users(id));

select * from quiz_submission;

create table quiz_submission_answers(submission_id BIGINT NOT NULL, question_id BIGINT NOT NULL, selected_option VARCHAR(5), PRIMARY KEY (submission_id, question_id), CONSTRAINT fk_submission FOREIGN KEY (submission_id) REFERENCES quiz_submission(id) ON DELETE CASCADE, CONSTRAINT fk_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE);

select * from quiz_submission_answers;

create table enrollments(id BIGINT AUTO_INCREMENT PRIMARY KEY, student_id BIGINT NOT NULL, quiz_id BIGINT NOT NULL, enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP, completed BOOLEAN DEFAULT FALSE, score DOUBLE DEFAULT NULL, FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE, UNIQUE KEY unique_enrollment (student_id, quiz_id));

select * from enrollments;

create table notifications(id BIGINT AUTO_INCREMENT PRIMARY KEY, recipient_id BIGINT NOT NULL, message VARCHAR(500) NOT NULL, `read` BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_notification_user FOREIGN KEY (recipient_id) REFERENCES users(id));

select * from notifications;

create table discussion_threads(id BIGINT NOT NULL AUTO_INCREMENT, title VARCHAR(255) NOT NULL, course_id BIGINT NOT NULL, creator_id BIGINT NOT NULL, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, PRIMARY KEY (id));

select * from discussion_threads;

update discussion_threads set creator_id = 7 where id = 1;

create table discussion_posts(id BIGINT NOT NULL AUTO_INCREMENT, content TEXT NOT NULL, thread_id BIGINT NOT NULL, author_id BIGINT NOT NULL, created_at DATETIME(6) NOT NULL, PRIMARY KEY (id), INDEX idx_thread_id (thread_id), INDEX idx_author_id (author_id));

select * from discussion_posts;

create table chat_messages(id BIGINT NOT NULL AUTO_INCREMENT, content VARCHAR(1000) NOT NULL, course_id BIGINT NOT NULL, sender_id BIGINT NOT NULL, timestamp DATETIME(6) NOT NULL, PRIMARY KEY (id), INDEX idx_course_id_timestamp (course_id, timestamp), INDEX idx_sender_id (sender_id));

select * from chat_messages;