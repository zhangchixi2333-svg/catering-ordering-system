package org.example.notificationservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.client.UserOnlineStatusClient;
import org.example.notificationservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/notification/{userId}")
public class NotificationWebSocket {

    private static final Map<Long, Set<Session>> SESSION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Long> SESSION_USER_MAP = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static UserOnlineStatusClient userOnlineStatusClient;

    @Autowired
    public void setUserOnlineStatusClient(UserOnlineStatusClient client) {
        NotificationWebSocket.userOnlineStatusClient = client;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        String token = getFirstQueryParam(session, "token");
        if (!JwtUtil.validateTokenForUser(token, userId)) {
            log.warn("WebSocket auth failed - userId: {}, sessionId: {}", userId, session.getId());
            closeSession(session, CloseReason.CloseCodes.VIOLATED_POLICY, "Invalid token");
            return;
        }

        SESSION_MAP.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
        SESSION_USER_MAP.put(session.getId(), userId);
        syncUserOnlineStatus(userId, true);
        broadcast(new NotificationMessage("USER_STATUS", "User status changed", Map.of("userId", userId, "online", true)));
        log.info("WebSocket connected - userId: {}, sessionId: {}, onlineUsers: {}, sessions: {}",
                userId, session.getId(), SESSION_MAP.size(), getSessionCount());

        sendMessage(userId, new NotificationMessage("CONNECTED", "WebSocket connected", null));
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        removeSession(userId, session);
        syncOfflineIfNoSession(userId);
        log.info("WebSocket closed - userId: {}, sessionId: {}, onlineUsers: {}, sessions: {}",
                userId, session.getId(), SESSION_MAP.size(), getSessionCount());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        log.debug("Received WebSocket message - userId: {}, message: {}", userId, message);
        if ("PING".equalsIgnoreCase(message)) {
            sendMessage(userId, new NotificationMessage("PONG", "pong", null));
        }
    }

    @OnError
    public void onError(Session session, @PathParam("userId") Long userId, Throwable error) {
        log.error("WebSocket error - userId: {}, sessionId: {}", userId, session == null ? null : session.getId(), error);
        if (session != null) {
            removeSession(userId, session);
            syncOfflineIfNoSession(userId);
        }
    }

    public static boolean sendMessage(Long userId, NotificationMessage message) {
        Set<Session> sessions = SESSION_MAP.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("User offline, unable to push message - userId: {}", userId);
            return false;
        }

        boolean sent = false;
        for (Session session : sessions) {
            if (session == null || !session.isOpen()) {
                removeSession(userId, session);
                continue;
            }
            try {
                String jsonMessage = OBJECT_MAPPER.writeValueAsString(message);
                session.getAsyncRemote().sendText(jsonMessage);
                sent = true;
                log.debug("Message pushed - userId: {}, sessionId: {}, type: {}", userId, session.getId(), message.getType());
            } catch (IOException e) {
                log.error("Message push failed - userId: {}, sessionId: {}", userId, session.getId(), e);
                removeSession(userId, session);
            }
        }
        return sent;
    }

    public static boolean pushQueueNotification(Long userId, String type, Object data) {
        return sendMessage(userId, new NotificationMessage(type, getNotificationTitle(type), data));
    }

    public static void broadcast(NotificationMessage message) {
        SESSION_MAP.keySet().forEach(userId -> sendMessage(userId, message));
    }

    private static String getNotificationTitle(String type) {
        return switch (type) {
            case "QUEUE_CREATED" -> "取号成功";
            case "QUEUE_CALLED" -> "叫号通知";
            case "QUEUE_COMPLETED" -> "排队完成";
            case "QUEUE_CANCELLED" -> "排队取消";
            case "ORDER_CREATED" -> "订单创建成功";
            case "ORDER_PAID" -> "订单支付成功";
            case "ORDER_COMPLETED" -> "订单已完成";
            case "ORDER_CANCELLED" -> "订单已取消";
            default -> "通知";
        };
    }

    public static boolean isUserOnline(Long userId) {
        Set<Session> sessions = SESSION_MAP.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        sessions.removeIf(session -> session == null || !session.isOpen());
        if (sessions.isEmpty()) {
            SESSION_MAP.remove(userId);
            return false;
        }
        return true;
    }

    public static int getOnlineCount() {
        SESSION_MAP.keySet().forEach(NotificationWebSocket::isUserOnline);
        return SESSION_MAP.size();
    }

    public static int getSessionCount() {
        SESSION_MAP.keySet().forEach(NotificationWebSocket::isUserOnline);
        return SESSION_MAP.values().stream().mapToInt(Set::size).sum();
    }

    public static Set<Long> getOnlineUserIds() {
        SESSION_MAP.keySet().forEach(NotificationWebSocket::isUserOnline);
        return Set.copyOf(SESSION_MAP.keySet());
    }

    private static String getFirstQueryParam(Session session, String name) {
        List<String> values = session.getRequestParameterMap().get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private static void closeSession(Session session, CloseReason.CloseCode closeCode, String reason) {
        try {
            if (session != null && session.isOpen()) {
                session.close(new CloseReason(closeCode, reason));
            }
        } catch (IOException e) {
            log.warn("Failed to close WebSocket session: {}", session == null ? null : session.getId(), e);
        }
    }

    private static void removeSession(Long userId, Session session) {
        if (session == null) {
            return;
        }
        Long resolvedUserId = userId != null ? userId : SESSION_USER_MAP.remove(session.getId());
        if (resolvedUserId == null) {
            return;
        }
        SESSION_USER_MAP.remove(session.getId());
        Set<Session> sessions = SESSION_MAP.get(resolvedUserId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                SESSION_MAP.remove(resolvedUserId);
            }
        }
    }

    private static void syncOfflineIfNoSession(Long userId) {
        if (userId != null && !isUserOnline(userId)) {
            syncUserOnlineStatus(userId, false);
            broadcast(new NotificationMessage("USER_STATUS", "User status changed", Map.of("userId", userId, "online", false)));
        }
    }

    private static void syncUserOnlineStatus(Long userId, boolean online) {
        if (userOnlineStatusClient != null) {
            userOnlineStatusClient.updateOnlineStatus(userId, online);
        }
    }

    public static class NotificationMessage {
        private String type;
        private String title;
        private Object data;
        private Long timestamp;

        public NotificationMessage(String type, String title, Object data) {
            this.type = type;
            this.title = title;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

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
