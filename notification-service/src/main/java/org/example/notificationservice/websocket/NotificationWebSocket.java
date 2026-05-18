package org.example.notificationservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务端点
 * 用于向客户端推送实时通知
 * 
 * 连接地址: ws://localhost:8086/ws/notification/{userId}
 * 示例: ws://localhost:8086/ws/notification/1001
 */
@Slf4j
@Component
@ServerEndpoint("/ws/notification/{userId}")
public class NotificationWebSocket {

    /**
     * 存储所有在线用户的WebSocket会话
     * Key: userId, Value: Session
     */
    private static final Map<Long, Session> SESSION_MAP = new ConcurrentHashMap<>();
    
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立成功时调用
     * @param session WebSocket会话
     * @param userId 用户ID（从路径参数获取）
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        SESSION_MAP.put(userId, session);
        log.info("WebSocket连接建立 - 用户ID: {}, 当前在线用户数: {}", userId, SESSION_MAP.size());
        
        // 发送连接成功消息
        sendMessage(userId, new NotificationMessage("CONNECTED", "WebSocket连接成功", null));
    }

    /**
     * 连接关闭时调用
     * @param userId 用户ID
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        SESSION_MAP.remove(userId);
        log.info("WebSocket连接关闭 - 用户ID: {}, 当前在线用户数: {}", userId, SESSION_MAP.size());
    }

    /**
     * 收到客户端消息时调用
     * @param message 客户端发送的消息
     * @param userId 用户ID
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        log.info("收到用户 {} 的消息: {}", userId, message);
        // 可以在这里处理客户端发来的消息
    }

    /**
     * 发生错误时调用
     * @param userId 用户ID
     * @param error 错误信息
     */
    @OnError
    public void onError(@PathParam("userId") Long userId, Throwable error) {
        log.error("WebSocket发生错误 - 用户ID: {}", userId, error);
        SESSION_MAP.remove(userId);
    }

    /**
     * 向指定用户推送消息
     * @param userId 用户ID
     * @param message 消息对象
     */
    public static void sendMessage(Long userId, NotificationMessage message) {
        Session session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.getBasicRemote().sendText(jsonMessage);
                log.debug("消息推送成功 - 用户ID: {}, 消息类型: {}", userId, message.getType());
            } catch (IOException e) {
                log.error("消息推送失败 - 用户ID: {}", userId, e);
            }
        } else {
            log.warn("用户不在线，无法推送消息 - 用户ID: {}", userId);
        }
    }

    /**
     * 向指定用户推送排队相关通知
     * @param userId 用户ID
     * @param type 通知类型：QUEUE_CREATED-取号成功, QUEUE_CALLED-叫号通知, QUEUE_COMPLETED-排队完成, QUEUE_CANCELLED-排队取消
     * @param data 通知数据（排队信息）
     */
    public static void pushQueueNotification(Long userId, String type, Object data) {
        NotificationMessage message = new NotificationMessage(type, getNotificationTitle(type), data);
        sendMessage(userId, message);
    }

    /**
     * 根据通知类型获取标题
     */
    private static String getNotificationTitle(String type) {
        switch (type) {
            case "QUEUE_CREATED":
                return "取号成功";
            case "QUEUE_CALLED":
                return "叫号通知";
            case "QUEUE_COMPLETED":
                return "排队完成";
            case "QUEUE_CANCELLED":
                return "排队取消";
            default:
                return "通知";
        }
    }

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public static boolean isUserOnline(Long userId) {
        Session session = SESSION_MAP.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取当前在线用户数
     */
    public static int getOnlineCount() {
        return SESSION_MAP.size();
    }

    /**
     * 通知消息内部类
     */
    public static class NotificationMessage {
        private String type;      // 消息类型
        private String title;     // 消息标题
        private Object data;      // 消息数据
        private Long timestamp;   // 时间戳

        public NotificationMessage(String type, String title, Object data) {
            this.type = type;
            this.title = title;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
