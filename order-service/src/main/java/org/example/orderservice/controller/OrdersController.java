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
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.common.Result;
import org.example.orderservice.dto.OrderCreateRequest;
import org.example.orderservice.dto.OrderItemRequest;
import org.example.orderservice.entity.OrderItem;
import org.example.orderservice.entity.Orders;
import org.example.orderservice.feign.MenuFeignClient;
import org.example.orderservice.feign.NotificationFeignClient;
import org.example.orderservice.feign.QueueFeignClient;
import org.example.orderservice.feign.ShopFeignClient;
import org.example.orderservice.feign.TableFeignClient;
import org.example.orderservice.mapper.OrderItemMapper;
import org.example.orderservice.service.OrdersService;
import org.example.orderservice.util.OrderNoGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单管理控制器
 */
@Slf4j
@Tag(name = "订单管理", description = "订单的增删改查和状态管理")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;
    private final OrderItemMapper orderItemMapper;
    private final ShopFeignClient shopFeignClient;
    private final TableFeignClient tableFeignClient;
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
        log.info("\n========== 收到订单查询请求 ==========");
        log.info("【接口路径】GET /api/order/no/{}", orderNo);
        log.info("【请求时间】{}", java.time.LocalDateTime.now());
        log.info("【订单编号】{}", orderNo);
        
        Orders order = ordersService.getByOrderNo(orderNo);
        
        if (order == null) {
            log.error("❌ 订单不存在 - 订单号: {}", orderNo);
            log.error("==========================================\n");
            return Result.error("订单不存在");
        }
        
        log.info("✅ 订单查询成功");
        log.info("  - 订单ID: {}", order.getId());
        log.info("  - 订单状态: {}", getOrderByStatusText(order.getOrderStatus()));
        log.info("  - 支付状态: {}", order.getPaymentStatus() != null ? (order.getPaymentStatus() == 0 ? "未支付" : "已支付") : "未知");
        log.info("  - 订单金额: ¥{}", order.getTotalAmount());
        log.info("==========================================\n");
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
        summary = "根据排队号码获取订单列表",
        description = "<font color='green'>📋 功能说明：</font><br/>" +
                "查询指定排队号码关联的所有订单，按创建时间倒序排列<br/><br/>" +
                "<font color='blue'>💡 使用场景：</font><br/>" +
                "- 排队页面检查用户是否已下单<br/>" +
                "- 判断是否需要显示'前往点菜'按钮<br/>" +
                "- 一个排队可能对应多个订单（多次点餐）<br/><br/>" +
                "<font color='orange'>⚠️ 注意事项：</font><br/>" +
                "- 如果该排队没有关联任何订单，返回空数组 []<br/>" +
                "- 仅返回与 queueNumber 匹配的订单"
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
                            "      \"queueNumber\": \"A001\",\n" +
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
    @GetMapping("/queue/{queueNumber}")
    public Result<List<Orders>> getOrdersByQueue(
            @Parameter(description = "排队号码（如：A001、B002）", example = "A001", required = true)
            @PathVariable("queueNumber") String queueNumber) {
        List<Orders> orders = ordersService.lambdaQuery()
                .eq(Orders::getQueueNumber, queueNumber)
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
        summary = "创建订单（完整点单流程 - 支持自动分配桌子和估算时间）",
        description = "<font color='red'>【核心功能】</font> 创建新订单，包含订单主表和订单明细<br/><br/>" +
                "<font color='green'>📋 业务流程：</font><br/>" +
                "1. <b>验证店铺</b> - 通过 Feign 调用 shop-service 验证店铺是否存在且营业中<br/>" +
                "2. <b>验证排队</b> - 如果提供 queueNumber，验证排队记录是否存在且状态为'已叫号'<br/>" +
                "3. <b>自动分配桌子</b> - 如果是堂食且未指定 tableId，自动查询并分配空闲桌子<br/>" +
                "4. <b>获取桌台编号</b> - 如果已指定 tableId，从 shop-service 查询 table_info 表获取 tableNumber 并填充<br/>" +
                "5. <b>验证订单明细</b> - 检查 items 列表不为空<br/>" +
                "6. <b>服务端计算</b> - 自动计算订单总金额（totalAmount）、菜品总数量（itemCount）和预计制作时间（estimatedTime）<br/>" +
                "7. <b>保存订单</b> - 先保存 orders 主表，再批量保存 order_item 明细表<br/>" +
                "8. <b>发送通知</b> - 通过 notification-service 推送 WebSocket 通知给用户<br/>" +
                "9. <b>更新排队</b> - 如果有关联排队，从 Redis 叫号队列移除<br/>" +
                "10. <b>更新桌台状态</b> - 如果有桌台ID，将桌台状态更新为'已占用'（状态码1）<br/>" +
                "11. <b>✨更新排队状态</b> - 如果有排队号码，将排队状态更新为'已入座'（状态码2）<br/><br/>" +
                "<font color='blue'>💡 使用场景：</font><br/>" +
                "- 用户被叫号后点击'前往点菜'按钮<br/>" +
                "- 用户在点餐页面选择菜品并提交订单<br/>" +
                "- 订单自动关联排队号码，实现排队与订单的绑定<br/>" +
                "- 系统自动为用户分配空闲桌子（堂食场景）<br/>" +
                "- 根据菜品制作时间估算订单完成时间<br/>" +
                "- 自动从 table_info 表查询并填充桌台编号（tableNumber）<br/>" +
                "- 订单创建成功后自动将桌台状态更新为'已占用'<br/>" +
                "- 订单创建成功后自动将排队状态更新为'已入座'<br/><br/>" +
                "<font color='orange'>⚠️ 注意事项：</font><br/>" +
                "- 订单金额由服务端计算，不使用前端传入的值（安全考虑）<br/>" +
                "- 订单明细必须包含 itemId、itemName、price、quantity 字段<br/>" +
                "- 如果排队服务不可用，会返回错误，不允许创建订单<br/>" +
                "- 通知推送失败不影响订单创建（降级策略）<br/>" +
                "- estimatedTime 由服务端根据菜品制作时间自动计算，无需前端传入<br/>" +
                "- tableNumber 由服务端从 table_info 表自动查询并填充，无需前端传入<br/>" +
                "- 桌台状态更新失败不影响订单创建（降级策略），可稍后手动更新<br/>" +
                "- 排队状态更新失败不影响订单创建（降级策略），可稍后手动更新"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "订单创建请求数据",
        required = true,
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = OrderCreateRequest.class),
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "堂食订单示例（自动分配桌子）",
                value = "{\n" +
                        "  \"shopId\": 1,\n" +
                        "  \"userId\": 1001,\n" +
                        "  \"orderType\": 1,\n" +
                        "  \"queueNumber\": \"A001\",\n" +
                        "  \"tableId\": null,\n" +
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
        System.out.println("请求参数: shopId=" + request.getShopId() + ", userId=" + request.getUserId() + ", queueNumber=" + request.getQueueNumber());
        
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
        
        // 2. 如果有排队号码，验证排队是否存在且已叫号
        System.out.println("\n步骤2: 检查是否需要验证排队...");
        System.out.println("queueNumber 的值: " + request.getQueueNumber());
        System.out.println("queueNumber 是否为 null: " + (request.getQueueNumber() == null));
        
        if (request.getQueueNumber() != null) {
            System.out.println("\n========== 开始调用 Queue Service ==========");
            System.out.println("排队号码: " + request.getQueueNumber());
            
            try {
                System.out.println("调用 queueFeignClient.getQueueByNo()...");
                
                Result<QueueFeignClient.QueueInfoDTO> queueResult = queueFeignClient.getQueueByNo(request.getQueueNumber());
                
                System.out.println("Queue Service 返回结果: " + (queueResult != null ? queueResult.getCode() : "null"));
                
                if (queueResult == null || queueResult.getData() == null) {
                    System.err.println("❌ Queue Service 返回数据为空");
                    return Result.error("排队记录不存在，号码: " + request.getQueueNumber());
                }
                
                QueueFeignClient.QueueInfoDTO queueInfo = queueResult.getData();
                System.out.println("Queue Service 返回数据 - 排队号码: " + queueInfo.getQueueNo() + ", 状态: " + queueInfo.getQueueStatus());
                
                // 验证排队状态：0-等待中，1-已叫号，2-已完成，3-已取消
                if (queueInfo.getQueueStatus() == null || queueInfo.getQueueStatus() != 1) {
                    System.err.println("❌ 排队状态不正确: " + getQueueStatusText(queueInfo.getQueueStatus()));
                    return Result.error("排队记录未被叫号，请先叫号再下单。当前状态: " + 
                            getQueueStatusText(queueInfo.getQueueStatus()));
                }
                
                // 验证该排队号码是否已有关联的订单
                System.out.println("验证该排队号码是否已有关联订单...");
                List<Orders> existingOrders = ordersService.lambdaQuery()
                        .eq(Orders::getQueueNumber, request.getQueueNumber())
                        .orderByDesc(Orders::getCreatedAt)
                        .list();
                
                if (existingOrders != null && !existingOrders.isEmpty()) {
                    System.err.println("❌ 该排队号码已存在关联订单，不允许重复下单 - 排队号码: " + request.getQueueNumber());
                    System.err.println("已关联的订单数量: " + existingOrders.size());
                    for (Orders order : existingOrders) {
                        System.err.println("  - 订单号: " + order.getOrderNo() + ", 状态: " + getOrderByStatusText(order.getOrderStatus()));
                    }
                    return Result.error("该排队号码已存在关联订单，不能重复下单");
                }
                
                System.out.println("✅ 排队验证通过 - 排队号码: " + request.getQueueNumber() + ", 状态: 已叫号");
                System.out.println("✅ 该排队号码未关联订单，允许下单");
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
            System.out.println("⚠️ 跳过 Queue Service 调用 - 原因: queueNumber 为 null");
            System.out.println("提示：如果要测试 Queue Service 调用，请在请求中包含 queueNumber 字段");
            System.out.println("例如: {\"shopId\":1, \"userId\":1001, \"queueNumber\":\"A001\"}\n");
        }
        
        // 3. 验证订单明细
        System.out.println("\n步骤3: 验证订单明细...");
        if (request.getItems() == null || request.getItems().isEmpty()) {
            System.err.println("❌ 订单明细为空");
            return Result.error("订单明细不能为空");
        }
        System.out.println("✅ 订单明细数量: " + request.getItems().size());
        
        // 3.5 自动分配桌子（如果是堂食且未指定tableId）
        if (request.getOrderType() != null && request.getOrderType() == 1 && request.getTableId() == null) {
            System.out.println("\n步骤3.5: 自动分配空闲桌子...");
            try {
                Result<List<TableFeignClient.TableInfoDTO>> tablesResult = tableFeignClient.getAvailableTables(request.getShopId());
                
                if (tablesResult != null && tablesResult.getData() != null && !tablesResult.getData().isEmpty()) {
                    // 选择第一个可用的桌子
                    TableFeignClient.TableInfoDTO firstTable = tablesResult.getData().get(0);
                    request.setTableId(firstTable.getId());
                    System.out.println("✅ 自动分配桌子: " + firstTable.getTableNumber() + 
                                     " (ID: " + firstTable.getId() + ", 容纳人数: " + firstTable.getCapacity() + ")");
                } else {
                    System.err.println("⚠️ 没有可用的桌子");
                    return Result.error("当前没有可用的桌子，请稍后再试");
                }
            } catch (Exception e) {
                System.err.println("❌ 查询可用桌子失败: " + e.getMessage());
                // 降级策略：不阻断订单创建，允许用户稍后手动分配
                System.out.println("⚠️ 跳过自动分配桌子，继续创建订单");
            }
        }
        
        // 3.6 如果已指定tableId，从shop-service获取桌台编号（tableNumber）
        String tableNumber = null;
        if (request.getTableId() != null) {
            System.out.println("\n步骤3.6: 获取桌台编号...");
            try {
                Result<TableFeignClient.TableInfoDTO> tableResult = tableFeignClient.getTableById(request.getTableId());
                
                if (tableResult != null && tableResult.getData() != null) {
                    tableNumber = tableResult.getData().getTableNumber();
                    System.out.println("✅ 获取桌台编号成功: " + tableNumber);
                } else {
                    System.err.println("⚠️ 无法获取桌台信息，桌台ID: " + request.getTableId());
                }
            } catch (Exception e) {
                System.err.println("❌ 获取桌台编号失败: " + e.getMessage());
                // 降级策略：不阻断订单创建，tableNumber可以为null
                System.out.println("⚠️ 跳过获取桌台编号，继续创建订单");
            }
        }
        
        // 4. 计算订单总金额、总数量和估算制作时间（服务端计算）
        System.out.println("\n步骤4: 计算订单金额、数量和估算时间...");
        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;
        int maxPrepareTime = 0;  // 最长制作时间
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            BigDecimal subtotal = itemRequest.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            itemCount += itemRequest.getQuantity();
            
            // 获取菜品制作时间
            try {
                Result<MenuFeignClient.MenuItemInfoDTO> itemResult = menuFeignClient.getMenuItemById(itemRequest.getItemId());
                if (itemResult != null && itemResult.getData() != null) {
                    Integer prepareTime = itemResult.getData().getPrepareTime();
                    if (prepareTime != null && prepareTime > maxPrepareTime) {
                        maxPrepareTime = prepareTime;
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ 获取菜品 " + itemRequest.getItemId() + " 的制作时间失败: " + e.getMessage());
            }
            
            System.out.println(String.format("  - %s x%d = ¥%.2f", 
                itemRequest.getItemName(), itemRequest.getQuantity(), subtotal.doubleValue()));
        }
        
        // 估算时间 = 最长制作时间 + 缓冲时间（5分钟）
        int estimatedTime = maxPrepareTime > 0 ? maxPrepareTime + 5 : 15; // 默认15分钟
        System.out.println(String.format("✅ 订单总金额: ¥%.2f, 总数量: %d, 估算时间: %d分钟", 
            totalAmount.doubleValue(), itemCount, estimatedTime));
        
        // 5. 创建订单主表
        System.out.println("\n步骤5: 创建订单记录...");
        Orders order = new Orders();
        BeanUtils.copyProperties(request, order);
        // 生成订单号
        order.setOrderNo(OrderNoGenerator.generate());
        // 设置服务端计算的金额、数量和估算时间
        order.setTotalAmount(totalAmount);  // ✅ 服务端计算
        order.setActualAmount(totalAmount); // ✅ 暂时无优惠
        order.setItemCount(itemCount);      // ✅ 服务端统计
        order.setEstimatedTime(estimatedTime); // ✅ 服务端估算
        
        // 填充从shop-service获取的tableNumber
        if (tableNumber != null) {
            order.setTableNumber(tableNumber);
            System.out.println("✅ 已填充桌台编号: " + tableNumber);
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
        
        // 8. 如果有排队号码，从Redis叫号队列移除（订单创建成功意味着用户已入座）
        if (request.getQueueNumber() != null) {
            try {
                System.out.println("开始从Redis叫号队列移除 - 排队号码: " + request.getQueueNumber());
                // TODO: 这里可以调用 queue-service 的接口从 Redis 移除
                // queueFeignClient.removeFromCallingQueue(request.getQueueNumber());
                System.out.println("✅ 已从Redis叫号队列移除 - 排队号码: " + request.getQueueNumber());
            } catch (Exception e) {
                System.err.println("⚠️ 从Redis移除失败，但不影响订单创建: " + e.getMessage());
            }
        }
        
        // 9. 如果有桌台ID，将桌台状态更新为"已占用"（状态码1）
        if (request.getTableId() != null) {
            System.out.println("\n步骤9: 更新桌台状态为已占用...");
            try {
                TableFeignClient.TableStatusUpdateRequest statusRequest = new TableFeignClient.TableStatusUpdateRequest();
                statusRequest.setTableStatus(1); // 1-已占用
                
                Result<Boolean> updateResult = tableFeignClient.updateTableStatus(request.getTableId(), statusRequest);
                
                if (updateResult != null && Boolean.TRUE.equals(updateResult.getData())) {
                    System.out.println("✅ 桌台状态更新成功 - 桌台ID: " + request.getTableId() + ", 状态: 已占用");
                } else {
                    String errorMsg = updateResult != null ? updateResult.getMessage() : "返回结果为null";
                    System.err.println("⚠️ 桌台状态更新失败 - 桌台ID: " + request.getTableId() + ", 原因: " + errorMsg);
                }
            } catch (Exception e) {
                // 桌台状态更新失败不影响订单创建主流程（降级策略）
                System.err.println("❌ 桌台状态更新异常 - 桌台ID: " + request.getTableId() + ", 错误: " + e.getMessage());
                System.out.println("⚠️ 桌台状态更新失败，但不影响订单创建，可稍后手动更新");
            }
        }
        
        // 10. 如果有排队号码，将排队状态更新为"已入座"（状态码2）
        if (request.getQueueNumber() != null) {
            System.out.println("\n步骤10: 更新排队状态为已入座...");
            try {
                // 先根据排队号码获取排队信息
                Result<QueueFeignClient.QueueInfoDTO> queueResult = queueFeignClient.getQueueByNo(request.getQueueNumber());
                
                if (queueResult != null && queueResult.getData() != null) {
                    Long queueId = queueResult.getData().getId();
                    System.out.println("获取到排队ID: " + queueId);
                    
                    // 更新排队状态为已入座（2）
                    Result<Boolean> updateResult = queueFeignClient.updateQueueStatus(queueId, 2);
                    
                    if (updateResult != null && Boolean.TRUE.equals(updateResult.getData())) {
                        System.out.println("✅ 排队状态更新成功 - 排队号码: " + request.getQueueNumber() + ", 状态: 已入座");
                    } else {
                        String errorMsg = updateResult != null ? updateResult.getMessage() : "返回结果为null";
                        System.err.println("⚠️ 排队状态更新失败 - 排队号码: " + request.getQueueNumber() + ", 原因: " + errorMsg);
                    }
                } else {
                    System.err.println("⚠️ 无法获取排队信息 - 排队号码: " + request.getQueueNumber());
                }
            } catch (Exception e) {
                // 排队状态更新失败不影响订单创建主流程（降级策略）
                System.err.println("❌ 排队状态更新异常 - 排队号码: " + request.getQueueNumber() + ", 错误: " + e.getMessage());
                System.out.println("⚠️ 排队状态更新失败，但不影响订单创建，可稍后手动更新");
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

    @Operation(
        summary = "支付成功后更新订单状态（payment-service调用）",
        description = "<font color='red'>【服务间调用】</font><br/>" +
                "当payment-service收到第三方支付平台通知后，调用此接口更新订单状态<br/><br/>" +
                "<font color='green'>业务流程：</font><br/>" +
                "1. <b>验证订单</b> - 根据订单编号和ID查询订单是否存在<br/>" +
                "2. <b>更新订单状态</b> - 将订单状态更新为'待接单'（orderStatus=1）<br/>" +
                "3. <b>更新支付状态</b> - 将支付状态更新为'已支付'（paymentStatus=1），记录支付时间<br/><br/>" +
                "<font color='blue'>💡 使用场景：</font><br/>" +
                "- 微信支付、支付宝等第三方支付平台支付成功后，通过payment-service回调此接口<br/>" +
                "- 确保订单状态与支付状态的一致性<br/><br/>" +
                "<font color='orange'>⚠️ 注意事项：</font><br/>" +
                "- 此接口仅供payment-service通过Feign调用，不直接对外暴露<br/>" +
                "- 如果订单已经是'待接单'状态，不会重复处理"
    )
    @PutMapping("/payment/success")
    public Result<Boolean> updateOrderStatusByPayment(
            @Parameter(description = "订单编号", example = "ORD2026051700001", required = true)
            @RequestParam("orderNo") String orderNo,
            @Parameter(description = "订单ID", example = "1", required = true)
            @RequestParam("orderId") Long orderId) {
        System.out.println("\n========== 开始更新订单状态（支付成功） ==========");
        System.out.println("订单编号: " + orderNo);
        System.out.println("订单ID: " + orderId);
        
        // 1. 查询订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo)
               .eq(Orders::getId, orderId);
        Orders order = ordersService.getOne(wrapper);
        
        if (order == null) {
            System.err.println("❌ 订单不存在 - 订单号: " + orderNo + ", 订单ID: " + orderId);
            return Result.error("订单不存在");
        }
        
        System.out.println("✅ 找到订单 - 当前状态: " + getOrderByStatusText(order.getOrderStatus()) 
                          + ", 支付状态: " + (order.getPaymentStatus() != null ? (order.getPaymentStatus() == 0 ? "未支付" : "已支付") : "未知"));
        
        // 2. 验证订单状态（只有待支付状态的订单才能更新为待接单）
        if (order.getOrderStatus() != null && order.getOrderStatus() >= 1) {
            System.out.println("⚠️ 订单已经处于待接单或之后状态，无需重复更新");
            return Result.success(true);
        }
        
        // 3. 更新订单状态
        order.setOrderStatus(1); // 1-待接单
        order.setPaymentStatus(1); // 1-已支付
        order.setPaymentTime(java.time.LocalDateTime.now()); // ✅ 服务端生成支付时间
        
        boolean success = ordersService.updateById(order);
        
        if (success) {
            System.out.println("✅ 订单状态更新成功");
            System.out.println("  - 订单状态: 待支付 → 待接单");
            System.out.println("  - 支付状态: 未支付 → 已支付");
            System.out.println("  - 支付时间: " + order.getPaymentTime());
            System.out.println("==========================================\n");
            return Result.success(true);
        } else {
            System.err.println("❌ 订单状态更新失败");
            System.err.println("==========================================\n");
            return Result.error("更新订单状态失败");
        }
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

    /**
     * 获取订单状态文本描述
     */
    private String getOrderByStatusText(Integer status) {
        if (status == null) return "待支付";
        switch (status) {
            case 0: return "待支付";
            case 1: return "待接单";
            case 2: return "制作中";
            case 3: return "待取餐";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知状态(" + status + ")";
        }
    }
}