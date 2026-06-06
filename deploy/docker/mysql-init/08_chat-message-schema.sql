CREATE DATABASE IF NOT EXISTS notification_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE notification_service;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  message_id VARCHAR(64) NOT NULL COMMENT 'message id',
  sender_id BIGINT NOT NULL COMMENT 'sender user id',
  sender_name VARCHAR(100) DEFAULT NULL COMMENT 'sender display name',
  sender_role VARCHAR(50) DEFAULT NULL COMMENT 'sender role',
  shop_id BIGINT DEFAULT NULL COMMENT 'shop id',
  content TEXT NOT NULL COMMENT 'message content',
  delivered TINYINT NOT NULL DEFAULT 0 COMMENT 'delivered to at least one recipient',
  send_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'send time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_chat_message_id (message_id),
  KEY idx_chat_sender_time (sender_id, send_time),
  KEY idx_chat_shop_time (shop_id, send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='chat message';

CREATE TABLE IF NOT EXISTS chat_message_recipient (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  message_id VARCHAR(64) NOT NULL COMMENT 'message id',
  recipient_id BIGINT NOT NULL COMMENT 'recipient user id',
  delivered TINYINT NOT NULL DEFAULT 0 COMMENT 'realtime delivered',
  delivered_time DATETIME DEFAULT NULL COMMENT 'delivered time',
  read_status TINYINT NOT NULL DEFAULT 0 COMMENT '0-unread, 1-read',
  read_time DATETIME DEFAULT NULL COMMENT 'read time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_chat_message_recipient (message_id, recipient_id),
  KEY idx_chat_recipient_time (recipient_id, created_at),
  KEY idx_chat_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='chat message recipient';
