-- Ma-GGom Backend Schema
-- 최초 배포 시 한 번만 실행

CREATE TABLE IF NOT EXISTS member (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    role        VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',
    is_verified BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS subscription_preference (
    id            BIGSERIAL PRIMARY KEY,
    member_id     BIGINT       NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    receive_days  VARCHAR(50)  NOT NULL DEFAULT 'MON,WED,FRI',
    receive_time  TIME         NOT NULL DEFAULT '08:00:00',
    pref_regions  TEXT         NOT NULL DEFAULT '["수도권"]',
    pref_distances TEXT        NOT NULL DEFAULT '["10K","HALF"]',
    include_small BOOLEAN      NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS marathon_event (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    event_date     DATE         NOT NULL,
    region         VARCHAR(100) NOT NULL,
    distances      TEXT         NOT NULL,
    reg_start_date TIMESTAMP    NOT NULL,
    reg_end_date   TIMESTAMP,
    link_url       VARCHAR(500) NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    source_name    VARCHAR(100) NOT NULL,
    source_url     VARCHAR(500) NOT NULL,
    crawled_at_kst TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_marathon_event_status_region
    ON marathon_event (status, region);
