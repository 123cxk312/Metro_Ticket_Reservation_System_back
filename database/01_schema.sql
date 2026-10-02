-- Step 2: create all tables for the metro ticket reservation system.

USE metro_ticket_reservation_system;

-- User accounts. Passwords must always be stored as BCrypt hashes.
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)     NOT NULL,
    password_hash   VARCHAR(100)    NOT NULL,
    real_name       VARCHAR(50)     NULL,
    phone           VARCHAR(20)     NULL,
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1=enabled, 0=disabled',
    version         INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_phone (phone),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Metro lines.
CREATE TABLE IF NOT EXISTS metro_lines (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    line_code       VARCHAR(20)     NOT NULL,
    line_name       VARCHAR(50)     NOT NULL,
    city            VARCHAR(50)     NOT NULL,
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1=enabled, 0=disabled',
    version         INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_metro_lines_code (line_code),
    KEY idx_metro_lines_city_status (city, status),
    CONSTRAINT chk_metro_lines_status CHECK (status IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Stations. A station can belong to more than one line.
CREATE TABLE IF NOT EXISTS stations (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    station_code    VARCHAR(20)     NOT NULL,
    station_name    VARCHAR(50)     NOT NULL,
    city            VARCHAR(50)     NOT NULL,
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1=enabled, 0=disabled',
    version         INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stations_code (station_code),
    UNIQUE KEY uk_stations_city_name (city, station_name),
    KEY idx_stations_city_status (city, status),
    CONSTRAINT chk_stations_status CHECK (status IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Ordered stations on each metro line.
CREATE TABLE IF NOT EXISTS line_stations (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    line_id         BIGINT UNSIGNED NOT NULL,
    station_id      BIGINT UNSIGNED NOT NULL,
    sequence_no     INT UNSIGNED    NOT NULL COMMENT 'Station order on the line',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_line_station (line_id, station_id),
    UNIQUE KEY uk_line_sequence (line_id, sequence_no),
    KEY idx_line_stations_station (station_id),
    CONSTRAINT fk_line_stations_line
        FOREIGN KEY (line_id) REFERENCES metro_lines (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_line_stations_station
        FOREIGN KEY (station_id) REFERENCES stations (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Tickets released by administrators.
-- remaining_stock is updated atomically when a user books a ticket.
CREATE TABLE IF NOT EXISTS tickets (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ticket_no           VARCHAR(32)     NOT NULL,
    line_id             BIGINT UNSIGNED NOT NULL,
    start_station_id    BIGINT UNSIGNED NOT NULL,
    end_station_id      BIGINT UNSIGNED NOT NULL,
    direction           VARCHAR(20)     NOT NULL DEFAULT 'UP',
    travel_date         DATE            NOT NULL,
    departure_time      TIME            NOT NULL,
    arrival_time        TIME            NULL,
    price               DECIMAL(10, 2)  NOT NULL,
    total_stock         INT UNSIGNED    NOT NULL,
    remaining_stock     INT UNSIGNED    NOT NULL,
    sale_start_at       DATETIME        NOT NULL,
    sale_end_at         DATETIME        NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    version             INT UNSIGNED    NOT NULL DEFAULT 0,
    created_by          BIGINT UNSIGNED NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tickets_ticket_no (ticket_no),
    UNIQUE KEY uk_tickets_route_time (
        line_id,
        start_station_id,
        end_station_id,
        direction,
        travel_date,
        departure_time
    ),
    KEY idx_tickets_search (
        start_station_id,
        end_station_id,
        travel_date,
        status,
        departure_time
    ),
    KEY idx_tickets_sale (status, sale_start_at, sale_end_at),
    KEY idx_tickets_created_by (created_by),
    CONSTRAINT fk_tickets_line
        FOREIGN KEY (line_id) REFERENCES metro_lines (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tickets_start_station
        FOREIGN KEY (start_station_id) REFERENCES stations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_end_station
        FOREIGN KEY (end_station_id) REFERENCES stations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_tickets_price CHECK (price >= 0),
    CONSTRAINT chk_tickets_stock CHECK (
        total_stock >= 0
        AND remaining_stock >= 0
        AND remaining_stock <= total_stock
    ),
    CONSTRAINT chk_tickets_stations CHECK (start_station_id <> end_station_id),
    CONSTRAINT chk_tickets_sale_time CHECK (sale_end_at > sale_start_at),
    CONSTRAINT chk_tickets_status CHECK (
        status IN ('DRAFT', 'ON_SALE', 'SOLD_OUT', 'CLOSED')
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- User booking orders.
CREATE TABLE IF NOT EXISTS ticket_orders (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_no        VARCHAR(32)     NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    ticket_id       BIGINT UNSIGNED NOT NULL,
    quantity        INT UNSIGNED    NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10, 2)  NOT NULL,
    total_amount    DECIMAL(10, 2)  NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'BOOKED',
    version         INT UNSIGNED    NOT NULL DEFAULT 0,
    cancelled_at    DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ticket_orders_order_no (order_no),
    KEY idx_ticket_orders_user_status (user_id, status, created_at),
    KEY idx_ticket_orders_ticket (ticket_id),
    CONSTRAINT fk_ticket_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_orders_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_ticket_orders_quantity CHECK (quantity > 0),
    CONSTRAINT chk_ticket_orders_amount CHECK (
        unit_price >= 0
        AND total_amount >= 0
        AND total_amount = unit_price * quantity
    ),
    CONSTRAINT chk_ticket_orders_status CHECK (
        status IN ('BOOKED', 'REFUND_PENDING', 'REFUNDED', 'CANCELLED')
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Refund requests submitted by users and processed by administrators.
CREATE TABLE IF NOT EXISTS refund_requests (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id        BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    reason          VARCHAR(255)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    version         INT UNSIGNED    NOT NULL DEFAULT 0,
    handled_by      BIGINT UNSIGNED NULL,
    handled_at      DATETIME        NULL,
    handle_remark   VARCHAR(255)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refund_requests_order (order_id),
    KEY idx_refund_requests_status_time (status, created_at),
    KEY idx_refund_requests_user (user_id, created_at),
    KEY idx_refund_requests_handler (handled_by),
    CONSTRAINT fk_refund_requests_order
        FOREIGN KEY (order_id) REFERENCES ticket_orders (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_refund_requests_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_refund_requests_handler
        FOREIGN KEY (handled_by) REFERENCES users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_refund_requests_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
