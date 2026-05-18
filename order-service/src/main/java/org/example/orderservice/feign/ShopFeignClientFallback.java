package org.example.orderservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.common.Result;
import org.springframework.stereotype.Component;

/**
 * ShopFeignClient熔断降级处理类
 * 当shop-service不可用时，提供友好的降级响应
 */
@Slf4j
@Component
public class ShopFeignClientFallback implements ShopFeignClient {

    @Override
    public Result<ShopInfoDTO> getShopById(Long id) {
        log.error("调用shop-service失败，店铺ID: {}", id);
        return Result.error("店铺服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<ShopInfoDTO> getShopByCode(String shopCode) {
        log.error("调用shop-service失败，店铺编码: {}", shopCode);
        return Result.error("店铺服务暂时不可用，请稍后重试");
    }
}
