package org.example.notificationservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.common.Result;
import org.example.notificationservice.entity.MessageSendLog;
import org.example.notificationservice.service.MessageSendLogService;
import org.example.notificationservice.util.MessageIdGenerator;
import org.example.notificationservice.websocket.NotificationWebSocket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "WebSocket通知", description = "WebSocket实时推送接口")
@RestController
@RequestMapping("/api/notification/ws")
@RequiredArgsConstructor
public class WebSocketNotificationController {

    private final MessageSendLogService messageSendLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "推送排队相关通知")
    @PostMapping("/push/queue")
    public Result<Boolean> pushQueueNotification(@RequestBody QueueNotificationRequest request) {
        return pushNotification(request.getUserId(), request.getNotificationType(), request.getData(), "QUEUE");
    }

    @Operation(summary = "推送订单相关通知")
    @PostMapping("/push/order")
    public Result<Boolean> pushOrderNotification(@RequestBody OrderNotificationRequest request) {
        return pushNotification(request.getUserId(), request.getNotificationType(), request.getData(), "ORDER");
    }

    @Operation(summary = "获取在线用户数")
    @GetMapping("/online/count")
    public Result<Integer> getOnlineCount() {
        return Result.success(NotificationWebSocket.getOnlineCount());
    }

    @Operation(summary = "获取在线WebSocket会话数")
    @GetMapping("/online/session/count")
    public Result<Integer> getSessionCount() {
        return Result.success(NotificationWebSocket.getSessionCount());
    }

    @Operation(summary = "批量查询用户WebSocket在线状态")
    @GetMapping("/online/status")
    public Result<Map<Long, Boolean>> getOnlineStatus(@RequestParam("userIds") List<Long> userIds) {
        Map<Long, Boolean> result = new LinkedHashMap<>();
        if (userIds != null) {
            userIds.stream()
                    .filter(userId -> userId != null)
                    .distinct()
                    .forEach(userId -> result.put(userId, NotificationWebSocket.isUserOnline(userId)));
        }
        return Result.success(result);
    }

    private Result<Boolean> pushNotification(Long userId, String notificationType, Object data, String category) {
        log.info("Received notification push - category: {}, userId: {}, type: {}", category, userId, notificationType);
        if (userId == null || notificationType == null || notificationType.isBlank()) {
            return Result.error("userId and notificationType are required");
        }

        boolean sent = NotificationWebSocket.pushQueueNotification(userId, notificationType, data);
        saveSendLog(userId, notificationType, data, sent);

        if (sent) {
            log.info("Notification pushed - userId: {}, type: {}", userId, notificationType);
            return Result.success(true, "通知推送成功");
        }

        log.warn("Notification stored but user is offline - userId: {}, type: {}", userId, notificationType);
        return Result.success(false, "用户不在线，通知已记录");
    }

    private void saveSendLog(Long userId, String notificationType, Object data, boolean sent) {
        try {
            MessageSendLog logEntity = new MessageSendLog();
            logEntity.setMessageId(MessageIdGenerator.generate());
            logEntity.setTemplateCode("WS_" + notificationType);
            logEntity.setRecipientType(1);
            logEntity.setRecipientId(userId);
            logEntity.setMessageType(3);
            logEntity.setBusinessType(notificationType);
            logEntity.setBusinessId(resolveBusinessId(data));
            logEntity.setTitle(resolveTitle(notificationType));
            logEntity.setContent(toJson(data));
            logEntity.setSendStatus(sent ? 2 : 3);
            logEntity.setSendTime(LocalDateTime.now());
            logEntity.setRetryCount(0);
            logEntity.setMaxRetry(0);
            if (!sent) {
                logEntity.setErrorCode("USER_OFFLINE");
                logEntity.setErrorMsg("WebSocket user is offline");
            }
            logEntity.setChannelResponse(sent ? "WebSocket sent" : "WebSocket offline");
            logEntity.setExtraData(toJson(Map.of("notificationType", notificationType)));
            messageSendLogService.save(logEntity);
        } catch (Exception e) {
            log.warn("Failed to save notification send log - userId: {}, type: {}", userId, notificationType, e);
        }
    }

    private String resolveBusinessId(Object data) {
        if (data == null) {
            return null;
        }
        Map<?, ?> map = objectMapper.convertValue(data, Map.class);
        for (String key : new String[]{"queueNo", "orderNo", "paymentNo", "id"}) {
            Object value = map.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private String resolveTitle(String notificationType) {
        return switch (notificationType) {
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

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    @Data
    static class QueueNotificationRequest {
        private Long userId;
        private String notificationType;
        private Object data;
    }

    @Data
    static class OrderNotificationRequest {
        private Long userId;
        private String notificationType;
        private Object data;
    }
}
