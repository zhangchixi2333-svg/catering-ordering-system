package org.example.queueservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.queueservice.common.Result;
import org.example.queueservice.dto.QueueTakeNumberRequest;
import org.example.queueservice.entity.QueueNumber;
import org.example.queueservice.service.QueueNumberService;
import org.example.queueservice.util.QueueNoGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "排队管理", description = "排队取号、叫号管理")
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueNumberController {

    private final QueueNumberService queueNumberService;

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

    @Operation(summary = "排队取号")
    @PostMapping
    public Result<Boolean> takeNumber(@RequestBody @Valid QueueTakeNumberRequest request) {
        QueueNumber queue = new QueueNumber();
        BeanUtils.copyProperties(request, queue);
        
        // 生成排队号码：前缀 + 序列号
        String prefix = QueueNoGenerator.getPrefixByType(request.getQueueType(), request.getTableType());
        // 简单实现：使用当前时间戳后4位作为序列号（实际应该从数据库查询当前店铺的最大序列号+1）
        int sequence = (int) (System.currentTimeMillis() % 10000);
        String queueNo = QueueNoGenerator.generate(prefix, sequence);
        queue.setQueueNo(queueNo);
        
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
        return success ? Result.success(true) : Result.error("取号失败");
    }

    @Operation(summary = "更新排队记录")
    @PutMapping
    public Result<Boolean> updateQueue(@RequestBody QueueNumber queue) {
        boolean success = queueNumberService.updateById(queue);
        return success ? Result.success(true) : Result.error("更新失败");
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
        return success ? Result.success(true) : Result.error("取消失败");
    }
}
