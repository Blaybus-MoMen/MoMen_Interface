-- ============================================================
-- Test Data: Mentor 3 + Mentee 6 (each mentor -> 2 mentees)
-- Password: password123 (BCrypt encoded)
-- ============================================================

-- ============================================================
-- Users (3 mentors + 6 mentees)
-- ============================================================

INSERT IGNORE INTO tbl_user (LOGIN_ID, EMAIL, PASSWORD_HASH, NAME, PHONE, ROLE, IS_ACTIVE, EMAIL_VERIFIED, EMAIL_VERIFIED_AT, CREATE_DT, UPDATE_DT) VALUES
('mentor01', 'mentor01@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '김영수', '010-1111-0001', 'MENTOR', 1, 1, NOW(), NOW(), NOW()),
('mentor02', 'mentor02@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '이지현', '010-1111-0002', 'MENTOR', 1, 1, NOW(), NOW(), NOW()),
('mentor03', 'mentor03@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '박준호', '010-1111-0003', 'MENTOR', 1, 1, NOW(), NOW(), NOW()),
('mentee01', 'mentee01@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '최민서', '010-2222-0001', 'MENTEE', 1, 1, NOW(), NOW(), NOW()),
('mentee02', 'mentee02@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '정하윤', '010-2222-0002', 'MENTEE', 1, 1, NOW(), NOW(), NOW()),
('mentee03', 'mentee03@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '한서준', '010-2222-0003', 'MENTEE', 1, 1, NOW(), NOW(), NOW()),
('mentee04', 'mentee04@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '윤도현', '010-2222-0004', 'MENTEE', 1, 1, NOW(), NOW(), NOW()),
('mentee05', 'mentee05@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '강예린', '010-2222-0005', 'MENTEE', 1, 1, NOW(), NOW(), NOW()),
('mentee06', 'mentee06@test.com', '$2a$10$vRZtvvQ2AxIoqIhpH2YIpu9O7LtzndGqx492TyWVPS67l.lln/qou', '송지우', '010-2222-0006', 'MENTEE', 1, 1, NOW(), NOW(), NOW());

-- ============================================================
-- Mentors
-- ============================================================

INSERT IGNORE INTO mentors (user_id, intro, CREATE_DT, UPDATE_DT) VALUES
((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor01'), '수학/영어 전문 멘토입니다. 서울대 수학과 졸업.', NOW(), NOW()),
((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor02'), '국어/영어 전문 멘토. 연세대 국문과 재학중.', NOW(), NOW()),
((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor03'), '수학/국어 전문 멘토. 고려대 교육학과 졸업.', NOW(), NOW());

-- ============================================================
-- Mentees
-- ============================================================

-- mentor01 -> mentee01, mentee02
INSERT IGNORE INTO mentees (user_id, mentor_id, grade, cheer_message, CREATE_DT, UPDATE_DT) VALUES
((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee01'),
 (SELECT mentor_id FROM mentors WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor01')),
 '고2', '오늘도 화이팅!', NOW(), NOW()),

((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee02'),
 (SELECT mentor_id FROM mentors WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor01')),
 '고3', '수능까지 파이팅!', NOW(), NOW());

-- mentor02 -> mentee03, mentee04
INSERT IGNORE INTO mentees (user_id, mentor_id, grade, cheer_message, CREATE_DT, UPDATE_DT) VALUES
((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee03'),
 (SELECT mentor_id FROM mentors WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor02')),
 '고1', '기초부터 탄탄하게!', NOW(), NOW()),

((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee04'),
 (SELECT mentor_id FROM mentors WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor02')),
 '고2', '꾸준히 하면 된다!', NOW(), NOW());

-- mentor03 -> mentee05, mentee06
INSERT IGNORE INTO mentees (user_id, mentor_id, grade, cheer_message, CREATE_DT, UPDATE_DT) VALUES
((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee05'),
 (SELECT mentor_id FROM mentors WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor03')),
 '고3', '마지막까지 최선을!', NOW(), NOW()),

((SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee06'),
 (SELECT mentor_id FROM mentors WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentor03')),
 '고1', '시작이 반이다!', NOW(), NOW());

-- ============================================================
-- Mentee Subjects (수강 과목)
-- ============================================================

-- mentee01 (최민서, 고2): 수학, 영어
INSERT IGNORE INTO mentee_subjects (mentee_id, subject) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee01')), 'MATH'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee01')), 'ENGLISH');

-- mentee02 (정하윤, 고3): 국어, 수학, 영어
INSERT IGNORE INTO mentee_subjects (mentee_id, subject) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee02')), 'KOREAN'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee02')), 'MATH'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee02')), 'ENGLISH');

-- mentee03 (한서준, 고1): 국어, 영어
INSERT IGNORE INTO mentee_subjects (mentee_id, subject) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee03')), 'KOREAN'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee03')), 'ENGLISH');

-- mentee04 (윤도현, 고2): 국어, 수학
INSERT IGNORE INTO mentee_subjects (mentee_id, subject) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee04')), 'KOREAN'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee04')), 'MATH');

-- mentee05 (강예린, 고3): 수학, 국어
INSERT IGNORE INTO mentee_subjects (mentee_id, subject) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee05')), 'MATH'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee05')), 'KOREAN');

-- mentee06 (송지우, 고1): 영어, 수학
INSERT IGNORE INTO mentee_subjects (mentee_id, subject) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee06')), 'ENGLISH'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee06')), 'MATH');

-- ============================================================
-- Mentee Cards (특징 카드, 각 3개)
-- ============================================================

-- mentee01 (최민서): 아침형 인간, 꼼꼼함, 만점목표
INSERT IGNORE INTO mentee_cards (mentee_id, card) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee01')), '아침형 인간'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee01')), '꼼꼼함'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee01')), '만점목표');

-- mentee02 (정하윤): 끈기있는, 이해 중심, 인서울 목표
INSERT IGNORE INTO mentee_cards (mentee_id, card) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee02')), '끈기있는'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee02')), '이해 중심'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee02')), '인서울 목표');

-- mentee03 (한서준): 질문 많음, 기초부터, 슬로우 스타터
INSERT IGNORE INTO mentee_cards (mentee_id, card) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee03')), '질문 많음'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee03')), '기초부터'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee03')), '슬로우 스타터');

-- mentee04 (윤도현): 벼락치기형, 집중력 갑, 암기형
INSERT IGNORE INTO mentee_cards (mentee_id, card) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee04')), '벼락치기형'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee04')), '집중력 갑'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee04')), '암기형');

-- mentee05 (강예린): 팩폭 환영, 전교 1등 도전, 집중력 부족
INSERT IGNORE INTO mentee_cards (mentee_id, card) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee05')), '팩폭 환영'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee05')), '전교 1등 도전'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee05')), '집중력 부족');

-- mentee06 (송지우): 유리 멘탈, 내신 올킬, 수포자 탈출
INSERT IGNORE INTO mentee_cards (mentee_id, card) VALUES
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee06')), '유리 멘탈'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee06')), '내신 올킬'),
((SELECT mentee_id FROM mentees WHERE user_id = (SELECT USER_ID FROM tbl_user WHERE LOGIN_ID = 'mentee06')), '수포자 탈출');
