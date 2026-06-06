package org.example.queueservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.queueservice.common.Result;
import org.springframework.stereotype.Component;

/**
 * NotificationFeignClient熔断降级处理类
 * 当notification-service不可用时，提供友好的降级响应
 */
@Slf4j
@Component
public class NotificationFeignClientFallback implements NotificationFeignClient {

    @Override
    public Result<Boolean> pushQueueNotification(QueueNotificationRequest request) {
        log.error("调用notification-service失败，用户ID: {}, 通知类型: {}。WebSocket推送失败，但不影响主业务流程", 
                request.getUserId(), request.getNotificationType());
        // 返回成功，避免影响取号主流程
        return Result.success(false);
    }
}
