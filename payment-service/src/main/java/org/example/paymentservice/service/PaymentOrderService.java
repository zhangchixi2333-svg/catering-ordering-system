package org.example.paymentservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.paymentservice.entity.PaymentOrder;

import java.util.List;

public interface PaymentOrderService extends IService<PaymentOrder> {
    PaymentOrder getByPaymentNo(String paymentNo);
    PaymentOrder getByOrderNo(String orderNo);
    List<PaymentOrder> getByShopId(Long shopId);
    List<PaymentOrder> getByUserId(Long userId);
    List<PaymentOrder> getByStatus(Integer paymentStatus);
}
