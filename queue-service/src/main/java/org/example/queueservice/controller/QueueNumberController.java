package org.example.queueservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.queueservice.common.Result;
import org.example.queueservice.dto.QueueTakeNumberRequest;
import org.example.queueservice.dto.QueueUpdateRequest;
import org.example.queueservice.entity.QueueNumber;
import org.example.queueservice.feign.NotificationFeignClient;
import org.example.queueservice.feign.ShopFeignClient;
import org.example.queueservice.service.QueueNumberService;
import org.example.queueservice.service.RedisQueueService;
import org.example.queueservice.util.QueueNoGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Tag(name = "排队管理", description = "排队取号、叫号管理")
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueNumberController {

    private final QueueNumberService queueNumberService;
    private final ShopFeignClient shopFeignClient;
    private final NotificationFeignClient notificationFeignClient;
    private final RedisQueueService redisQueueService;

    @Operation(summary = "获取所有排队记录列表")
    @GetMapping("/list")
    public Result<List<QueueNumber>> listQueues() {
        List<QueueNumber> queues = queueNumberService.list();
        return Result.success(queues);
    }

    @Operation(summary = "根据ID获取排队记录详情")
    @GetMapping("/{id}")
    public Result<QueueNumber> getQueueById(
            @Parameter(description = "排队ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        QueueNumber queue = queueNumberService.getById(id);
        if (queue == null) {
            return Result.error("排队记录不存在");
        }
        return Result.success(queue);
    }

    @Operation(summary = "根据排队号码获取记录")
    @GetMapping("/no/{queueNo}")
    public Result<QueueNumber> getQueueByNo(
            @Parameter(description = "排队号码", example = "A001", required = true)
            @PathVariable("queueNo") String queueNo) {
        QueueNumber queue = queueNumberService.getByQueueNo(queueNo);
        if (queue == null) {
            return Result.error("排队记录不存在");
        }
        return Result.success(queue);
    }

    @Operation(summary = "根据店铺ID获取排队列表")
    @GetMapping("/shop/{shopId}")
    public Result<List<QueueNumber>> getQueuesByShop(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<QueueNumber> queues = queueNumberService.getByShopId(shopId);
        return Result.success(queues);
    }

    @Operation(summary = "获取等待中的排队列表")
    @GetMapping("/waiting/{shopId}")
    public Result<List<QueueNumber>> getWaitingQueues(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<QueueNumber> queues = queueNumberService.getWaitingQueue(shopId);
        return Result.success(queues);
    }

    @Operation(summary = "根据状态获取排队列表")
    @GetMapping("/status/{queueStatus}")
    public Result<List<QueueNumber>> getQueuesByStatus(
            @Parameter(description = "排队状态：0-等待中，1-已叫号，2-已入座，3-已取消，4-已过号", example = "0", required = true)
            @PathVariable("queueStatus") Integer queueStatus) {
        List<QueueNumber> queues = queueNumberService.getByStatus(queueStatus);
        return Result.success(queues);
    }

    @Operation(
        summary = "根据用户ID获取排队列表",
        description = "<font color='green'>💡 使用场景：</font><br/>" +
                "- 前端'我的排队'页面展示当前用户的排队记录<br/>" +
                "- 用户可以查看自己的所有排队历史<br/>" +
                "- 支持按店铺筛选（可选）"
    )
    @GetMapping("/user/{userId}")
    public Result<List<QueueNumber>> getQueuesByUser(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable("userId") Long userId,
            @Parameter(description = "店铺ID（可选）", example = "1")
            @RequestParam(value = "shopId", required = false) Long shopId) {
        List<QueueNumber> queues;
        if (shopId != null) {
            // 按用户和店铺查询
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QueueNumber> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(QueueNumber::getUserId, userId)
                   .eq(QueueNumber::getShopId, shopId)
                   .orderByDesc(QueueNumber::getCreatedAt);
            queues = queueNumberService.list(wrapper);
        } else {
            // 只按用户查询
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QueueNumber> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(QueueNumber::getUserId, userId)
                   .orderByDesc(QueueNumber::getCreatedAt);
            queues = queueNumberService.list(wrapper);
        }
        return Result.success(queues);
    }

    @Operation(
        summary = "排队取号（验证店铺 + Redis队列 + WebSocket推送）",
        description = "<font color='red'>【核心功能 - 已集成Redis】</font><br/>" +
                "用户在线取号，系统会自动验证店铺信息、加入Redis排序集合并推送WebSocket通知<br/><br/>" +
                "<font color='green'>业务规则：</font><br/>" +
                "1. <b>验证店铺</b> - 调用 shop-service 验证店铺是否存在且营业中 - 不满足则返回错误<br/>" +
                "2. <b>生成号码</b> - 前缀(堂食A/外带B) + 序列号<br/>" +
                "3. <b>设置默认值</b> - queueStatus=0(等待中), isNotified=0, notifyCount=0<br/>" +
                "4. <b>唯一性检查</b> - 同一用户在同一个店铺只能有一个等待中的排队记录<br/>" +
                "5. <font color='blue'>【Redis】</font>取号成功后自动添加到 Redis Sorted Set 等待队列（按时间戳排序）<br/>" +
                "6. <font color='blue'>【WebSocket】</font>通过 notification-service 推送取号成功通知给用户<br/><br/>" +
                "<font color='orange'>测试提示：</font>如果没有前端连接WebSocket，会显示'用户不在线'，这是正常的，不影响Redis队列操作"
    )
    @PostMapping
    public Result<Boolean> takeNumber(@RequestBody @Valid QueueTakeNumberRequest request) {
        // 1. 验证店铺是否存在且营业中
        Result<ShopFeignClient.ShopInfoDTO> shopResult = null;
        try {
            shopResult = shopFeignClient.getShopById(request.getShopId());
        } catch (Exception e) {
            return Result.error("店铺服务暂时不可用，请稍后重试");
        }
        
        if (shopResult == null || shopResult.getData() == null) {
            return Result.error("店铺不存在，ID: " + request.getShopId());
        }
        
        ShopFeignClient.ShopInfoDTO shopInfo = shopResult.getData();
        if (!shopInfo.isOpen()) {
            return Result.error("店铺当前未营业，无法取号");
        }
        
        // 2. 创建排队记录
        QueueNumber queue = new QueueNumber();
        BeanUtils.copyProperties(request, queue);
        
        // 【重要】服务端生成排队号码：前缀 + 日期 + 序列号
        String prefix = QueueNoGenerator.getPrefixByType(request.getQueueType(), request.getTableType());
        
        // 查询当前店铺今天的最大序列号
        int todaySequence = getTodaySequence(request.getShopId(), prefix);
        String queueNo = QueueNoGenerator.generate(prefix, todaySequence);
        queue.setQueueNo(queueNo);
        
        log.info("🔢 生成排队号码 - 店铺ID: {}, 前缀: {}, 序号: {}, 号码: {}", 
                request.getShopId(), prefix, todaySequence, queueNo);
        
        // 设置默认值
        if (queue.getQueueStatus() == null) {
            queue.setQueueStatus(0); // 默认等待中
        }
        if (queue.getIsNotified() == null) {
            queue.setIsNotified(0);
        }
        if (queue.getNotifyCount() == null) {
            queue.setNotifyCount(0);
        }
        boolean success = queueNumberService.save(queue);
        
        // 3. 【新增】添加到Redis等待队列
        if (success) {
            long timestamp = System.currentTimeMillis();
            redisQueueService.addToWaitingQueue(request.getShopId(), queue.getId(), timestamp);
            log.info("✅ 已添加到Redis等待队列 - 店铺ID: {}, 排队ID: {}", request.getShopId(), queue.getId());
        }
        
        // 4. 【新增】取号成功后推送WebSocket通知
        if (success) {
            if (request.getUserId() == null) {
                log.warn("取号成功但未提供用户ID，无法推送WebSocket通知 - 排队号码: {}", queueNo);
            } else {
                try {
                    log.info("开始推送WebSocket通知 - 用户ID: {}, 排队号码: {}", request.getUserId(), queueNo);
                    
                    NotificationFeignClient.QueueNotificationRequest notificationRequest = new NotificationFeignClient.QueueNotificationRequest();
                    notificationRequest.setUserId(request.getUserId());
                    notificationRequest.setNotificationType("QUEUE_CREATED");
                    notificationRequest.setData(queue);
                    
                    Result<Boolean> pushResult = notificationFeignClient.pushQueueNotification(notificationRequest);
                    
                    if (pushResult != null && Boolean.TRUE.equals(pushResult.getData())) {
                        log.info("✅ WebSocket推送成功 - 用户ID: {}, 排队号码: {}", request.getUserId(), queueNo);
                    } else {
                        String errorMsg = pushResult != null ? pushResult.getMessage() : "返回结果为null";
                        log.warn("⚠️ WebSocket推送失败或不在线 - 用户ID: {}, 排队号码: {}, 原因: {}", 
                                request.getUserId(), queueNo, errorMsg);
                    }
                } catch (Exception e) {
                    // WebSocket推送失败不影响取号主流程
                    log.error("❌ WebSocket推送异常 - 用户ID: {}, 排队号码: {}, 错误: {}", 
                            request.getUserId(), queueNo, e.getMessage(), e);
                }
            }
        }
        
        return success ? Result.success(true) : Result.error("取号失败");
    }

    @Operation(
        summary = "更新排队记录",
        description = "更新排队记录信息，如果数据无变化则返回提示"
    )
    @PutMapping
    public Result<Boolean> updateQueue(@RequestBody @Valid QueueUpdateRequest request) {
        // 1. 查询原数据
        QueueNumber existingQueue = queueNumberService.getById(request.getId());
        if (existingQueue == null) {
            return Result.error("排队记录不存在");
        }
        
        // 2. 检查数据是否有变化
        boolean hasChanges = false;
        StringBuilder changeDesc = new StringBuilder();
        
        if (request.getUserId() != null && !request.getUserId().equals(existingQueue.getUserId())) {
            hasChanges = true;
            changeDesc.append("用户ID");
        }
        if (request.getPhone() != null && !request.getPhone().equals(existingQueue.getPhone())) {
            hasChanges = true;
            if (changeDesc.length() > 0) changeDesc.append(", ");
            changeDesc.append("联系电话");
        }
        if (request.getPartySize() != null && !request.getPartySize().equals(existingQueue.getPartySize())) {
            hasChanges = true;
            if (changeDesc.length() > 0) changeDesc.append(", ");
            changeDesc.append("用餐人数");
        }
        if (request.getTableType() != null && !request.getTableType().equals(existingQueue.getTableType())) {
            hasChanges = true;
            if (changeDesc.length() > 0) changeDesc.append(", ");
            changeDesc.append("桌台类型");
        }
        if (request.getRemark() != null && !request.getRemark().equals(existingQueue.getRemark())) {
            hasChanges = true;
            if (changeDesc.length() > 0) changeDesc.append(", ");
            changeDesc.append("备注");
        }
        if (request.getIsNotified() != null && !request.getIsNotified().equals(existingQueue.getIsNotified())) {
            hasChanges = true;
            if (changeDesc.length() > 0) changeDesc.append(", ");
            changeDesc.append("通知状态");
        }
        if (request.getNotifyCount() != null && !request.getNotifyCount().equals(existingQueue.getNotifyCount())) {
            hasChanges = true;
            if (changeDesc.length() > 0) changeDesc.append(", ");
            changeDesc.append("通知次数");
        }
        
        // 3. 如果数据无变化，返回提示信息
        if (!hasChanges) {
            return Result.success(true, "数据未发生变化，无需更新");
        }
        
        // 4. 有变化则执行更新
        if (request.getUserId() != null) {
            existingQueue.setUserId(request.getUserId());
        }
        if (request.getPhone() != null) {
            existingQueue.setPhone(request.getPhone());
        }
        if (request.getPartySize() != null) {
            existingQueue.setPartySize(request.getPartySize());
        }
        if (request.getTableType() != null) {
            existingQueue.setTableType(request.getTableType());
        }
        if (request.getRemark() != null) {
            existingQueue.setRemark(request.getRemark());
        }
        if (request.getIsNotified() != null) {
            existingQueue.setIsNotified(request.getIsNotified());
        }
        if (request.getNotifyCount() != null) {
            existingQueue.setNotifyCount(request.getNotifyCount());
        }
        
        boolean success = queueNumberService.updateById(existingQueue);
        return success ? Result.success(true, "更新成功: " + changeDesc.toString()) : Result.error("更新失败");
    }

    @Operation(summary = "删除排队记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQueue(
            @Parameter(description = "排队ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        boolean success = queueNumberService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新排队状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @Parameter(description = "排队ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "排队状态：0-等待中，1-已叫号，2-已入座，3-已取消，4-已过号", example = "1", required = true)
            @RequestParam("queueStatus") Integer queueStatus) {
        QueueNumber queue = queueNumberService.getById(id);
        if (queue == null) {
            return Result.error("排队记录不存在");
        }
        queue.setQueueStatus(queueStatus);
        boolean success = queueNumberService.updateById(queue);
        return success ? Result.success(true) : Result.error("更新状态失败");
    }

    @Operation(summary = "取消排队")
    @PutMapping("/{id}/cancel")
    public Result<Boolean> cancelQueue(
            @Parameter(description = "排队ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "取消原因", example = "用户主动取消")
            @RequestParam(value = "cancelReason", required = false) String cancelReason) {
        QueueNumber queue = queueNumberService.getById(id);
        if (queue == null) {
            return Result.error("排队记录不存在");
        }
        queue.setQueueStatus(3); // 已取消
        queue.setCancelReason(cancelReason);
        boolean success = queueNumberService.updateById(queue);
        
        // 从Redis等待队列移除
        if (success) {
            redisQueueService.removeFromWaitingQueue(queue.getShopId(), queue.getId());
            log.info("✅ 已从Redis等待队列移除 - 排队ID: {}", id);
        }
        
        return success ? Result.success(true) : Result.error("取消失败");
    }

    @Operation(
        summary = "叫号（Redis队列移动 + WebSocket推送）",
        description = "<font color='red'>【核心功能 - 已集成Redis】</font><br/>" +
                "将排队状态更新为'已叫号'，从Redis等待队列移动到叫号队列，并推送WebSocket通知<br/><br/>" +
                "<font color='green'>业务流程：</font><br/>" +
                "1. <b>验证状态</b> - 验证排队记录是否存在且为'等待中'状态<br/>" +
                "2. <b>更新数据库</b> - 更新排队状态为'已叫号'（queueStatus=1）<br/>" +
                "3. <font color='blue'>【Redis】</font>从等待队列移动到叫号队列（queue:waiting → queue:calling）<br/>" +
                "4. <font color='blue'>【WebSocket】</font>通过 notification-service 推送叫号通知给用户<br/><br/>" +
                "<font color='orange'>使用场景：</font>店员点击'叫号'按钮时调用此接口，用户手机会收到实时通知"
    )
    @PutMapping("/{id}/call")
    public Result<Boolean> callNumber(
            @Parameter(description = "排队ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        // 1. 查询排队记录
        QueueNumber queue = queueNumberService.getById(id);
        if (queue == null) {
            return Result.error("排队记录不存在");
        }
        
        // 2. 验证状态
        if (queue.getQueueStatus() != 0) {
            return Result.error("当前状态不是等待中，无法叫号");
        }
        
        // 3. 更新状态为已叫号
        queue.setQueueStatus(1); // 已叫号
        boolean success = queueNumberService.updateById(queue);
        
        if (!success) {
            return Result.error("叫号失败");
        }
        
        // 4. 【新增】移动到Redis叫号队列
        long callTimestamp = System.currentTimeMillis();
        redisQueueService.moveToCallingQueue(queue.getShopId(), queue.getId(), callTimestamp);
        log.info("✅ 已移动到Redis叫号队列 - 店铺ID: {}, 排队ID: {}", queue.getShopId(), id);
        
        // 5. 【新增】推送WebSocket通知
        if (queue.getUserId() != null) {
            try {
                log.info("开始推送叫号通知 - 用户ID: {}, 排队号码: {}", queue.getUserId(), queue.getQueueNo());
                
                NotificationFeignClient.QueueNotificationRequest notificationRequest = new NotificationFeignClient.QueueNotificationRequest();
                notificationRequest.setUserId(queue.getUserId());
                notificationRequest.setNotificationType("QUEUE_CALLED");
                notificationRequest.setData(queue);
                
                Result<Boolean> pushResult = notificationFeignClient.pushQueueNotification(notificationRequest);
                
                if (pushResult != null && Boolean.TRUE.equals(pushResult.getData())) {
                    log.info("✅ 叫号通知推送成功 - 用户ID: {}", queue.getUserId());
                } else {
                    String errorMsg = pushResult != null ? pushResult.getMessage() : "返回结果为null";
                    log.warn("⚠️ 叫号通知推送失败或不在线 - 用户ID: {}, 原因: {}", queue.getUserId(), errorMsg);
                }
            } catch (Exception e) {
                log.error("❌ 叫号通知推送异常 - 用户ID: {}, 错误: {}", queue.getUserId(), e.getMessage(), e);
            }
        }
        
        return Result.success(true);
    }

    @Operation(
        summary = "完成排队（Redis队列移动）",
        description = "<font color='red'>【Redis队列管理】</font><br/>" +
                "将排队状态更新为'已完成'，并从Redis叫号队列移动到完成队列<br/><br/>" +
                "<font color='green'>业务流程：</font><br/>" +
                "1. <b>更新数据库</b> - 更新排队状态为'已完成'（queueStatus=2）<br/>" +
                "2. <font color='blue'>【Redis】</font>从叫号队列移动到完成队列（queue:calling → queue:completed）<br/><br/>" +
                "<font color='orange'>使用场景：</font>用户入座后，店员点击'完成'按钮时调用"
    )
    @PutMapping("/{id}/complete")
    public Result<Boolean> completeQueue(
            @Parameter(description = "排队ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        QueueNumber queue = queueNumberService.getById(id);
        if (queue == null) {
            return Result.error("排队记录不存在");
        }
        
        queue.setQueueStatus(2); // 已完成
        boolean success = queueNumberService.updateById(queue);
        
        // 移动到Redis完成队列
        if (success) {
            redisQueueService.moveToCompletedQueue(queue.getShopId(), queue.getId());
            log.info("✅ 已移动到Redis完成队列 - 店铺ID: {}, 排队ID: {}", queue.getShopId(), id);
        }
        
        return success ? Result.success(true) : Result.error("操作失败");
    }

    @Operation(
        summary = "获取实时等待队列（Redis Sorted Set）",
        description = "<font color='red'>【实时数据 - Redis】</font><br/>" +
                "从Redis Sorted Set获取当前店铺的实时等待队列，按取号时间自动排序<br/><br/>" +
                "<font color='green'>技术实现：</font><br/>" +
                "- <b>Redis Key</b>: queue:waiting:{shopId}<br/>" +
                "- <b>数据结构</b>: Sorted Set（ZSET）<br/>" +
                "- <b>Score</b>: 取号时间戳（timestamp），保证先进先出<br/>" +
                "- <b>Value</b>: 排队ID（queueId）<br/><br/>" +
                "<font color='orange'>返回数据：</font><br/>" +
                "- waitingCount: 等待人数<br/>" +
                "- queueIds: 按顺序排列的排队ID列表<br/><br/>" +
                "<font color='blue'>测试方法：</font>先调用'排队取号'接口添加几个排队，然后调用此接口查看实时队列"
    )
    @GetMapping("/redis/waiting/{shopId}")
    public Result<Object> getRealTimeWaitingQueue(
            @Parameter(description = "店铺ID", example = "1", required = true, schema = @Schema(type = "integer"))
            @PathVariable("shopId") Long shopId) {
        // 获取等待人数
        Long count = redisQueueService.getWaitingCount(shopId);
        
        // 获取所有等待中的排队ID
        Set<String> waitingIds = redisQueueService.getWaitingQueue(shopId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("shopId", shopId);
        result.put("waitingCount", count);
        result.put("queueIds", waitingIds);
        
        return Result.success(result);
    }

    @Operation(
        summary = "获取实时叫号队列（Redis Sorted Set）",
        description = "<font color='red'>【实时数据 - Redis】</font><br/>" +
                "从Redis Sorted Set获取当前店铺的实时叫号队列（已叫号但未完成的排队）<br/><br/>" +
                "<font color='green'>技术实现：</font><br/>" +
                "- <b>Redis Key</b>: queue:calling:{shopId}<br/>" +
                "- <b>数据结构</b>: Sorted Set（ZSET）<br/>" +
                "- <b>Score</b>: 叫号时间戳（timestamp）<br/>" +
                "- <b>Value</b>: 排队ID（queueId）<br/><br/>" +
                "<font color='orange'>返回数据：</font><br/>" +
                "- callingIds: 已叫号的排队ID列表<br/><br/>" +
                "<font color='blue'>测试方法：</font>先调用'叫号'接口，然后调用此接口查看叫号队列"
    )
    @GetMapping("/redis/calling/{shopId}")
    public Result<Object> getRealTimeCallingQueue(
            @Parameter(description = "店铺ID", example = "1", required = true, schema = @Schema(type = "integer"))
            @PathVariable("shopId") Long shopId) {
        Set<String> callingIds = redisQueueService.getCallingQueue(shopId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("shopId", shopId);
        result.put("callingIds", callingIds);
        
        return Result.success(result);
    }

    @Operation(
        summary = "获取用户在队列中的位置（Redis ZRANK）",
        description = "<font color='red'>【实时查询 - Redis】</font><br/>" +
                "查询指定用户在等待队列中的排名（从0开始），用于告诉用户前面还有几人<br/><br/>" +
                "<font color='green'>技术实现：</font><br/>" +
                "- <b>Redis命令</b>: ZRANK queue:waiting:{shopId} {queueId}<br/>" +
                "- <b>返回值</b>: 排名（从0开始），-1表示不在队列中<br/><br/>" +
                "<font color='orange'>返回数据：</font><br/>" +
                "- position: 排名位置（0表示第一个）<br/>" +
                "- message: 友好提示（如'前面还有2人'）<br/><br/>" +
                "<font color='blue'>测试方法：</font><br/>" +
                "1. 先调用'排队取号'创建排队记录，记录返回的queueId<br/>" +
                "2. 再调用'排队取号'创建第二个排队<br/>" +
                "3. 调用此接口查询第二个排队的位置，应该返回 position=1（前面还有1人）"
    )
    @GetMapping("/redis/position/{shopId}/{queueId}")
    public Result<Object> getUserPosition(
            @Parameter(description = "店铺ID", example = "1", required = true, schema = @Schema(type = "integer"))
            @PathVariable("shopId") Long shopId,
            @Parameter(description = "排队ID", example = "1", required = true, schema = @Schema(type = "integer"))
            @PathVariable("queueId") Long queueId) {
        Long position = redisQueueService.getQueuePosition(shopId, queueId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("shopId", shopId);
        result.put("queueId", queueId);
        result.put("position", position);
        result.put("message", position == null || position == -1 ? "不在队列中" : "前面还有" + position + "人");
        
        return Result.success(result);
    }

    /**
     * 获取当前店铺今天的最大序列号（服务端生成）
     * @param shopId 店铺ID
     * @param prefix 前缀
     * @return 今天的序列号（从1开始）
     */
    private int getTodaySequence(Long shopId, String prefix) {
        // 查询今天该店铺该前缀的最大序列号
        String today = java.time.LocalDate.now().toString().replace("-", ""); // yyyyMMdd
        String pattern = prefix + today + "%";
        
        // 使用 MyBatis Plus 查询
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QueueNumber> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.likeRight(QueueNumber::getQueueNo, prefix + today)
               .eq(QueueNumber::getShopId, shopId)
               .orderByDesc(QueueNumber::getQueueNo)
               .last("LIMIT 1");
        
        QueueNumber lastQueue = queueNumberService.getOne(wrapper);
        
        if (lastQueue != null && lastQueue.getQueueNo() != null) {
            // 提取最后4位数字作为序列号
            String lastNo = lastQueue.getQueueNo();
            try {
                // 假设格式为: A20260518001，提取最后的数字
                String sequenceStr = lastNo.substring(lastNo.length() - 3);
                int lastSequence = Integer.parseInt(sequenceStr);
                return lastSequence + 1; // 递增
            } catch (Exception e) {
                log.warn("解析排队号码失败: {}", lastNo);
            }
        }
        
        return 1; // 第一个号码
    }
}
