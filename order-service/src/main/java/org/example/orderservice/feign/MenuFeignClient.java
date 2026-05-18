package org.example.orderservice.feign;

import org.example.orderservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Menu服务Feign客户端
 * 用于调用menu-service的菜品相关接口
 */
@FeignClient(name = "menu-service", fallback = MenuFeignClientFallback.class)
public interface MenuFeignClient {

    /**
     * 根据ID获取菜品信息
     * @param id 菜品ID
     * @return 菜品信息
     */
    @GetMapping("/api/menu/item/{id}")
    Result<MenuItemInfoDTO> getMenuItemById(@PathVariable("id") Long id);

    /**
     * 更新菜品库存
     * @param id 菜品ID
     * @param stock 新库存值
     * @return 是否成功
     */
    @PutMapping("/api/menu/item/{id}/stock")
    Result<Boolean> updateStock(@PathVariable("id") Long id, @RequestParam("stock") Integer stock);

    /**
     * 菜品信息DTO
     */
    class MenuItemInfoDTO {
        private Long id;
        private String itemName;
        private java.math.BigDecimal price;
        private Integer stock;
        private Integer isAvailable; // 1-可售，0-不可售

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public java.math.BigDecimal getPrice() {
            return price;
        }

        public void setPrice(java.math.BigDecimal price) {
            this.price = price;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public Integer getIsAvailable() {
            return isAvailable;
        }

        public void setIsAvailable(Integer isAvailable) {
            this.isAvailable = isAvailable;
        }
    }
}
