package org.example.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.common.Result;
import org.example.paymentservice.entity.PaymentOrder;
import org.example.paymentservice.feign.OrderFeignClient;
import org.example.paymentservice.service.PaymentOrderService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 支付沙盒测试控制器
 * 用于沙盒环境模拟各种支付方式的回调
 * 
 * ⚠️ 注意：此接口仅用于开发和测试环境，生产环境应禁用
 */
@Slf4j
@Tag(name = "支付沙盒测试", description = "模拟各种支付方式的回调（仅测试环境）")
@RestController
@RequestMapping("/api/payment/sandbox")
@RequiredArgsConstructor
public class PaymentSandboxController {
    
    private final PaymentOrderService paymentOrderService;
    private final OrderFeignClient orderFeignClient;
    
    @Operation(
        summary = "模拟支付成功",
        description = "<font color='red'>【沙盒测试】</font><br/>" +
                "模拟第三方支付平台支付成功后的回调<br/><br/>" +
                "<font color='green'>支持的支付方式：</font><br/>" +
                "- wechat: 微信支付<br/>" +
                "- alipay: 支付宝<br/>" +
                "- cash: 现金支付<br/>" +
                "- member_card: 会员卡支付<br/>" +
                "- bank_card: 银行卡支付"
    )
    @PostMapping("/success/{paymentId}")
    public Result<Boolean> simulateSuccess(
            @Parameter(description = "支付ID", example = "1", required = true)
            @PathVariable("paymentId") Long paymentId) {
        
        log.info("\n========== 🧪 沙盒测试：模拟支付成功 ==========");
        log.info("【支付ID】{}", paymentId);
        
        // 1. 查询支付订单
        PaymentOrder payment = paymentOrderService.getById(paymentId);
        if (payment == null) {
            log.error("❌ 支付订单不存在 - ID: {}", paymentId);
            return Result.error("支付订单不存在");
        }
        
        // 2. 验证支付状态
        if (payment.getPaymentStatus() != null && payment.getPaymentStatus() >= 2) {
            log.warn("⚠️ 该支付订单已完成 - 当前状态: {}", payment.getPaymentStatus());
            return Result.error("该支付订单已完成");
        }
        
        log.info("【订单编号】{}", payment.getOrderNo());
        log.info("【订单ID】{}", payment.getOrderId());
        log.info("【支付金额】¥{}", payment.getPaymentAmount());
        log.info("【支付方式】{}", getPaymentMethodName(payment.getPaymentMethod()));
        
        // 3. 生成模拟的第三方支付交易号
        String transactionId = generateTransactionId(payment.getPaymentMethod());
        log.info("【模拟交易号】{}", transactionId);
        
        // 4. 更新支付订单状态为支付成功
        payment.setPaymentStatus(2); // 2-支付成功
        payment.setTransactionId(transactionId);
        payment.setPayTime(LocalDateTime.now());
        
        boolean updateSuccess = paymentOrderService.updateById(payment);
        if (!updateSuccess) {
            log.error("❌ 更新支付订单状态失败");
            return Result.error("更新支付状态失败");
        }
        
        log.info("✅ 支付订单状态已更新为：支付成功");
        
        // 5. 调用 order-service 更新订单状态为"待接单"并更新支付信息
        try {
            log.info("\n【步骤5】调用 order-service 更新订单状态和支付信息...");
            log.info("【调用接口】PUT /api/order/{}/status?orderStatus=1&paymentStatus=1&paymentMethod={}", 
                    payment.getOrderId(), payment.getPaymentMethod());
            log.info("【请求参数】订单ID: {}, 订单状态: 1(待接单), 支付状态: 1(已支付), 支付方式: {}", 
                    payment.getOrderId(), payment.getPaymentMethod());
            
            Result<Boolean> orderResult = orderFeignClient.updateOrderStatus(
                payment.getOrderId(), 
                1,  // 1-待接单
                1,  // 1-已支付
                payment.getPaymentMethod()  // 支付方式
            );
            
            if (orderResult != null && Boolean.TRUE.equals(orderResult.getData())) {
                log.info("✅ 订单状态和支付信息更新成功 - 订单ID: {}, 状态: 待接单, 支付状态: 已支付, 支付方式: {}", 
                        payment.getOrderId(), getPaymentMethodText(payment.getPaymentMethod()));
            } else {
                String errorMsg = orderResult != null ? orderResult.getMessage() : "返回结果为null";
                log.warn("⚠️ 订单状态和支付信息更新失败: {}", errorMsg);
                log.warn("⚠️ 但支付已成功，请手动处理或通过补偿任务修复");
            }
        } catch (Exception e) {
            log.error("❌ 调用 order-service 异常: {}", e.getMessage());
            log.error("⚠️ 但支付已成功，请手动处理或通过补偿任务修复");
        }
        
        log.info("==================================================\n");
        return Result.success(true);
    }
    
