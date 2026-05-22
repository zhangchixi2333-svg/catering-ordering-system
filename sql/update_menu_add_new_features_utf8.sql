-- =============================================
-- 鏇存柊鑿滃崟鏁版嵁 - 娣诲姞鏂板姛鑳介〉闈?-- 鐗堟湰: 1.1
-- 鏃ユ湡: 2026-05-21
-- 璇存槑: 娣诲姞鎴戠殑璁㈠崟銆佹敮浠樿鍗曘€佹鍙扮鐞嗙瓑鏂拌彍鍗?-- =============================================

USE catering_auth;

-- =============================================
-- 1. 鏇存柊鐜版湁鑿滃崟璺緞
-- =============================================

-- 鏇存柊"鎴戠殑璁㈠崟"璺緞锛堜粠 /orders 鏀逛负 /my-orders锛?UPDATE sys_menu SET path = '/my-orders', component = 'MyOrdersView.vue' WHERE id = 9 AND menu_code = 'order:my';

-- =============================================
-- 2. 娣诲姞鏂拌彍鍗曢」
-- =============================================

-- 娣诲姞"鏀粯璁㈠崟"鑿滃崟锛圛D: 10锛?INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) 
VALUES (10, 3, '鏀粯璁㈠崟', 'order:payment', 2, '/payment', 'PaymentView.vue', '馃挸', 3, 'order:payment', 1)
ON DUPLICATE KEY UPDATE 
    menu_name = VALUES(menu_name),
    path = VALUES(path),
    component = VALUES(component);

-- 娣诲姞"鍏ㄩ儴璁㈠崟"鑿滃崟锛圛D: 16锛?INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) 
VALUES (16, 3, '鍏ㄩ儴璁㈠崟', 'order:all', 2, '/orders', 'OrderView.vue', '馃搳', 4, 'order:all', 1)
ON DUPLICATE KEY UPDATE 
    menu_name = VALUES(menu_name),
    path = VALUES(path),
    component = VALUES(component);

-- 娣诲姞"妗屽彴绠＄悊"鑿滃崟锛圛D: 17锛?INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) 
VALUES (17, 4, '妗屽彴绠＄悊', 'shop:table', 2, '/table-management', 'TableManagementView.vue', '馃獞', 3, 'shop:table', 1)
ON DUPLICATE KEY UPDATE 
    menu_name = VALUES(menu_name),
    path = VALUES(path),
    component = VALUES(component);

-- =============================================
-- 3. 鍒犻櫎鏃х殑閲嶅鑿滃崟锛堝鏋滃瓨鍦級
-- =============================================

-- 鍒犻櫎鏃х殑"鍏ㄩ儴璁㈠崟"鑿滃崟锛堝鏋滀箣鍓嶆湁閿欒鐨処D锛?DELETE FROM sys_menu WHERE menu_code = 'order:all' AND id NOT IN (16);

-- =============================================
-- 4. 鏇存柊瑙掕壊鑿滃崟鏉冮檺
-- =============================================

-- 娓呯┖鐜版湁鐨勮鑹茶彍鍗曞叧鑱旓紙閲嶆柊鍒嗛厤锛?DELETE FROM sys_role_menu WHERE role_id IN (1, 2, 3, 4);

-- 鏅€氱敤鎴?(USER) 鏉冮檺锛氶椤点€佸湪绾跨偣椁愩€佸彇鍙锋帓闃熴€佹垜鐨勮鍗曘€佹敮浠樿鍗?INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1),   -- 棣栭〉
(1, 3),   -- 璁㈠崟绠＄悊锛堢洰褰曪級
(1, 8),   -- 鍦ㄧ嚎鐐归
(1, 9),   -- 鎴戠殑璁㈠崟
(1, 10),  -- 鏀粯璁㈠崟
(1, 2),   -- 鎺掗槦绠＄悊锛堢洰褰曪級
(1, 6);   -- 鍙栧彿鎺掗槦

-- 搴楀憳 (STAFF) 鏉冮檺锛氭櫘閫氱敤鎴?+ 鍙彿绠＄悊銆佸叏閮ㄨ鍗曘€佹鍙扮鐞?INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),   -- 棣栭〉
(2, 3),   -- 璁㈠崟绠＄悊锛堢洰褰曪級
(2, 8),   -- 鍦ㄧ嚎鐐归
(2, 9),   -- 鎴戠殑璁㈠崟
(2, 10),  -- 鏀粯璁㈠崟
(2, 16),  -- 鍏ㄩ儴璁㈠崟
(2, 2),   -- 鎺掗槦绠＄悊锛堢洰褰曪級
(2, 6),   -- 鍙栧彿鎺掗槦
(2, 7),   -- 鍙彿绠＄悊
(2, 4),   -- 搴楅摵绠＄悊锛堢洰褰曪級
(2, 17);  -- 妗屽彴绠＄悊

-- 搴楅暱 (MANAGER) 鏉冮檺锛氬簵鍛?+ 搴楅摵绠＄悊
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1),   -- 棣栭〉
(3, 3),   -- 璁㈠崟绠＄悊锛堢洰褰曪級
(3, 8),   -- 鍦ㄧ嚎鐐归
(3, 9),   -- 鎴戠殑璁㈠崟
(3, 10),  -- 鏀粯璁㈠崟
(3, 16),  -- 鍏ㄩ儴璁㈠崟
(3, 2),   -- 鎺掗槦绠＄悊锛堢洰褰曪級
(3, 6),   -- 鍙栧彿鎺掗槦
(3, 7),   -- 鍙彿绠＄悊
(3, 4),   -- 搴楅摵绠＄悊锛堢洰褰曪級
(3, 11),  -- 搴楅摵鍒楄〃
(3, 12),  -- 搴楅摵缁熻
(3, 17);  -- 妗屽彴绠＄悊

-- 瓒呯骇绠＄悊鍛?(ADMIN) 鏉冮檺锛氭墍鏈夎彍鍗?INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 2), (4, 6), (4, 7),    -- 棣栭〉銆佹帓闃熺鐞?(4, 3), (4, 8), (4, 9), (4, 10), (4, 16),  -- 璁㈠崟绠＄悊
(4, 4), (4, 11), (4, 12), (4, 17), -- 搴楅摵绠＄悊
(4, 5), (4, 13), (4, 14), (4, 15); -- 绯荤粺绠＄悊

-- =============================================
-- 5. 楠岃瘉鏇存柊缁撴灉
-- =============================================

-- 鏌ョ湅鎵€鏈夎彍鍗?SELECT id, parent_id, menu_name, menu_code, menu_type, path, icon, visible 
FROM sys_menu 
ORDER BY parent_id, sort_order;

-- 鏌ョ湅鍚勮鑹茬殑鑿滃崟鏁伴噺
SELECT 
    r.role_name,
    COUNT(rm.menu_id) as menu_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.id = rm.role_id
GROUP BY r.id, r.role_name
ORDER BY r.sort_order;

-- 鏌ョ湅鏅€氱敤鎴风殑鑿滃崟
SELECT m.* FROM sys_menu m
JOIN sys_role_menu rm ON m.id = rm.menu_id
JOIN sys_user_role ur ON rm.role_id = ur.role_id
WHERE ur.user_id = 1 AND m.status = 1
ORDER BY m.sort_order;

-- =============================================
-- 瀹屾垚鎻愮ず
-- =============================================
SELECT '鉁?鑿滃崟鏇存柊瀹屾垚锛佽閲嶅惎鍓嶇鏈嶅姟骞舵竻闄ゆ祻瑙堝櫒缂撳瓨銆? AS message;
