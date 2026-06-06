-- Sync database support for realtime chat and online/offline status.
-- Safe to run repeatedly.

USE catering_auth;

SET NAMES utf8mb4;

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'is_online'
);
SET @sql := IF(
    @column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN is_online TINYINT DEFAULT 0 COMMENT ''online status: 0-offline, 1-online'' AFTER status',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND INDEX_NAME = 'idx_is_online'
);
SET @sql := IF(
    @index_exists = 0,
    'ALTER TABLE sys_user ADD INDEX idx_is_online (is_online)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- After service restart there are no live WebSocket sessions yet, so clear stale online flags.
UPDATE sys_user SET is_online = 0 WHERE is_online IS NULL OR is_online <> 0;

USE notification_service;

SET NAMES utf8mb4;

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'message_send_log'
      AND INDEX_NAME = 'idx_chat_conversation'
);
SET @sql := IF(
    @index_exists = 0,
    'ALTER TABLE message_send_log ADD INDEX idx_chat_conversation (business_type, business_id, send_time)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'message_send_log'
      AND INDEX_NAME = 'idx_chat_recent'
);
SET @sql := IF(
    @index_exists = 0,
    'ALTER TABLE message_send_log ADD INDEX idx_chat_recent (business_type, recipient_id, send_time)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Close stale session rows left by browser/process crashes. Runtime truth is the active WebSocket map.
UPDATE websocket_session
SET status = 0,
    disconnect_time = COALESCE(disconnect_time, NOW())
WHERE status = 1;
