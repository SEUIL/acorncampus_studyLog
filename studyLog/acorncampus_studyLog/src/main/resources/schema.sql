-- ============================================================
-- 나만의 학습 기록 블로그 플랫폼 — Oracle 19c DDL
-- 실행 전: Oracle에 "blog" 계정 생성 후 해당 계정으로 실행
-- CREATE USER blog IDENTIFIED BY blog1234;
-- GRANT CONNECT, RESOURCE, DBA TO blog;
-- ============================================================

-- ── 시퀀스 ──────────────────────────────────────────────────
CREATE SEQUENCE users_seq    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE posts_seq    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE tags_seq     START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE series_seq   START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE comments_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE reports_seq  START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ── 사용자 ──────────────────────────────────────────────────
CREATE TABLE users (
    user_id    NUMBER        PRIMARY KEY,
    email      VARCHAR2(100) NOT NULL,
    nickname   VARCHAR2(50)  NOT NULL,
    password   VARCHAR2(200) NOT NULL,   -- BCrypt 해시
    bio        VARCHAR2(500),
    avatar_url VARCHAR2(500),
    role       VARCHAR2(10)  DEFAULT 'USER' NOT NULL,    -- USER / ADMIN
    is_banned  CHAR(1)       DEFAULT 'N'    NOT NULL,    -- Y / N
    created_at TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,                                -- 소프트 삭제
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_nickname UNIQUE (nickname),
    CONSTRAINT chk_users_role    CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_banned  CHECK (is_banned IN ('Y', 'N'))
);

-- ── 시리즈 (posts보다 먼저 생성 — FK 참조) ──────────────────
CREATE TABLE series (
    series_id   NUMBER        PRIMARY KEY,
    user_id     NUMBER        NOT NULL,
    name        VARCHAR2(200) NOT NULL,
    description VARCHAR2(500),
    is_public   CHAR(1)   DEFAULT 'Y' NOT NULL,   -- Y / N
    created_at  TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_series_user   FOREIGN KEY (user_id)   REFERENCES users(user_id),
    CONSTRAINT chk_series_public CHECK (is_public IN ('Y', 'N'))
);

-- ── 게시글 ──────────────────────────────────────────────────
CREATE TABLE posts (
    post_id       NUMBER        PRIMARY KEY,
    user_id       NUMBER        NOT NULL,
    series_id     NUMBER,                          -- NULL 가능 (시리즈 없음)
    title         VARCHAR2(300) NOT NULL,
    content       CLOB,                            -- 마크다운 본문
    thumbnail_url VARCHAR2(500),
    is_public     CHAR(1)   DEFAULT 'Y' NOT NULL, -- Y / N
    view_count    NUMBER    DEFAULT 0   NOT NULL,
    created_at    TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at    TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    deleted_at    TIMESTAMP,                       -- 소프트 삭제
    CONSTRAINT fk_posts_user   FOREIGN KEY (user_id)   REFERENCES users(user_id),
    CONSTRAINT fk_posts_series FOREIGN KEY (series_id) REFERENCES series(series_id),
    CONSTRAINT chk_posts_public CHECK (is_public IN ('Y', 'N'))
);

-- ── 태그 ────────────────────────────────────────────────────
CREATE TABLE tags (
    tag_id NUMBER        PRIMARY KEY,
    name   VARCHAR2(100) NOT NULL,
    CONSTRAINT uq_tags_name UNIQUE (name)
);

-- ── 게시글-태그 N:M ─────────────────────────────────────────
CREATE TABLE post_tags (
    post_id NUMBER NOT NULL,
    tag_id  NUMBER NOT NULL,
    CONSTRAINT pk_post_tags  PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_pt_post    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_tag     FOREIGN KEY (tag_id)  REFERENCES tags(tag_id)   ON DELETE CASCADE
);

