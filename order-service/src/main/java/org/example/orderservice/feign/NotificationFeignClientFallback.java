package org.example.orderservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.common.Result;
import org.springframework.stereotype.Component;

/**
 * NotificationFeignClient熔断降级处理类
 * 当notification-service不可用时，提供友好的降级响应
 */
@Slf4j
@Component
public class NotificationFeignClientFallback implements NotificationFeignClient {

    @Override
    public Result<Boolean> pushOrderNotification(OrderNotificationRequest request) {
        log.error("调用notification-service失败，用户ID: {}, 通知类型: {}。通知推送失败，但不影响订单创建主流程", 
                request.getUserId(), request.getNotificationType());
        // 返回成功，避免影响订单创建主流程
        return Result.success(false);
    }
}
