package org.example.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.orderservice.entity.Orders;
import org.example.orderservice.mapper.OrdersMapper;
import org.example.orderservice.service.OrdersService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单服务实现类
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    
    @Override
    public Orders getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        return getOne(wrapper);
    }
    
    @Override
    public List<Orders> getByShopId(Long shopId) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getShopId, shopId)
               .orderByDesc(Orders::getCreatedAt);
        return list(wrapper);
    }
    
    @Override
    public List<Orders> getByUserId(Long userId) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId)
               .orderByDesc(Orders::getCreatedAt);
        return list(wrapper);
    }
    
    @Override
    public List<Orders> getByStatus(Integer orderStatus) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderStatus, orderStatus)
               .orderByDesc(Orders::getCreatedAt);
        return list(wrapper);
    }
}
