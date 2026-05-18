package org.example.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.notificationservice.common.Result;
import org.example.notificationservice.dto.MessageSendRequest;
import org.example.notificationservice.entity.MessageSendLog;
import org.example.notificationservice.service.MessageSendLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "消息通知管理", description = "消息发送、查询")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class MessageSendLogController {

    private final MessageSendLogService messageSendLogService;

    @Operation(summary = "获取所有消息发送记录列表")
    @GetMapping("/list")
    public Result<List<MessageSendLog>> listMessages() {
        List<MessageSendLog> messages = messageSendLogService.list();
        return Result.success(messages);
    }

    @Operation(summary = "根据ID获取消息发送记录详情")
    @GetMapping("/{id}")
    public Result<MessageSendLog> getMessageById(
            @Parameter(description = "日志ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        MessageSendLog message = messageSendLogService.getById(id);
        if (message == null) {
            return Result.error("消息发送记录不存在");
        }
        return Result.success(message);
    }

    @Operation(summary = "根据消息ID获取记录")
    @GetMapping("/msg/{messageId}")
    public Result<MessageSendLog> getMessageByMsgId(
            @Parameter(description = "消息ID", example = "MSG2026051700001", required = true)
            @PathVariable("messageId") String messageId) {
        MessageSendLog message = messageSendLogService.getByMessageId(messageId);
        if (message == null) {
            return Result.error("消息发送记录不存在");
        }
        return Result.success(message);
    }

    @Operation(summary = "根据业务ID获取消息列表")
    @GetMapping("/business/{businessId}")
    public Result<List<MessageSendLog>> getMessagesByBusiness(
            @Parameter(description = "业务ID（订单号、排队号等）", example = "ORD2026051700001", required = true)
            @PathVariable("businessId") String businessId) {
        List<MessageSendLog> messages = messageSendLogService.getByBusinessId(businessId);
        return Result.success(messages);
    }

    @Operation(summary = "根据手机号获取消息列表")
    @GetMapping("/phone/{phone}")
    public Result<List<MessageSendLog>> getMessagesByPhone(
            @Parameter(description = "手机号", example = "13800138001", required = true)
            @PathVariable("phone") String phone) {
        List<MessageSendLog> messages = messageSendLogService.getByPhone(phone);
        return Result.success(messages);
    }

    @Operation(summary = "根据状态获取消息列表")
    @GetMapping("/status/{sendStatus}")
    public Result<List<MessageSendLog>> getMessagesByStatus(
            @Parameter(description = "发送状态：0-待发送，1-发送中，2-发送成功，3-发送失败", example = "2", required = true)
            @PathVariable("sendStatus") Integer sendStatus) {
        List<MessageSendLog> messages = messageSendLogService.getByStatus(sendStatus);
        return Result.success(messages);
    }

    @Operation(summary = "发送消息")
    @PostMapping
    public Result<Boolean> sendMessage(@RequestBody @Valid MessageSendRequest request) {
        MessageSendLog message = new MessageSendLog();
        BeanUtils.copyProperties(request, message);
        // 设置默认值
        if (message.getSendStatus() == null) {
            message.setSendStatus(0); // 默认待发送
        }
        if (message.getRetryCount() == null) {
            message.setRetryCount(0);
        }
        if (message.getMaxRetry() == null) {
            message.setMaxRetry(3); // 默认重试3次
        }
        boolean success = messageSendLogService.save(message);
        return success ? Result.success(true) : Result.error("发送失败");
    }

    @Operation(summary = "更新消息发送记录")
    @PutMapping
    public Result<Boolean> updateMessage(@RequestBody MessageSendLog message) {
        boolean success = messageSendLogService.updateById(message);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除消息发送记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMessage(
            @Parameter(description = "日志ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        boolean success = messageSendLogService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新消息发送状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @Parameter(description = "日志ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "发送状态：0-待发送，1-发送中，2-发送成功，3-发送失败", example = "2", required = true)
            @RequestParam("sendStatus") Integer sendStatus) {
        MessageSendLog message = messageSendLogService.getById(id);
        if (message == null) {
            return Result.error("消息发送记录不存在");
        }
        message.setSendStatus(sendStatus);
        boolean success = messageSendLogService.updateById(message);
        return success ? Result.success(true) : Result.error("更新状态失败");
    }
}
