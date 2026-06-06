package org.example.queueservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.queueservice.common.Result;
import org.springframework.stereotype.Component;

/**
 * ShopFeignClient熔断降级处理类
 */
@Slf4j
@Component
public class ShopFeignClientFallback implements ShopFeignClient {

    @Override
    public Result<ShopInfoDTO> getShopById(Long id) {
        log.error("调用shop-service失败，店铺ID: {}。请检查：1.shop-service是否启动 2.Eureka中服务是否正常注册 3.网络连接是否正常", id);
        return Result.error("店铺服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<ShopInfoDTO> getShopByCode(String shopCode) {
        log.error("调用shop-service失败，店铺编码: {}。请检查：1.shop-service是否启动 2.Eureka中服务是否正常注册 3.网络连接是否正常", shopCode);
        return Result.error("店铺服务暂时不可用，请稍后重试");
    }
}
