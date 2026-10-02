-- Step 4: verify that the database and base data were created.

USE metro_ticket_reservation_system;

SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL
SELECT 'metro_lines', COUNT(*) FROM metro_lines
UNION ALL
SELECT 'stations', COUNT(*) FROM stations
UNION ALL
SELECT 'line_stations', COUNT(*) FROM line_stations
UNION ALL
SELECT 'tickets', COUNT(*) FROM tickets
UNION ALL
SELECT 'ticket_orders', COUNT(*) FROM ticket_orders
UNION ALL
SELECT 'refund_requests', COUNT(*) FROM refund_requests;

SELECT
    t.ticket_no,
    l.line_name,
    start_s.station_name AS start_station,
    end_s.station_name AS end_station,
    t.travel_date,
    t.departure_time,
    t.price,
    t.remaining_stock,
    t.status
FROM tickets t
JOIN metro_lines l ON l.id = t.line_id
JOIN stations start_s ON start_s.id = t.start_station_id
JOIN stations end_s ON end_s.id = t.end_station_id
ORDER BY t.travel_date, t.departure_time;

-- When booking is implemented, use one conditional UPDATE inside a transaction.
-- This prevents overselling even when many users book at the same time:
--
-- UPDATE tickets
-- SET remaining_stock = remaining_stock - 1
-- WHERE id = ?
--   AND status = 'ON_SALE'
--   AND remaining_stock >= 1;
--
-- If affected rows = 0, the ticket is unavailable and the order must not be created.
