package org.example.paymentservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.common.Result;
import org.springframework.stereotype.Component;

/**
 * OrderFeignClient熔断降级处理类
 */
@Slf4j
@Component
public class OrderFeignClientFallback implements OrderFeignClient {

    @Override
    public Result<OrderInfoDTO> getOrderByOrderNo(String orderNo) {
        log.error("调用order-service失败，订单编号: {}", orderNo);
        log.error("请检查：1) order-service是否启动 2) 是否注册到Eureka 3) 端口是否为8083");
        return Result.error("订单服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> updateOrderStatusByPayment(String orderNo, Long orderId) {
        log.error("调用order-service失败，无法更新订单状态，订单编号: {}, 订单ID: {}", orderNo, orderId);
        return Result.error("订单服务暂时不可用，请稍后重试");
    }
}
