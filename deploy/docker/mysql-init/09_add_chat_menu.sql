USE catering_auth;

SET NAMES utf8mb4;

SET @chat_menu_id := (
    SELECT id
    FROM sys_menu
    WHERE menu_code = 'notification_chat'
    LIMIT 1
);

INSERT INTO sys_menu (
    parent_id,
    menu_name,
    menu_code,
    menu_type,
    path,
    component,
    icon,
    sort_order,
    permission,
    visible,
    status
)
SELECT
    0,
    CONVERT(UNHEX('E5AE9EE697B6E5AFB9E8AF9D') USING utf8mb4),
    'notification_chat',
    2,
    '/chat',
    'ChatView.vue',
    'Bell',
    6,
    'notification:chat',
    1,
    1
WHERE @chat_menu_id IS NULL;

SET @chat_menu_id := (
    SELECT id
    FROM sys_menu
    WHERE menu_code = 'notification_chat'
    LIMIT 1
);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT id, @chat_menu_id
FROM sys_role
WHERE role_code IN ('USER', 'STAFF', 'MANAGER', 'ADMIN');
