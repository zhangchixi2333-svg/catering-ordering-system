package org.example.paymentservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.common.Result;
import org.springframework.stereotype.Component;

/**
 * OrderFeignClient熔断降级处理类
 * 
 * 当Feign调用order-service失败时触发此降级逻辑
 * 常见失败原因：
 * 1. order-service未启动或已宕机
 * 2. order-service未注册到Eureka或状态不是UP
 * 3. 网络连接超时（默认连接超时2秒，读取超时5秒）
 * 4. order-service返回HTTP错误状态码（4xx/5xx）
 * 5. 请求参数不匹配导致404或400错误
 * 6. order-service内部异常导致500错误
 * 7. 负载均衡器找不到可用的服务实例
 */
@Slf4j
@Component
public class OrderFeignClientFallback implements OrderFeignClient {

    @Override
    public Result<OrderInfoDTO> getOrderByOrderNo(String orderNo) {
        log.error("========== Feign调用order-service失败 ==========");
        log.error("【调用方法】getOrderByOrderNo");
        log.error("【请求参数】订单编号: {}", orderNo);
        log.error("【目标服务】order-service (通过Eureka发现)");
        log.error("【预期路径】GET /api/order/no/{}", orderNo);
        log.error("");
        log.error("【可能原因分析】");
        log.error("  1. order-service未启动 - 请检查进程是否运行在8083端口");
        log.error("  2. Eureka注册问题 - 请确认ORDER-SERVICE在Eureka中状态为UP");
        log.error("  3. 网络超时 - order-service响应超过Feign超时配置");
        log.error("  4. HTTP错误 - order-service返回404/500等错误状态码");
        log.error("  5. 路径不匹配 - Controller路径与Feign定义不一致");
        log.error("  6. 参数错误 - 订单编号格式不正确或不存在");
        log.error("");
        log.error("【排查步骤】");
        log.error("  Step 1: 访问 http://localhost:8761 查看ORDER-SERVICE状态");
        log.error("  Step 2: 直接访问 http://localhost:8083/api/order/no/{} 测试", orderNo);
        log.error("  Step 3: 检查payment-service和order-service的日志文件");
        log.error("  Step 4: 验证Feign超时配置: connectTimeout=2000ms, readTimeout=5000ms");
        log.error("==================================================");
        return Result.error("订单服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> updateOrderStatus(Long id, Integer orderStatus, Integer paymentStatus, Integer paymentMethod) {
        log.error("========== Feign调用order-service失败 ==========");
        log.error("【调用方法】updateOrderStatus");
        log.error("【请求参数】订单ID: {}, 订单状态: {}, 支付状态: {}, 支付方式: {}", id, orderStatus, paymentStatus, paymentMethod);
        log.error("【目标服务】order-service (通过Eureka发现)");
        log.error("【预期路径】PUT /api/order/{}/status?orderStatus={}&paymentStatus={}&paymentMethod={}", id, orderStatus, paymentStatus, paymentMethod);
        log.error("");
        log.error("【可能原因分析】");
        log.error("  1. order-service未启动 - 请检查进程是否运行在8083端口");
        log.error("  2. Eureka注册问题 - 请确认ORDER-SERVICE在Eureka中状态为UP");
        log.error("  3. 网络超时 - order-service响应超过Feign超时配置");
        log.error("  4. HTTP错误 - order-service返回404/500等错误状态码");
        log.error("  5. 路径不匹配 - Controller路径与Feign定义不一致");
        log.error("  6. 接口不存在 - order-service未实现该接口或版本不匹配");
        log.error("  7. 参数类型错误 - 参数类型与Controller定义不一致");
        log.error("");
        log.error("【排查步骤】");
        log.error("  Step 1: 访问 http://localhost:8761 查看ORDER-SERVICE状态");
        log.error("  Step 2: 检查order-service是否有 /api/order/{{id}}/status 接口");
        log.error("  Step 3: 验证OrdersController中有 @PutMapping(\"/{id}/status\") 方法");
        log.error("  Step 4: 检查payment-service和order-service的日志文件");
        log.error("  Step 5: 使用curl或Postman直接测试该接口");
        log.error("==================================================");
        return Result.error("订单服务暂时不可用，请稍后重试");
    }
}