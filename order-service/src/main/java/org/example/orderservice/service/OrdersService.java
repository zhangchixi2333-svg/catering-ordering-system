package org.example.orderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.orderservice.entity.Orders;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrdersService extends IService<Orders> {
    
    /**
     * 根据订单编号获取订单
     */
    Orders getByOrderNo(String orderNo);
    
    /**
     * 根据店铺ID获取订单列表
     */
    List<Orders> getByShopId(Long shopId);
    
    /**
     * 根据用户ID获取订单列表
     */
    List<Orders> getByUserId(Long userId);
    
    /**
     * 根据状态获取订单列表
     */
    List<Orders> getByStatus(Integer orderStatus);
}