    @Operation(
        summary = "模拟支付失败",
        description = "<font color='red'>【沙盒测试】</font><br/>" +
                "模拟用户取消支付或支付失败的场景"
    )
    @PostMapping("/failure/{paymentId}")
    public Result<Boolean> simulateFailure(
            @Parameter(description = "支付ID", example = "1", required = true)
            @PathVariable("paymentId") Long paymentId,
            @Parameter(description = "失败原因", example = "用户取消支付")
            @RequestParam(value = "reason", required = false, defaultValue = "用户取消支付") String reason) {
        
        log.info("\n========== 🧪 沙盒测试：模拟支付失败 ==========");
        log.info("【支付ID】{}", paymentId);
        log.info("【失败原因】{}", reason);
        
        PaymentOrder payment = paymentOrderService.getById(paymentId);
        if (payment == null) {
            log.error("❌ 支付订单不存在 - ID: {}", paymentId);
            return Result.error("支付订单不存在");
        }
        
        // 更新为支付失败状态
        payment.setPaymentStatus(3); // 3-支付失败
        paymentOrderService.updateById(payment);
        
        log.info("✅ 支付订单状态已更新为：支付失败");
        log.info("==================================================\n");
        
        return Result.success(true);
    }
    
    @Operation(
        summary = "模拟支付超时",
        description = "<font color='red'>【沙盒测试】</font><br/>" +
                "模拟用户长时间未完成支付的场景"
    )
    @PostMapping("/timeout/{paymentId}")
    public Result<Boolean> simulateTimeout(
            @Parameter(description = "支付ID", example = "1", required = true)
            @PathVariable("paymentId") Long paymentId) {
        
        log.info("\n========== 🧪 沙盒测试：模拟支付超时 ==========");
        log.info("【支付ID】{}", paymentId);
        
        PaymentOrder payment = paymentOrderService.getById(paymentId);
        if (payment == null) {
            log.error("❌ 支付订单不存在 - ID: {}", paymentId);
            return Result.error("支付订单不存在");
        }
        
        // 保持支付中状态，但记录超时时间
        log.info("⚠️ 支付超时，订单保持'支付中'状态，等待后续确认");
        log.info("==================================================\n");
        
        return Result.success(true);
    }
    
    /**
     * 生成模拟的第三方支付交易号
     */
    private String generateTransactionId(Integer paymentMethod) {
        String prefix;
        if (paymentMethod == null) {
            prefix = "PAY";
        } else {
            switch (paymentMethod) {
                case 1:
                    prefix = "WX";
                    break;
                case 2:
                    prefix = "ALI";
                    break;
                case 3:
                    prefix = "CASH";
                    break;
                case 4:
                    prefix = "MEMBER";
                    break;
                case 5:
                    prefix = "BANK";
                    break;
                default:
                    prefix = "PAY";
            }
        }
        
        return prefix + System.currentTimeMillis() + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
    
    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(Integer paymentMethod) {
        if (paymentMethod == null) {
            return "未知";
        }
        
        switch (paymentMethod) {
            case 1:
                return "微信支付";
            case 2:
                return "支付宝";
            case 3:
                return "现金支付";
            case 4:
                return "会员卡支付";
            case 5:
                return "银行卡支付";
            default:
                return "未知(" + paymentMethod + ")";
        }
    }
    
    /**
     * 获取支付方式文本（简化版）
     */
    private String getPaymentMethodText(Integer paymentMethod) {
        if (paymentMethod == null) {
            return "未知";
        }
        
        switch (paymentMethod) {
            case 1:
                return "微信";
            case 2:
                return "支付宝";
            case 3:
                return "现金";
            case 4:
                return "会员卡";
            case 5:
                return "银行卡";
            default:
                return "未知";
        }
    }
}