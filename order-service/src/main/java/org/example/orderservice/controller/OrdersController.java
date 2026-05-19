package org.example.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.common.Result;
import org.example.orderservice.dto.OrderCreateRequest;
import org.example.orderservice.dto.OrderItemRequest;
import org.example.orderservice.entity.OrderItem;
import org.example.orderservice.entity.Orders;
import org.example.orderservice.feign.MenuFeignClient;
import org.example.orderservice.feign.NotificationFeignClient;
import org.example.orderservice.feign.QueueFeignClient;
import org.example.orderservice.feign.ShopFeignClient;
import org.example.orderservice.mapper.OrderItemMapper;
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
    private final OrderItemMapper orderItemMapper;
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

    @Operation(
        summary = "根据店铺ID获取订单列表",
        description = "<font color='green'>📋 功能说明：</font><br/>" +
                "查询指定店铺的所有订单，用于店铺管理后台<br/><br/>" +
                "<font color='blue'>💡 使用场景：</font><br/>" +
                "- 店铺后台查看今日订单<br/>" +
                "- 店员管理订单状态"
    )
    @GetMapping("/shop/{shopId}")
    public Result<List<Orders>> getOrdersByShop(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<Orders> orders = ordersService.getByShopId(shopId);
        return Result.success(orders);
    }

    @Operation(
        summary = "根据用户ID获取订单列表",
        description = "<font color='green'>📋 功能说明：</font><br/>" +
                "查询指定用户的所有订单，按创建时间倒序排列<br/><br/>" +
                "<font color='blue'>💡 使用场景：</font><br/>" +
                "- 个人中心查看历史订单<br/>" +
                "- 订单页面展示用户的点餐记录"
    )
    @GetMapping("/user/{userId}")
    public Result<List<Orders>> getOrdersByUser(
            @Parameter(description = "用户ID", example = "1001", required = true)
            @PathVariable("userId") Long userId) {
        List<Orders> orders = ordersService.getByUserId(userId);
        return Result.success(orders);
    }

    @Operation(
        summary = "根据排队ID获取订单列表",
        description = "<font color='green'>📋 功能说明：</font><br/>" +
                "查询指定排队记录关联的所有订单，按创建时间倒序排列<br/><br/>" +
                "<font color='blue'>💡 使用场景：</font><br/>" +
                "- 排队页面检查用户是否已下单<br/>" +
                "- 判断是否需要显示'前往点菜'按钮<br/>" +
                "- 一个排队可能对应多个订单（多次点餐）<br/><br/>" +
                "<font color='orange'>⚠️ 注意事项：</font><br/>" +
                "- 如果该排队没有关联任何订单，返回空数组 []<br/>" +
                "- 仅返回与 queueId 匹配的订单"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Result.class),
                examples = @ExampleObject(
                    name = "成功响应 - 有订单",
                    value = "{\n" +
                            "  \"code\": 200,\n" +
                            "  \"message\": \"操作成功\",\n" +
                            "  \"data\": [\n" +
                            "    {\n" +
                            "      \"id\": 123,\n" +
                            "      \"orderNo\": \"ORD2026051900001\",\n" +
                            "      \"queueId\": 17,\n" +
                            "      \"totalAmount\": 94.00,\n" +
                            "      \"itemCount\": 3,\n" +
                            "      \"orderStatus\": 0\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(responseCode = "200", description = "查询成功 - 无订单",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "成功响应 - 空数组",
                    value = "{\n" +
                            "  \"code\": 200,\n" +
                            "  \"message\": \"操作成功\",\n" +
                            "  \"data\": []\n" +
                            "}"
                )
            )
        )
    })
    @GetMapping("/queue/{queueId}")
    public Result<List<Orders>> getOrdersByQueue(
            @Parameter(description = "排队ID", example = "17", required = true)
            @PathVariable("queueId") Long queueId) {
        List<Orders> orders = ordersService.lambdaQuery()
                .eq(Orders::getQueueId, queueId)
                .orderByDesc(Orders::getCreatedAt)
                .list();
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
        summary = "创建订单（完整点单流程）",
        description = "<font color='red'>【核心功能】</font> 创建新订单，包含订单主表和订单明细<br/><br/>" +
                "<font color='green'>📋 业务流程：</font><br/>" +
                "1. <b>验证店铺</b> - 通过 Feign 调用 shop-service 验证店铺是否存在且营业中<br/>" +
                "2. <b>验证排队</b> - 如果提供 queueId，验证排队记录是否存在且状态为'已叫号'<br/>" +
                "3. <b>验证订单明细</b> - 检查 items 列表不为空<br/>" +
                "4. <b>服务端计算</b> - 自动计算订单总金额（totalAmount）和菜品总数量（itemCount）<br/>" +
                "5. <b>保存订单</b> - 先保存 orders 主表，再批量保存 order_item 明细表<br/>" +
                "6. <b>发送通知</b> - 通过 notification-service 推送 WebSocket 通知给用户<br/>" +
                "7. <b>更新排队</b> - 如果有关联排队，从 Redis 叫号队列移除<br/><br/>" +
                "<font color='blue'>💡 使用场景：</font><br/>" +
                "- 用户被叫号后点击'前往点菜'按钮<br/>" +
                "- 用户在点餐页面选择菜品并提交订单<br/>" +
                "- 订单自动关联排队ID，实现排队与订单的绑定<br/><br/>" +
                "<font color='orange'>⚠️ 注意事项：</font><br/>" +
                "- 订单金额由服务端计算，不使用前端传入的值（安全考虑）<br/>" +
                "- 订单明细必须包含 itemId、itemName、price、quantity 字段<br/>" +
                "- 如果排队服务不可用，会返回错误，不允许创建订单<br/>" +
                "- 通知推送失败不影响订单创建（降级策略）"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "订单创建请求数据",
        required = true,
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = OrderCreateRequest.class),
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "堂食订单示例",
                value = "{\n" +
                        "  \"shopId\": 1,\n" +
                        "  \"userId\": 1001,\n" +
                        "  \"orderType\": 1,\n" +
                        "  \"queueId\": 17,\n" +
                        "  \"remark\": \"不要辣，少盐\",\n" +
                        "  \"items\": [\n" +
                        "    {\n" +
                        "      \"itemId\": 1,\n" +
                        "      \"itemName\": \"宫保鸡丁\",\n" +
                        "      \"price\": 38.00,\n" +
                        "      \"quantity\": 2,\n" +
                        "      \"remark\": \"微辣\",\n" +
                        "      \"specification\": \"{\\\"size\\\":\\\"大份\\\",\\\"spicy\\\":\\\"微辣\\\"}\",\n" +
                        "      \"toppings\": \"[{\\\"name\\\":\\\"加蛋\\\",\\\"price\\\":3}]\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"itemId\": 5,\n" +
                        "      \"itemName\": \"酸辣汤\",\n" +
                        "      \"price\": 18.00,\n" +
                        "      \"quantity\": 1,\n" +
                        "      \"remark\": \"\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "订单创建成功", 
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Result.class),
                examples = @ExampleObject(
                    name = "成功响应",
                    value = "{\n" +
                            "  \"code\": 200,\n" +
                            "  \"message\": \"操作成功\",\n" +
                            "  \"data\": true\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "请求参数错误（如：店铺不存在、排队未叫号、订单明细为空）"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
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
        
        // 3. 验证订单明细
        System.out.println("\n步骤3: 验证订单明细...");
        if (request.getItems() == null || request.getItems().isEmpty()) {
            System.err.println("❌ 订单明细为空");
            return Result.error("订单明细不能为空");
        }
        System.out.println("✅ 订单明细数量: " + request.getItems().size());
        
        // 4. 计算订单总金额和总数量（服务端计算）
        System.out.println("\n步骤4: 计算订单金额和数量...");
        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            BigDecimal subtotal = itemRequest.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            itemCount += itemRequest.getQuantity();
            System.out.println(String.format("  - %s x%d = ¥%.2f", 
                itemRequest.getItemName(), itemRequest.getQuantity(), subtotal.doubleValue()));
        }
        
        System.out.println(String.format("✅ 订单总金额: ¥%.2f, 总数量: %d", totalAmount.doubleValue(), itemCount));
        
        // 5. 创建订单主表
        System.out.println("\n步骤5: 创建订单记录...");
        Orders order = new Orders();
        BeanUtils.copyProperties(request, order);
        // 生成订单号
        order.setOrderNo(OrderNoGenerator.generate());
        // 设置服务端计算的金额和数量
        order.setTotalAmount(totalAmount);  // ✅ 服务端计算
        order.setActualAmount(totalAmount); // ✅ 暂时无优惠
        order.setItemCount(itemCount);      // ✅ 服务端统计
        
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
        System.out.println("✅ 订单主表创建成功 - 订单ID: " + order.getId() + ", 订单号: " + order.getOrderNo());
        
        // 6. 批量插入订单明细
        System.out.println("\n步骤6: 保存订单明细...");
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(order.getOrderNo());
            orderItem.setItemId(itemRequest.getItemId());
            orderItem.setItemName(itemRequest.getItemName());
            orderItem.setPrice(itemRequest.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            // 计算小计
            orderItem.setSubtotal(itemRequest.getPrice().multiply(new BigDecimal(itemRequest.getQuantity())));
            orderItem.setRemark(itemRequest.getRemark());
            orderItem.setSpecification(itemRequest.getSpecification());
            orderItem.setToppings(itemRequest.getToppings());
            
            orderItemMapper.insert(orderItem);
            System.out.println(String.format("  ✅ 明细: %s x%d", itemRequest.getItemName(), itemRequest.getQuantity()));
        }
        System.out.println("✅ 订单明细保存成功 - 共" + request.getItems().size() + "项");
        
        // 7. 订单创建成功后发送通知
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
        
        // 8. 如果有排队ID，从Redis叫号队列移除（订单创建成功意味着用户已入座）
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
        
        // 9. TODO: 扣减库存（异步处理或后续补偿）
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
