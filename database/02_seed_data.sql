-- Step 3: load optional demonstration data.
-- This file does not create an administrator account.
-- The backend will create the first administrator with a BCrypt password hash.

USE metro_ticket_reservation_system;

INSERT IGNORE INTO metro_lines (id, line_code, line_name, city, status)
VALUES
    (1, 'L1', 'Metro Line 1', 'Demo City', 1),
    (2, 'L2', 'Metro Line 2', 'Demo City', 1),
    (3, 'L3', 'Metro Line 3', 'Demo City', 1);

INSERT IGNORE INTO stations (id, station_code, station_name, city, status)
VALUES
    (1, 'S001', 'Central Station', 'Demo City', 1),
    (2, 'S002', 'City Hall', 'Demo City', 1),
    (3, 'S003', 'Technology Park', 'Demo City', 1),
    (4, 'S004', 'University', 'Demo City', 1),
    (5, 'S005', 'Museum', 'Demo City', 1),
    (6, 'S006', 'Sports Center', 'Demo City', 1),
    (7, 'S007', 'Airport', 'Demo City', 1),
    (8, 'S008', 'Business District', 'Demo City', 1);

INSERT IGNORE INTO line_stations (line_id, station_id, sequence_no)
VALUES
    (1, 1, 1),
    (1, 2, 2),
    (1, 3, 3),
    (1, 4, 4),
    (2, 5, 1),
    (2, 6, 2),
    (2, 7, 3),
    (3, 1, 1),
    (3, 8, 2),
    (3, 6, 3);

INSERT IGNORE INTO tickets (
    ticket_no,
    line_id,
    start_station_id,
    end_station_id,
    direction,
    travel_date,
    departure_time,
    arrival_time,
    price,
    total_stock,
    remaining_stock,
    sale_start_at,
    sale_end_at,
    status,
    created_by
)
VALUES
    (
        'DEMO-L1-001',
        1,
        1,
        4,
        'UP',
        DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY),
        '08:30:00',
        '09:00:00',
        4.00,
        100,
        100,
        NOW(),
        DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY),
        'ON_SALE',
        NULL
    ),
    (
        'DEMO-L2-001',
        2,
        5,
        7,
        'UP',
        DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY),
        '09:00:00',
        '09:35:00',
        5.00,
        80,
        80,
        NOW(),
        DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY),
        'ON_SALE',
        NULL
    ),
    (
        'DEMO-L3-001',
        3,
        1,
        6,
        'UP',
        DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY),
        '10:00:00',
        '10:40:00',
        6.00,
        120,
        120,
        NOW(),
        DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY),
        'ON_SALE',
        NULL
    );
