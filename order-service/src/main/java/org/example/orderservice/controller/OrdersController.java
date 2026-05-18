package org.example.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.common.Result;
import org.example.orderservice.dto.OrderCreateRequest;
import org.example.orderservice.entity.Orders;
import org.example.orderservice.service.OrdersService;
import org.example.orderservice.util.OrderNoGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理控制器
 */
@Tag(name = "订单管理", description = "订单的增删改查和状态管理")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @Operation(summary = "获取所有订单列表")
    @GetMapping("/list")
    public Result<List<Orders>> listOrders() {
        List<Orders> orders = ordersService.list();
        return Result.success(orders);
    }

    @Operation(summary = "根据ID获取订单详情")
    @GetMapping("/{id}")
    public Result<Orders> getOrderById(
            @Parameter(description = "订单ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @Operation(summary = "根据订单编号获取订单")
    @GetMapping("/no/{orderNo}")
    public Result<Orders> getOrderByNo(
            @Parameter(description = "订单编号", example = "ORD2026051700001", required = true)
            @PathVariable("orderNo") String orderNo) {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @Operation(summary = "根据店铺ID获取订单列表")
    @GetMapping("/shop/{shopId}")
    public Result<List<Orders>> getOrdersByShop(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<Orders> orders = ordersService.getByShopId(shopId);
        return Result.success(orders);
    }

    @Operation(summary = "根据用户ID获取订单列表")
    @GetMapping("/user/{userId}")
    public Result<List<Orders>> getOrdersByUser(
            @Parameter(description = "用户ID", example = "1001", required = true)
            @PathVariable("userId") Long userId) {
        List<Orders> orders = ordersService.getByUserId(userId);
        return Result.success(orders);
    }

    @Operation(summary = "根据状态获取订单列表")
    @GetMapping("/status/{orderStatus}")
    public Result<List<Orders>> getOrdersByStatus(
            @Parameter(description = "订单状态：0-待支付，1-待接单，2-制作中，3-待取餐，4-已完成，5-已取消", example = "1", required = true)
            @PathVariable("orderStatus") Integer orderStatus) {
        List<Orders> orders = ordersService.getByStatus(orderStatus);
        return Result.success(orders);
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Boolean> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        Orders order = new Orders();
        BeanUtils.copyProperties(request, order);
        // 生成订单号
        order.setOrderNo(OrderNoGenerator.generate());
        // 设置默认值
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(0); // 默认待支付
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus(0); // 默认未支付
        }
        if (order.getPriority() == null) {
            order.setPriority(0); // 默认普通优先级
        }
        if (order.getIsEvaluated() == null) {
            order.setIsEvaluated(0); // 默认未评价
        }
        boolean success = ordersService.save(order);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新订单信息")
    @PutMapping
    public Result<Boolean> updateOrder(@RequestBody Orders order) {
        boolean success = ordersService.updateById(order);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteOrder(
            @Parameter(description = "订单ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        boolean success = ordersService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @Parameter(description = "订单ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "订单状态：0-待支付，1-待接单，2-制作中，3-待取餐，4-已完成，5-已取消", example = "2", required = true)
            @RequestParam("orderStatus") Integer orderStatus) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setOrderStatus(orderStatus);
        boolean success = ordersService.updateById(order);
        return success ? Result.success(true) : Result.error("更新状态失败");
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(
            @Parameter(description = "订单ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "取消原因", example = "用户取消", required = true)
            @RequestParam("cancelReason") String cancelReason) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setOrderStatus(5); // 已取消
        order.setCancelReason(cancelReason);
        boolean success = ordersService.updateById(order);
        return success ? Result.success(true) : Result.error("取消失败");
    }
}
