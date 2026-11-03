--1. Пользователи
CREATE TABLE users
(
    id        BIGSERIAL PRIMARY KEY,
    email     VARCHAR(255) NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    role      VARCHAR(20)  NOT NULL
        CHECK (role IN ('MANAGER', 'CONTRACTOR', 'CLIENT_ORG')),
    is_active BOOLEAN      NOT NULL DEFAULT TRUE
);


-- 2. Клиентские организации
CREATE TABLE client_orgs
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    address      VARCHAR(255) NOT NULL,
    legal_entity VARCHAR(255) NOT NULL,
    rate         NUMERIC(12, 2) CHECK (rate > 0),

    user_id      BIGINT       NOT NULL UNIQUE
        REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_client_orgs_address
    ON client_orgs (address);

CREATE INDEX idx_client_orgs_name
    ON client_orgs (name);


-- 3. Исполнители
CREATE TABLE contractors
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE
        REFERENCES users (id) ON DELETE RESTRICT
);


-- 4. Должности
CREATE TABLE positions
(
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN      NOT NULL DEFAULT TRUE
);


-- 5. Заявки
CREATE TABLE requests
(
    id            BIGSERIAL PRIMARY KEY,

    client_org_id BIGINT         NOT NULL
        REFERENCES client_orgs (id) ON DELETE RESTRICT,

    position_id   BIGINT         NOT NULL
        REFERENCES positions (id) ON DELETE RESTRICT,

    -- Тариф на момент создания заявки
    hourly_rate   NUMERIC(12, 2) NOT NULL
        CHECK (hourly_rate > 0),

    work_date     DATE           NOT NULL,
    start_time    TIME           NOT NULL,
    end_time      TIME           NOT NULL,

    status        VARCHAR(20)    NOT NULL DEFAULT 'OPEN'
        CHECK (
            status IN (
                       'OPEN',
                       'AGREED',
                       'EXPIRED',
                       'CANCELLED',
                       'CLOSED'
                )
            ),

    created_at    TIMESTAMPTZ    NOT NULL
                                          DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_requests_time
        CHECK (end_time > start_time)
);

CREATE INDEX idx_requests_work_date
    ON requests (work_date);

CREATE INDEX idx_requests_status
    ON requests (status);

CREATE INDEX idx_requests_client_org
    ON requests (client_org_id);

CREATE INDEX idx_requests_position
    ON requests (position_id);

CREATE INDEX idx_requests_created_at
    ON requests (created_at DESC);


-- 6. Отклики исполнителей
CREATE TABLE bids
(
    id            BIGSERIAL PRIMARY KEY,

    request_id    BIGINT      NOT NULL
        REFERENCES requests (id) ON DELETE RESTRICT,

    contractor_id BIGINT      NOT NULL
        REFERENCES contractors (id) ON DELETE RESTRICT,

    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (
            status IN ('PENDING', 'ACCEPTED', 'REJECTED')
            ),

    created_at    TIMESTAMPTZ NOT NULL
                                       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_bid_request_contractor
        UNIQUE (request_id, contractor_id)
);

CREATE INDEX idx_bids_request
    ON bids (request_id);

CREATE INDEX idx_bids_contractor
    ON bids (contractor_id);

CREATE INDEX idx_bids_status
    ON bids (status);

-- На одну заявку можно выбрать только одного исполнителя
CREATE UNIQUE INDEX uk_one_accepted_bid_per_request
    ON bids (request_id) WHERE status = 'ACCEPTED';
