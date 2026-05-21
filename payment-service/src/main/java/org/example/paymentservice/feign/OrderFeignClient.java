package org.example.paymentservice.feign;

import org.example.paymentservice.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务Feign客户端
 * 用于payment-service调用order-service获取订单信息
 */
@FeignClient(name = "order-service", path = "/api/order", fallback = OrderFeignClientFallback.class)
public interface OrderFeignClient {

    /**
     * 根据订单编号获取订单信息
     * @param orderNo 订单编号
     * @return 订单信息
     */
    @GetMapping("/no/{orderNo}")
    Result<OrderInfoDTO> getOrderByOrderNo(@PathVariable("orderNo") String orderNo);

    /**
     * 支付成功后更新订单状态为"待接单"
     * @param id 订单ID
     * @param orderStatus 订单状态（固定传1-待接单）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    Result<Boolean> updateOrderStatus(
        @PathVariable("id") Long id,
        @RequestParam("orderStatus") Integer orderStatus
    );

    /**
     * 订单信息DTO（简化版，只包含payment-service需要的字段）
     */
    class OrderInfoDTO {
        private Long id;
        private String orderNo;
        private Long shopId;
        private java.math.BigDecimal actualAmount;
        private Integer paymentStatus; // 0-未支付，1-已支付

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Long getShopId() {
            return shopId;
        }

        public void setShopId(Long shopId) {
            this.shopId = shopId;
        }

        public java.math.BigDecimal getActualAmount() {
            return actualAmount;
        }

        public void setActualAmount(java.math.BigDecimal actualAmount) {
            this.actualAmount = actualAmount;
        }

        public Integer getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(Integer paymentStatus) {
            this.paymentStatus = paymentStatus;
        }
    }
}
