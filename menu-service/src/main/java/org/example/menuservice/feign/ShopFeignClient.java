package org.example.menuservice.feign;

import org.example.menuservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 店铺服务Feign客户端
 * 用于menu-service调用shop-service获取店铺信息
 */
@FeignClient(name = "shop-service", path = "/api/shop", fallback = ShopFeignClientFallback.class)
public interface ShopFeignClient {

    /**
     * 根据ID获取店铺信息
     * @param id 店铺ID
     * @return 店铺信息
     */
    @GetMapping("/{id}")
    Result<ShopInfoDTO> getShopById(@PathVariable("id") Long id);

    /**
     * 根据编码获取店铺信息
     * @param shopCode 店铺编码
     * @return 店铺信息
     */
    @GetMapping("/code/{shopCode}")
    Result<ShopInfoDTO> getShopByCode(@PathVariable("shopCode") String shopCode);

    /**
     * 店铺信息DTO（简化版，只包含menu-service需要的字段）
     */
    class ShopInfoDTO {
        private Long id;
        private String shopCode;
        private String shopName;
        private Integer shopStatus; // 1-营业中，0-休息中，2-已关闭

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getShopCode() {
            return shopCode;
        }

        public void setShopCode(String shopCode) {
            this.shopCode = shopCode;
        }

        public String getShopName() {
            return shopName;
        }

        public void setShopName(String shopName) {
            this.shopName = shopName;
        }

        public Integer getShopStatus() {
            return shopStatus;
        }

        public void setShopStatus(Integer shopStatus) {
            this.shopStatus = shopStatus;
        }

        /**
         * 判断店铺是否营业中
         */
        public boolean isOpen() {
            return this.shopStatus != null && this.shopStatus == 1;
        }
    }
}
