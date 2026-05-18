package org.example.paymentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.paymentservice.entity.PaymentOrder;
import org.example.paymentservice.mapper.PaymentOrderMapper;
import org.example.paymentservice.service.PaymentOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentOrderServiceImpl extends ServiceImpl<PaymentOrderMapper, PaymentOrder> implements PaymentOrderService {
    
    @Override
    public PaymentOrder getByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getPaymentNo, paymentNo);
        return getOne(wrapper);
    }
    
    @Override
    public PaymentOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getOrderNo, orderNo);
        return getOne(wrapper);
    }
    
    @Override
    public List<PaymentOrder> getByShopId(Long shopId) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getShopId, shopId).orderByDesc(PaymentOrder::getCreatedAt);
        return list(wrapper);
    }
    
    @Override
    public List<PaymentOrder> getByUserId(Long userId) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getUserId, userId).orderByDesc(PaymentOrder::getCreatedAt);
        return list(wrapper);
    }
    
    @Override
    public List<PaymentOrder> getByStatus(Integer paymentStatus) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getPaymentStatus, paymentStatus).orderByDesc(PaymentOrder::getCreatedAt);
        return list(wrapper);
    }
}
