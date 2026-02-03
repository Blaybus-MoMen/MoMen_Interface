-- ============================================
-- Momen Database Schema (MariaDB)
-- ============================================

-- 1. 사용자 테이블
CREATE TABLE tbl_user (
    USER_ID        BIGINT       NOT NULL AUTO_INCREMENT,
    EMAIL          VARCHAR(255) UNIQUE,
    PASSWORD_HASH  VARCHAR(255),
    NAME           VARCHAR(255),
    IS_ACTIVE      TINYINT(1)   NOT NULL DEFAULT 1,
    PHONE          VARCHAR(20),
    ROLE           VARCHAR(20)  NOT NULL,
    EMAIL_VERIFIED TINYINT(1)   NOT NULL DEFAULT 0,
    EMAIL_VERIFIED_AT DATETIME,
    TERMS_AGREED_AT   DATETIME,
    PROFILE_IMAGE_URL VARCHAR(500),
    CREATE_DT      DATETIME     NOT NULL,
    UPDATE_DT      DATETIME     NOT NULL,
    PRIMARY KEY (USER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 리프레시 토큰 테이블
CREATE TABLE tbl_refresh_token (
    REFRESH_TOKEN_ID BIGINT       NOT NULL AUTO_INCREMENT,
    USER_ID          BIGINT,
    TOKEN            VARCHAR(512) UNIQUE,
    EXPIRES_AT       DATETIME,
    CREATE_DT        DATETIME     NOT NULL,
    UPDATE_DT        DATETIME     NOT NULL,
    PRIMARY KEY (REFRESH_TOKEN_ID),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (USER_ID) REFERENCES tbl_user (USER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 이메일 인증 테이블
CREATE TABLE tbl_email_verification (
    VERIFICATION_ID BIGINT       NOT NULL AUTO_INCREMENT,
    EMAIL           VARCHAR(100) NOT NULL,
    CODE            VARCHAR(6)   NOT NULL,
    EXPIRES_AT      DATETIME     NOT NULL,
    VERIFIED        TINYINT(1)   NOT NULL DEFAULT 0,
    VERIFIED_AT     DATETIME,
    ATTEMPT_COUNT   INT          NOT NULL DEFAULT 0,
    CREATE_DT       DATETIME     NOT NULL,
    UPDATE_DT       DATETIME     NOT NULL,
    PRIMARY KEY (VERIFICATION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 멘토 테이블
CREATE TABLE mentors (
    mentor_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id   BIGINT NOT NULL,
    intro     TEXT,
    CREATE_DT DATETIME NOT NULL,
    UPDATE_DT DATETIME NOT NULL,
    PRIMARY KEY (mentor_id),
    CONSTRAINT fk_mentor_user FOREIGN KEY (user_id) REFERENCES tbl_user (USER_ID),
    CONSTRAINT uk_mentor_user UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 멘티 테이블
CREATE TABLE mentees (
    mentee_id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    mentor_id         BIGINT,
    grade             VARCHAR(20),
    target_university VARCHAR(100),
    CREATE_DT         DATETIME     NOT NULL,
    UPDATE_DT         DATETIME     NOT NULL,
    PRIMARY KEY (mentee_id),
    CONSTRAINT fk_mentee_user   FOREIGN KEY (user_id)   REFERENCES tbl_user (USER_ID),
    CONSTRAINT fk_mentee_mentor FOREIGN KEY (mentor_id)  REFERENCES mentors (mentor_id),
    CONSTRAINT uk_mentee_user UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 플래너 테이블
CREATE TABLE planners (
    planner_id      BIGINT   NOT NULL AUTO_INCREMENT,
    mentee_id       BIGINT   NOT NULL,
    planner_date    DATE     NOT NULL,
    student_comment TEXT,
    sentiment_score DOUBLE,
    mood_emoji      VARCHAR(10),
    CREATE_DT       DATETIME NOT NULL,
    UPDATE_DT       DATETIME NOT NULL,
    PRIMARY KEY (planner_id),
    CONSTRAINT fk_planner_mentee FOREIGN KEY (mentee_id) REFERENCES mentees (mentee_id),
    CONSTRAINT uk_planner_mentee_date UNIQUE (mentee_id, planner_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 할 일(Todo) 테이블
CREATE TABLE todos (
    todo_id          BIGINT       NOT NULL AUTO_INCREMENT,
    planner_id       BIGINT       NOT NULL,
    title            VARCHAR(200) NOT NULL,
    subject          VARCHAR(20)  NOT NULL,
    goal_description TEXT,
    study_time       INT,
    is_completed     TINYINT(1)   DEFAULT 0,
    is_fixed         TINYINT(1)   DEFAULT 0,
    created_by       BIGINT       NOT NULL,
    CREATE_DT        DATETIME     NOT NULL,
    UPDATE_DT        DATETIME     NOT NULL,
    PRIMARY KEY (todo_id),
    CONSTRAINT fk_todo_planner FOREIGN KEY (planner_id) REFERENCES planners (planner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 피드백 테이블
CREATE TABLE feedbacks (
    feedback_id       BIGINT   NOT NULL AUTO_INCREMENT,
    planner_id        BIGINT   NOT NULL,
    mentor_id         BIGINT   NOT NULL,
    ai_generated_draft TEXT,
    is_ai_adopted     TINYINT(1) DEFAULT 0,
    korean_summary    TEXT,
    math_summary      TEXT,
    english_summary   TEXT,
    total_review      TEXT,
    CREATE_DT         DATETIME NOT NULL,
    UPDATE_DT         DATETIME NOT NULL,
    PRIMARY KEY (feedback_id),
    CONSTRAINT fk_feedback_planner FOREIGN KEY (planner_id) REFERENCES planners (planner_id),
    CONSTRAINT fk_feedback_mentor  FOREIGN KEY (mentor_id)  REFERENCES mentors (mentor_id),
    CONSTRAINT uk_feedback_planner UNIQUE (planner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 과제 제출 테이블
CREATE TABLE assignment_submissions (
    submission_id       BIGINT       NOT NULL AUTO_INCREMENT,
    todo_id             BIGINT       NOT NULL,
    file_url            VARCHAR(500) NOT NULL,
    submitted_at        DATETIME,
    ai_analysis_status  VARCHAR(20)  DEFAULT 'PENDING',
    study_density_score INT,
    ai_check_comment    TEXT,
    CREATE_DT           DATETIME     NOT NULL,
    UPDATE_DT           DATETIME     NOT NULL,
    PRIMARY KEY (submission_id),
    CONSTRAINT fk_submission_todo FOREIGN KEY (todo_id) REFERENCES todos (todo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 과제 자료 테이블
CREATE TABLE assignment_materials (
    material_id BIGINT       NOT NULL AUTO_INCREMENT,
    todo_id     BIGINT       NOT NULL,
    file_url    VARCHAR(500) NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    CREATE_DT   DATETIME     NOT NULL,
    UPDATE_DT   DATETIME     NOT NULL,
    PRIMARY KEY (material_id),
    CONSTRAINT fk_material_todo FOREIGN KEY (todo_id) REFERENCES todos (todo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 오답노트 테이블
CREATE TABLE mistake_notes (
    note_id              BIGINT       NOT NULL AUTO_INCREMENT,
    mentee_id            BIGINT       NOT NULL,
    todo_id              BIGINT,
    question_image_url   VARCHAR(500),
    ai_generated_question TEXT,
    is_solved            TINYINT(1)   DEFAULT 0,
    CREATE_DT            DATETIME     NOT NULL,
    UPDATE_DT            DATETIME     NOT NULL,
    PRIMARY KEY (note_id),
    CONSTRAINT fk_mistake_note_mentee FOREIGN KEY (mentee_id) REFERENCES mentees (mentee_id),
    CONSTRAINT fk_mistake_note_todo   FOREIGN KEY (todo_id)   REFERENCES todos (todo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. 집중 세션 테이블
CREATE TABLE focus_sessions (
    session_id       BIGINT   NOT NULL AUTO_INCREMENT,
    mentee_id        BIGINT   NOT NULL,
    start_time       DATETIME,
    end_time         DATETIME,
    drowsiness_count INT      DEFAULT 0,
    phone_use_count  INT      DEFAULT 0,
    focus_score      INT,
    CREATE_DT        DATETIME NOT NULL,
    UPDATE_DT        DATETIME NOT NULL,
    PRIMARY KEY (session_id),
    CONSTRAINT fk_focus_session_mentee FOREIGN KEY (mentee_id) REFERENCES mentees (mentee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 구술 테스트 테이블
CREATE TABLE oral_tests (
    test_id              BIGINT       NOT NULL AUTO_INCREMENT,
    mentee_id            BIGINT       NOT NULL,
    topic                VARCHAR(255) NOT NULL,
    audio_url            VARCHAR(500),
    transcription        TEXT,
    ai_accuracy_score    INT,
    ai_feedback_comment  TEXT,
    CREATE_DT            DATETIME     NOT NULL,
    UPDATE_DT            DATETIME     NOT NULL,
    PRIMARY KEY (test_id),
    CONSTRAINT fk_oral_test_mentee FOREIGN KEY (mentee_id) REFERENCES mentees (mentee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. 멘토링 채팅 로그 테이블
CREATE TABLE chat_logs (
    chat_id          BIGINT      NOT NULL AUTO_INCREMENT,
    mentee_id        BIGINT      NOT NULL,
    role             VARCHAR(20) NOT NULL,
    message_content  TEXT        NOT NULL,
    related_todo_id  BIGINT,
    CREATE_DT        DATETIME    NOT NULL,
    UPDATE_DT        DATETIME    NOT NULL,
    PRIMARY KEY (chat_id),
    CONSTRAINT fk_chat_log_mentee FOREIGN KEY (mentee_id)       REFERENCES mentees (mentee_id),
    CONSTRAINT fk_chat_log_todo   FOREIGN KEY (related_todo_id) REFERENCES todos (todo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. OpenAI 채팅 로그 테이블
CREATE TABLE tbl_openai_chat_log (
    CHAT_LOG_ID       BIGINT       NOT NULL AUTO_INCREMENT,
    JOB_ID            VARCHAR(36)  NOT NULL UNIQUE,
    USER_ID           BIGINT,
    MODEL             VARCHAR(50)  NOT NULL,
    SYSTEM_PROMPT     TEXT,
    USER_PROMPT       TEXT         NOT NULL,
    ASSISTANT_RESPONSE TEXT,
    TEMPERATURE       DECIMAL(3,2),
    MAX_TOKENS        INT,
    TOKENS_USED       INT,
    PROMPT_TOKENS     INT,
    COMPLETION_TOKENS INT,
    STATUS            VARCHAR(20)  NOT NULL,
    ERROR_MESSAGE     TEXT,
    ERROR_CODE        VARCHAR(50),
    METADATA          JSON,
    CREATE_DT         DATETIME     NOT NULL,
    UPDATE_DT         DATETIME     NOT NULL,
    PRIMARY KEY (CHAT_LOG_ID),
    CONSTRAINT fk_openai_chat_user FOREIGN KEY (USER_ID) REFERENCES tbl_user (USER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. DALL-E 이미지 생성 로그 테이블
CREATE TABLE tbl_dalle_generation_log (
    DALLE_LOG_ID           BIGINT        NOT NULL AUTO_INCREMENT,
    JOB_ID                 VARCHAR(36)   NOT NULL UNIQUE,
    USER_ID                BIGINT,
    MODEL                  VARCHAR(50)   NOT NULL,
    PROMPT                 TEXT          NOT NULL,
    REVISED_PROMPT         TEXT,
    SIZE                   VARCHAR(20),
    QUALITY                VARCHAR(20),
    STYLE                  VARCHAR(20),
    SEED                   BIGINT,
    IMAGE_URL              VARCHAR(1000),
    B64_JSON               TEXT,
    STATUS                 VARCHAR(20)   NOT NULL,
    ERROR_MESSAGE          TEXT,
    ERROR_CODE             VARCHAR(50),
    COPYRIGHT_FLAG         TINYINT(1),
    SAFETY_FILTER_TRIGGERED TINYINT(1),
    METADATA               JSON,
    CREATE_DT              DATETIME      NOT NULL,
    UPDATE_DT              DATETIME      NOT NULL,
    PRIMARY KEY (DALLE_LOG_ID),
    CONSTRAINT fk_dalle_log_user FOREIGN KEY (USER_ID) REFERENCES tbl_user (USER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 인덱스
-- ============================================
CREATE INDEX idx_user_email ON tbl_user (EMAIL);
CREATE INDEX idx_user_role ON tbl_user (ROLE);

CREATE INDEX idx_planner_mentee_date ON planners (mentee_id, planner_date);

CREATE INDEX idx_todo_planner ON todos (planner_id);
CREATE INDEX idx_todo_subject ON todos (subject);

CREATE INDEX idx_feedback_planner ON feedbacks (planner_id);

CREATE INDEX idx_submission_todo ON assignment_submissions (todo_id);

CREATE INDEX idx_mistake_note_mentee ON mistake_notes (mentee_id);

CREATE INDEX idx_focus_session_mentee ON focus_sessions (mentee_id);

CREATE INDEX idx_chat_log_mentee ON chat_logs (mentee_id);
CREATE INDEX idx_chat_log_create_dt ON chat_logs (CREATE_DT);

CREATE INDEX idx_openai_chat_user ON tbl_openai_chat_log (USER_ID);
CREATE INDEX idx_openai_chat_job ON tbl_openai_chat_log (JOB_ID);

CREATE INDEX idx_dalle_log_user ON tbl_dalle_generation_log (USER_ID);
CREATE INDEX idx_dalle_log_job ON tbl_dalle_generation_log (JOB_ID);
