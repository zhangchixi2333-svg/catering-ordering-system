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
        return Result.error("订单服务暂时不可用，请稍后重试");
    }
}
