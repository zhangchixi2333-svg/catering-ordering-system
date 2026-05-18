package org.example.shopservice.feign;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.example.shopservice.common.Result;
import org.example.shopservice.entity.ShopInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 店铺服务Feign客户端
 * 供其他微服务调用shop-service的接口
 */
@FeignClient(name = "shop-service", path = "/api/shop")
public interface ShopFeignClient {

    /**
     * 根据ID获取店铺信息
     * @param id 店铺ID
     * @return 店铺信息
     */
    @Operation(summary = "根据ID获取店铺信息(Feign)")
    @GetMapping("/{id}")
    Result<ShopInfo> getShopById(
            @Parameter(description = "店铺ID", example = "1")
            @PathVariable("id") Long id
    );

    /**
     * 根据编码获取店铺信息
     * @param shopCode 店铺编码
     * @return 店铺信息
     */
    @Operation(summary = "根据编码获取店铺信息(Feign)")
    @GetMapping("/code/{shopCode}")
    Result<ShopInfo> getShopByCode(
            @Parameter(description = "店铺编码", example = "SHOP001")
            @PathVariable("shopCode") String shopCode
    );
}
