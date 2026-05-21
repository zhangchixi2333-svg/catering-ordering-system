package org.example.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.common.Result;
import org.example.paymentservice.dto.PaymentOrderCreateRequest;
import org.example.paymentservice.entity.PaymentOrder;
import org.example.paymentservice.feign.OrderFeignClient;
import org.example.paymentservice.service.PaymentOrderService;
import org.example.paymentservice.util.PaymentNoGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "支付订单管理", description = "支付订单的增删改查")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentOrderController {

    private final PaymentOrderService paymentOrderService;
    private final OrderFeignClient orderFeignClient;

    @Operation(summary = "获取所有支付订单列表")
    @GetMapping("/list")
    public Result<List<PaymentOrder>> listPayments() {
        List<PaymentOrder> payments = paymentOrderService.list();
        return Result.success(payments);
    }

    @Operation(summary = "根据ID获取支付订单详情")
    @GetMapping("/{id}")
    public Result<PaymentOrder> getPaymentById(
            @Parameter(description = "支付ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        PaymentOrder payment = paymentOrderService.getById(id);
        if (payment == null) {
            return Result.error("支付订单不存在");
        }
        return Result.success(payment);
    }

    @Operation(summary = "根据支付单号获取支付订单")
    @GetMapping("/no/{paymentNo}")
    public Result<PaymentOrder> getPaymentByNo(
            @Parameter(description = "支付单号", example = "PAY2026051700001", required = true)
            @PathVariable("paymentNo") String paymentNo) {
        PaymentOrder payment = paymentOrderService.getByPaymentNo(paymentNo);
        if (payment == null) {
            return Result.error("支付订单不存在");
        }
        return Result.success(payment);
    }

    @Operation(summary = "根据订单编号获取支付订单")
    @GetMapping("/order/{orderNo}")
    public Result<PaymentOrder> getPaymentByOrderNo(
            @Parameter(description = "订单编号", example = "ORD2026051700001", required = true)
            @PathVariable("orderNo") String orderNo) {
        PaymentOrder payment = paymentOrderService.getByOrderNo(orderNo);
        if (payment == null) {
            return Result.error("支付订单不存在");
        }
        return Result.success(payment);
    }

    @Operation(summary = "根据店铺ID获取支付订单列表")
    @GetMapping("/shop/{shopId}")
    public Result<List<PaymentOrder>> getPaymentsByShop(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<PaymentOrder> payments = paymentOrderService.getByShopId(shopId);
        return Result.success(payments);
    }

    @Operation(summary = "根据用户ID获取支付订单列表")
    @GetMapping("/user/{userId}")
    public Result<List<PaymentOrder>> getPaymentsByUser(
            @Parameter(description = "用户ID", example = "1001", required = true)
            @PathVariable("userId") Long userId) {
        List<PaymentOrder> payments = paymentOrderService.getByUserId(userId);
        return Result.success(payments);
    }

    @Operation(summary = "根据状态获取支付订单列表")
    @GetMapping("/status/{paymentStatus}")
    public Result<List<PaymentOrder>> getPaymentsByStatus(
            @Parameter(description = "支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败，4-已退款", example = "2", required = true)
            @PathVariable("paymentStatus") Integer paymentStatus) {
        List<PaymentOrder> payments = paymentOrderService.getByStatus(paymentStatus);
        return Result.success(payments);
    }

    @Operation(
        summary = "创建支付订单（验证订单+使用订单金额）",
        description = "<font color='red'>【重构优化】</font><br/>" +
                "创建新的支付订单，系统会自动验证订单信息<br/><br/>" +
                "<font color='green'>业务规则：</font><br/>" +
                "1. 调用 order-service 验证订单是否存在 - 不存在则返回错误<br/>" +
                "2. 验证订单是否已支付 - 已支付则返回错误<br/>" +
                "3. <b>使用订单的actualAmount作为支付金额</b> - 不使用前端传入的金额<br/>" +
                "4. <b>使用订单的shopId作为店铺ID</b> - 不使用前端传入的店铺ID<br/>" +
                "5. 生成支付单号并设置默认值<br/><br/>" +
                "<font color='orange'>安全优势：</font>防止客户端篡改支付金额，确保支付金额与订单金额一致"
    )
    @PostMapping
    public Result<Boolean> createPayment(@RequestBody @Valid PaymentOrderCreateRequest request) {
        log.info("\n========== 开始创建支付订单 ==========");
        log.info("【请求参数】订单编号: {}", request.getOrderNo());
        
        // 1. 调用 order-service 验证订单是否存在
        Result<OrderFeignClient.OrderInfoDTO> orderResult = orderFeignClient.getOrderByOrderNo(request.getOrderNo());
        
        // 检查是否触发了熔断降级
        if (orderResult == null || orderResult.getCode() != 200) {
            String errorMsg = orderResult != null ? orderResult.getMessage() : "返回结果为null";
            log.error("❌ 订单服务调用失败: {}", errorMsg);
            return Result.error("订单服务暂时不可用，请稍后重试");
        }
        
        if (orderResult.getData() == null) {
            log.error("❌ 订单不存在 - 订单编号: {}", request.getOrderNo());
            return Result.error("订单不存在，订单编号: " + request.getOrderNo());
        }
        
        OrderFeignClient.OrderInfoDTO orderInfo = orderResult.getData();
        log.info("✅ 订单验证成功 - 订单ID: {}, 金额: ¥{}", orderInfo.getId(), orderInfo.getActualAmount());
        
        // 2. 验证订单是否已支付
        if (orderInfo.getPaymentStatus() != null && orderInfo.getPaymentStatus() == 1) {
            return Result.error("订单已支付，请勿重复支付");
        }
        
        // 3. 创建支付订单（使用订单的金额和店铺ID）
        PaymentOrder payment = new PaymentOrder();
        BeanUtils.copyProperties(request, payment);
        payment.setPaymentNo(PaymentNoGenerator.generate());
        payment.setPaymentAmount(orderInfo.getActualAmount());  // 从订单获取金额
        payment.setShopId(orderInfo.getShopId());               // 从订单获取店铺ID
        
        // 设置默认值
        payment.setPaymentStatus(1); // 1-支付中（用户点击支付后立即设置为支付中）
        if (payment.getCurrency() == null) {
            payment.setCurrency("CNY"); // 默认人民币
        }
        
        boolean success = paymentOrderService.save(payment);
        if (success) {
            log.info("✅ 支付订单创建成功 - 支付单号: {}, 金额: ¥{}", payment.getPaymentNo(), payment.getPaymentAmount());
        }
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新支付订单信息")
    @PutMapping("/{id}")
    public Result<Boolean> updatePayment(
            @Parameter(description = "支付ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @RequestBody PaymentOrder payment) {
        payment.setId(id);
        boolean success = paymentOrderService.updateById(payment);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除支付订单")
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePayment(
            @Parameter(description = "支付ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        boolean success = paymentOrderService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新支付状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @Parameter(description = "支付ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败，4-已退款", example = "2", required = true)
            @RequestParam("paymentStatus") Integer paymentStatus) {
        PaymentOrder payment = paymentOrderService.getById(id);
        if (payment == null) {
            return Result.error("支付订单不存在");
        }
        payment.setPaymentStatus(paymentStatus);
        boolean success = paymentOrderService.updateById(payment);
        return success ? Result.success(true) : Result.error("更新状态失败");
    }

    @Operation(
        summary = "支付成功回调（第三方平台调用）",
        description = "<font color='red'>【核心功能】</font><br/>" +
                "第三方支付平台支付成功后，调用此接口更新支付状态和订单状态<br/><br/>" +
                "<font color='green'>业务流程：</font><br/>" +
                "1. <b>验证支付订单</b> - 验证支付订单是否存在且为'支付中'状态<br/>" +
                "2. <b>更新支付订单</b> - 设置支付状态为'支付成功'（paymentStatus=2），记录支付时间和第三方支付交易号<br/>" +
                "3. <b>调用order-service</b> - 通过 Feign 客户端调用order-service更新订单状态<br/>" +
                "4. <b>更新订单状态</b> - 将订单状态更新为'待接单'（orderStatus=1），将支付状态更新为'已支付'（paymentStatus=1）<br/><br/>" +
                "<font color='blue'>💡 使用场景：</font><br/>" +
                "- 微信支付、支付宝等第三方支付平台支付成功后，异步通知本系统<br/>" +
                "- 前端轮询检测到支付成功后，后端调用此接口确认状态<br/><br/>" +
                "<font color='orange'>⚠️ 注意事项：</font><br/>" +
                "- transactionId 由第三方支付平台提供，用于对账和退款<br/>" +
                "- payTime 由服务端自动生成，不使用前端传入的时间<br/>" +
                "- 如果order-service不可用，会返回错误，由前端决定是否重试"
    )
    @PutMapping("/{id}/success")
    public Result<Boolean> paymentSuccess(
            @Parameter(description = "支付ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "第三方支付交易号", example = "WX20260517113000123456", required = true)
            @RequestParam("transactionId") String transactionId) {
        // 1. 查询支付订单
        PaymentOrder payment = paymentOrderService.getById(id);
        if (payment == null) {
            return Result.error("支付订单不存在");
        }
        
        // 2. 验证支付状态（只能是待支付或支付中才能设置为成功）
        if (payment.getPaymentStatus() != null && payment.getPaymentStatus() >= 2) {
            return Result.error("该支付订单已完成，无需重复支付");
        }
        
        System.out.println("\n========== 支付成功处理开始 ==========");
        System.out.println("支付ID: " + id);
        System.out.println("订单编号: " + payment.getOrderNo());
        System.out.println("支付金额: ¥" + payment.getPaymentAmount());
        System.out.println("第三方支付交易号: " + transactionId);
        
        // 3. 更新支付订单状态为支付成功
        payment.setPaymentStatus(2); // 2-支付成功
        payment.setTransactionId(transactionId);
        payment.setPayTime(java.time.LocalDateTime.now()); // ✅ 服务端生成支付时间
        
        boolean paymentUpdateSuccess = paymentOrderService.updateById(payment);
        if (!paymentUpdateSuccess) {
            System.err.println("❌ 更新支付订单状态失败");
            return Result.error("更新支付状态失败");
        }
        System.out.println("✅ 支付订单状态已更新为：支付成功");
        
        // 4. 调用order-service更新订单状态为"待接单"
        log.info("\n步骤4: 调用order-service更新订单状态...");
        log.info("【调用接口】PUT /api/order/{}/status?orderStatus=1", payment.getOrderId());
        log.info("【目标服务】order-service (通过Eureka发现)");
        log.info("【请求参数】订单ID: {}, 订单状态: 1(待接单)", payment.getOrderId());
        
        try {
            Result<Boolean> orderResult = orderFeignClient.updateOrderStatus(
                payment.getOrderId(), 
                1  // 1-待接单
            );
            
            if (orderResult != null && Boolean.TRUE.equals(orderResult.getData())) {
                log.info("✅ 订单状态更新成功");
                log.info("  - 订单ID: {}", payment.getOrderId());
                log.info("  - 新状态: 待接单");
                log.info("==========================================\n");
                return Result.success(true);
            } else {
                String errorMsg = orderResult != null ? orderResult.getMessage() : "返回结果为null";
                log.error("❌ 订单状态更新失败 - 错误信息: {}", errorMsg);
                log.error("⚠️ 但支付已成功，请手动处理或通过补偿任务修复");
                log.error("==========================================\n");
                return Result.error("支付成功，但订单状态更新失败: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("❌ 调用order-service异常 - 异常信息: {}", e.getMessage());
            log.error("【堆栈跟踪】", e);
            log.error("⚠️ 但支付已成功，请手动处理或通过补偿任务修复");
            log.error("==========================================\n");
            return Result.error("支付成功，但无法联系订单服务，请稍后重试");
        }
    }
}
