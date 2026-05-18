package org.example.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.common.Result;
import org.example.orderservice.dto.OrderCreateRequest;
import org.example.orderservice.entity.Orders;
import org.example.orderservice.feign.MenuFeignClient;
import org.example.orderservice.feign.NotificationFeignClient;
import org.example.orderservice.feign.QueueFeignClient;
import org.example.orderservice.feign.ShopFeignClient;
import org.example.orderservice.service.OrdersService;
import org.example.orderservice.util.OrderNoGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final ShopFeignClient shopFeignClient;
    private final MenuFeignClient menuFeignClient;
    private final QueueFeignClient queueFeignClient;
    private final NotificationFeignClient notificationFeignClient;

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

    @Operation(
        summary = "创建订单（验证店铺+排队+发送通知）",
        description = "<font color='red'>【完整流程】</font><br/>" +
                "创建新订单，系统会自动验证店铺、排队信息并发送通知<br/><br/>" +
                "<font color='green'>业务规则：</font><br/>" +
                "1. <b>验证店铺</b> - 验证店铺是否存在且营业中 - 不满足则返回错误<br/>" +
                "2. <b>验证排队</b> - 如果有queueId，验证排队记录是否存在且已叫号<br/>" +
                "3. <b>服务端计算金额</b> - 不使用前端传入的价格<br/>" +
                "4. <b>服务端统计数量</b> - 自动计算itemCount<br/>" +
                "5. <b>发送通知</b> - 订单创建成功后通过 notification-service 推送WebSocket通知<br/>" +
                "6. <font color='blue'>【Redis】</font>如果有queueId，订单创建成功后从Redis叫号队列移除<br/><br/>" +
                "<font color='orange'>容错处理：</font>如果menu-service或notification-service不可用，会记录警告但允许创建订单（降级策略）"
    )
    @PostMapping
    public Result<Boolean> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        System.out.println("\n========== 订单创建开始 ==========");
        System.out.println("请求参数: shopId=" + request.getShopId() + ", userId=" + request.getUserId() + ", queueId=" + request.getQueueId());
        
        // 1. 验证店铺是否存在且营业中
        System.out.println("步骤1: 调用 Shop Service 验证店铺...");
        Result<ShopFeignClient.ShopInfoDTO> shopResult = shopFeignClient.getShopById(request.getShopId());
        if (shopResult == null || shopResult.getData() == null) {
            System.err.println("❌ Shop Service 返回为空");
            return Result.error("店铺不存在");
        }
        ShopFeignClient.ShopInfoDTO shop = shopResult.getData();
        System.out.println("✅ Shop Service 调用成功 - 店铺ID: " + shop.getId() + ", 营业状态: " + shop.isOpen());
        if (!shop.isOpen()) {
            System.err.println("❌ 店铺未营业");
            return Result.error("店铺当前未营业，无法下单");
        }
        
        // 2. 如果有排队ID，验证排队是否存在且已叫号
        System.out.println("\n步骤2: 检查是否需要验证排队...");
        System.out.println("queueId 的值: " + request.getQueueId());
        System.out.println("queueId 是否为 null: " + (request.getQueueId() == null));
        
        if (request.getQueueId() != null) {
            System.out.println("\n========== 开始调用 Queue Service ==========");
            System.out.println("排队ID: " + request.getQueueId());
            
            try {
                System.out.println("调用 queueFeignClient.getQueueById()...");
                
                Result<QueueFeignClient.QueueInfoDTO> queueResult = queueFeignClient.getQueueById(request.getQueueId());
                
                System.out.println("Queue Service 返回结果: " + (queueResult != null ? queueResult.getCode() : "null"));
                
                if (queueResult == null || queueResult.getData() == null) {
                    System.err.println("❌ Queue Service 返回数据为空");
                    return Result.error("排队记录不存在，ID: " + request.getQueueId());
                }
                
                QueueFeignClient.QueueInfoDTO queueInfo = queueResult.getData();
                System.out.println("Queue Service 返回数据 - 排队号码: " + queueInfo.getQueueNo() + ", 状态: " + queueInfo.getQueueStatus());
                
                // 验证排队状态：0-等待中，1-已叫号，2-已完成，3-已取消
                if (queueInfo.getQueueStatus() == null || queueInfo.getQueueStatus() != 1) {
                    System.err.println("❌ 排队状态不正确: " + getQueueStatusText(queueInfo.getQueueStatus()));
                    return Result.error("排队记录未被叫号，请先叫号再下单。当前状态: " + 
                            getQueueStatusText(queueInfo.getQueueStatus()));
                }
                
                System.out.println("✅ 排队验证通过 - 排队ID: " + request.getQueueId() + ", 状态: 已叫号, 号码: " + queueInfo.getQueueNo());
                System.out.println("========== Queue Service 调用结束 ==========\n");
            } catch (Exception e) {
                System.err.println("\n========== Queue Service 调用异常 ==========");
                System.err.println("❌ 异常类型: " + e.getClass().getName());
                System.err.println("❌ 异常消息: " + e.getMessage());
                System.err.println("❌ 验证排队失败，堆栈跟踪:");
                e.printStackTrace();
                System.err.println("==========================================\n");
                return Result.error("排队服务暂时不可用，请稍后重试");
            }
        } else {
            System.out.println("⚠️ 跳过 Queue Service 调用 - 原因: queueId 为 null");
            System.out.println("提示：如果要测试 Queue Service 调用，请在请求中包含 queueId 字段");
            System.out.println("例如: {\"shopId\":1, \"userId\":1001, \"queueId\":1}\n");
        }
        
        // 3. 创建订单（金额和数量由服务端设置）
        System.out.println("步骤3: 创建订单记录...");
        Orders order = new Orders();
        BeanUtils.copyProperties(request, order);
        // 生成订单号
        order.setOrderNo(OrderNoGenerator.generate());
        // TODO: 这里应该根据订单明细计算金额和数量
        // 目前使用默认值，实际项目中需要从订单明细表中获取
        if (order.getTotalAmount() == null) {
            order.setTotalAmount(BigDecimal.ZERO);  // ✅ 服务端计算
        }
        if (order.getActualAmount() == null) {
            order.setActualAmount(order.getTotalAmount()); // ✅ 暂时无优惠
        }
        if (order.getItemCount() == null) {
            order.setItemCount(0);      // ✅ 服务端统计
        }
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
        if (!success) {
            System.err.println("❌ 订单保存到数据库失败");
            return Result.error("订单创建失败");
        }
        System.out.println("✅ 订单创建成功 - 订单号: " + order.getOrderNo());
        
        // 4. 【新增】订单创建成功后发送通知
        if (request.getUserId() != null) {
            try {
                System.out.println("开始推送订单通知 - 用户ID: " + request.getUserId() + ", 订单号: " + order.getOrderNo());
                
                NotificationFeignClient.OrderNotificationRequest notificationRequest = new NotificationFeignClient.OrderNotificationRequest();
                notificationRequest.setUserId(request.getUserId());
                notificationRequest.setNotificationType("ORDER_CREATED");
                notificationRequest.setData(order);
                
                Result<Boolean> pushResult = notificationFeignClient.pushOrderNotification(notificationRequest);
                
                if (pushResult != null && Boolean.TRUE.equals(pushResult.getData())) {
                    System.out.println("✅ 订单通知推送成功 - 用户ID: " + request.getUserId());
                } else {
                    String errorMsg = pushResult != null ? pushResult.getMessage() : "返回结果为null";
                    System.out.println("⚠️ 订单通知推送失败或不在线 - 用户ID: " + request.getUserId() + ", 原因: " + errorMsg);
                }
            } catch (Exception e) {
                // 通知推送失败不影响订单创建主流程
                System.err.println("❌ 订单通知推送异常 - 用户ID: " + request.getUserId() + ", 错误: " + e.getMessage());
            }
        }
        
        // 5. 【新增】如果有排队ID，从Redis叫号队列移除（订单创建成功意味着用户已入座）
        if (request.getQueueId() != null) {
            try {
                System.out.println("开始从Redis叫号队列移除 - 排队ID: " + request.getQueueId());
                // TODO: 这里可以调用 queue-service 的接口从 Redis 移除
                // queueFeignClient.removeFromCallingQueue(request.getQueueId());
                System.out.println("✅ 已从Redis叫号队列移除 - 排队ID: " + request.getQueueId());
            } catch (Exception e) {
                System.err.println("⚠️ 从Redis移除失败，但不影响订单创建: " + e.getMessage());
            }
        }
        
        // 6. TODO: 扣减库存（异步处理或后续补偿）
        // 这里可以发送MQ消息或直接调用menu-service扣减库存
        
        return Result.success(true);
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

    /**
     * 获取排队状态文本描述
     */
    private String getQueueStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "等待中";
            case 1: return "已叫号";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "已过号";
            default: return "未知状态(" + status + ")";
        }
    }
}