-- ── 댓글 (2depth: parent_comment_id 있으면 대댓글) ──────────
CREATE TABLE comments (
    comment_id        NUMBER         PRIMARY KEY,
    post_id           NUMBER         NOT NULL,
    user_id           NUMBER         NOT NULL,
    parent_comment_id NUMBER,                         -- NULL이면 최상위 댓글
    content           VARCHAR2(2000) NOT NULL,
    created_at        TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP,                      -- 소프트 삭제
    CONSTRAINT fk_comments_post   FOREIGN KEY (post_id)           REFERENCES posts(post_id),
    CONSTRAINT fk_comments_user   FOREIGN KEY (user_id)           REFERENCES users(user_id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id)
);

-- ── 게시글 좋아요/싫어요 ────────────────────────────────────
CREATE TABLE post_likes (
    post_id   NUMBER NOT NULL,
    user_id   NUMBER NOT NULL,
    like_type CHAR(1) NOT NULL,   -- L: 좋아요 / D: 싫어요
    CONSTRAINT pk_post_likes  PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_pl_post     FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_pl_user     FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_pl_type    CHECK (like_type IN ('L', 'D'))
);

-- ── 댓글 좋아요/싫어요 ──────────────────────────────────────
CREATE TABLE comment_likes (
    comment_id NUMBER NOT NULL,
    user_id    NUMBER NOT NULL,
    like_type  CHAR(1) NOT NULL,  -- L: 좋아요 / D: 싫어요
    CONSTRAINT pk_comment_likes PRIMARY KEY (comment_id, user_id),
    CONSTRAINT fk_cl_comment    FOREIGN KEY (comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_cl_user       FOREIGN KEY (user_id)    REFERENCES users(user_id),
    CONSTRAINT chk_cl_type      CHECK (like_type IN ('L', 'D'))
);

-- ── 신고 ────────────────────────────────────────────────────
CREATE TABLE reports (
    report_id   NUMBER         PRIMARY KEY,
    reporter_id NUMBER         NOT NULL,
    target_type VARCHAR2(10)   NOT NULL,  -- POST / COMMENT
    target_id   NUMBER         NOT NULL,
    reason      VARCHAR2(1000),
    status      VARCHAR2(15)   DEFAULT 'PENDING' NOT NULL, -- PENDING / RESOLVED / DISMISSED
    processed_by NUMBER,
    processed_at TIMESTAMP,
    created_at  TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id),
    CONSTRAINT fk_reports_processor FOREIGN KEY (processed_by) REFERENCES users(user_id),
    CONSTRAINT chk_reports_type    CHECK (target_type IN ('POST', 'COMMENT')),
    CONSTRAINT chk_reports_status  CHECK (status IN ('PENDING', 'RESOLVED', 'DISMISSED'))
);

-- ── 인덱스 ──────────────────────────────────────────────────
CREATE INDEX idx_posts_user_id    ON posts(user_id);
CREATE INDEX idx_posts_created    ON posts(created_at DESC);
CREATE INDEX idx_posts_deleted    ON posts(deleted_at);
CREATE INDEX idx_comments_post    ON comments(post_id);
CREATE INDEX idx_comments_parent  ON comments(parent_comment_id);
CREATE INDEX idx_post_tags_tag    ON post_tags(tag_id);
CREATE INDEX idx_reports_status   ON reports(status);
CREATE INDEX idx_reports_target   ON reports(target_type, target_id);
CREATE INDEX idx_series_user      ON series(user_id);

-- ── 비밀번호 재설정 토큰 ────────────────────────────────────
CREATE TABLE password_reset_tokens (
    token      VARCHAR2(64)  PRIMARY KEY,             -- SecureRandom hex 64자
    user_id    NUMBER        NOT NULL,
    expires_at TIMESTAMP     NOT NULL,                -- 생성 시각 + 30분
    used_yn    CHAR(1)       DEFAULT 'N' NOT NULL,    -- N: 미사용 / Y: 사용 완료
    CONSTRAINT fk_prt_user  FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_prt_used CHECK (used_yn IN ('Y', 'N'))
);

CREATE INDEX idx_prt_user_id ON password_reset_tokens(user_id);

-- ── 확인 쿼리 ───────────────────────────────────────────────
-- SELECT table_name FROM user_tables ORDER BY table_name;
-- SELECT sequence_name FROM user_sequences ORDER BY sequence_name;
