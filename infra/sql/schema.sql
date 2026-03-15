-- Ma-GGom Backend Schema
-- 최초 배포 시 한 번만 실행

-- ── 1. 회원/구독 ─────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS member (
    id          BIGSERIAL    PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    role        VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',
    is_verified BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS subscription_preference (
    id             BIGSERIAL    PRIMARY KEY,
    member_id      BIGINT       NOT NULL UNIQUE REFERENCES member (id) ON DELETE CASCADE,
    receive_days   VARCHAR(50)  NOT NULL DEFAULT 'MON,WED,FRI',
    receive_time   TIME         NOT NULL DEFAULT '08:00:00',
    pref_regions   TEXT         NOT NULL DEFAULT '["수도권"]',
    pref_distances TEXT         NOT NULL DEFAULT '["10K","HALF"]',
    include_small  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ── 2. 마라톤 이벤트 (API + 크롤러 공용) ──────────────────────────────────────

CREATE TABLE IF NOT EXISTS marathon_event (
    id             BIGSERIAL    PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    event_date     DATE         NOT NULL,
    region         VARCHAR(100) NOT NULL,
    distances      TEXT         NOT NULL DEFAULT '[]',
    reg_start_date TIMESTAMP    NOT NULL,
    reg_end_date   TIMESTAMP,
    is_major       BOOLEAN      NOT NULL DEFAULT FALSE,
    event_scale    VARCHAR(20)  NOT NULL DEFAULT 'UNKNOWN',
    link_url       TEXT         NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    source_name    VARCHAR(100),
    source_url     TEXT,
    crawled_at_kst TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_marathon_event_dedup
    ON marathon_event (title, event_date, region, link_url);
CREATE INDEX IF NOT EXISTS idx_marathon_event_event_date
    ON marathon_event (event_date);
CREATE INDEX IF NOT EXISTS idx_marathon_event_status
    ON marathon_event (status);
CREATE INDEX IF NOT EXISTS idx_marathon_event_event_scale
    ON marathon_event (event_scale);

-- ── 3. 크롤러 raw 데이터 ──────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS raw_crawled_data (
    id            BIGSERIAL    PRIMARY KEY,
    source        VARCHAR(100) NOT NULL,
    payload       JSONB        NOT NULL,
    parsed_status VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_raw_crawled_data_source_created_at
    ON raw_crawled_data (source, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_raw_crawled_data_parsed_status
    ON raw_crawled_data (parsed_status);

-- ── 4. 연간 재개최 추적 ───────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS marathon_event_watch (
    id                  BIGSERIAL    PRIMARY KEY,
    watch_key           VARCHAR(255) NOT NULL UNIQUE,
    base_title          VARCHAR(255) NOT NULL,
    sample_title        VARCHAR(255) NOT NULL,
    last_event_date     DATE         NOT NULL,
    expected_event_date DATE         NOT NULL,
    detected_event_date DATE,
    status              VARCHAR(20)  NOT NULL,
    source_name         VARCHAR(100),
    source_url          TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_marathon_event_watch_status
    ON marathon_event_watch (status);
CREATE INDEX IF NOT EXISTS idx_marathon_event_watch_expected_event_date
    ON marathon_event_watch (expected_event_date);

CREATE TABLE IF NOT EXISTS marathon_event_watch_seed (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    event_date  DATE         NOT NULL,
    source_name VARCHAR(100),
    source_url  TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_marathon_event_watch_seed_dedup
    ON marathon_event_watch_seed (title, event_date, COALESCE(source_name, ''), COALESCE(source_url, ''));

-- ── 5. 크롤러 소스 레지스트리 ─────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS crawler_source_registry (
    id                   BIGSERIAL    PRIMARY KEY,
    source_name          VARCHAR(100) NOT NULL UNIQUE,
    source_url           TEXT,
    source_kind          VARCHAR(30)  NOT NULL DEFAULT 'UNKNOWN',
    enabled              BOOLEAN      NOT NULL DEFAULT TRUE,
    first_seen_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_seen_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_crawled_at      TIMESTAMPTZ,
    last_success_at      TIMESTAMPTZ,
    last_status          VARCHAR(20)  NOT NULL DEFAULT 'UNKNOWN',
    last_event_count     INTEGER      NOT NULL DEFAULT 0,
    last_rare_event_count INTEGER     NOT NULL DEFAULT 0,
    success_count        INTEGER      NOT NULL DEFAULT 0,
    fail_count           INTEGER      NOT NULL DEFAULT 0,
    last_error           TEXT
);

CREATE INDEX IF NOT EXISTS idx_crawler_source_registry_status_enabled
    ON crawler_source_registry (last_status, enabled);
