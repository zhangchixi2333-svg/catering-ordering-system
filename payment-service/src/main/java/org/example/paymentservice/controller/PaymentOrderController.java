package org.example.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.paymentservice.common.Result;
import org.example.paymentservice.dto.PaymentOrderCreateRequest;
import org.example.paymentservice.entity.PaymentOrder;
import org.example.paymentservice.feign.OrderFeignClient;
import org.example.paymentservice.service.PaymentOrderService;
import org.example.paymentservice.util.PaymentNoGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        // 1. 调用 order-service 验证订单是否存在
        Result<OrderFeignClient.OrderInfoDTO> orderResult = null;
        try {
            orderResult = orderFeignClient.getOrderByOrderNo(request.getOrderNo());
        } catch (Exception e) {
            return Result.error("订单服务暂时不可用，请稍后重试");
        }
        
        if (orderResult == null || orderResult.getData() == null) {
            return Result.error("订单不存在，订单编号: " + request.getOrderNo());
        }
        
        OrderFeignClient.OrderInfoDTO orderInfo = orderResult.getData();
        
        // 2. 验证订单是否已支付
        if (orderInfo.getPaymentStatus() != null && orderInfo.getPaymentStatus() == 1) {
            return Result.error("订单已支付，请勿重复支付");
        }
        
        // 3. 创建支付订单（使用订单的金额和店铺ID）
        PaymentOrder payment = new PaymentOrder();
        BeanUtils.copyProperties(request, payment);
        // 生成支付单号
        payment.setPaymentNo(PaymentNoGenerator.generate());
        // ✅ 服务端设置金额和店铺ID
        payment.setPaymentAmount(orderInfo.getActualAmount());  // 从订单获取金额
        payment.setShopId(orderInfo.getShopId());               // 从订单获取店铺ID
        // 设置默认值
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(0); // 默认待支付
        }
        if (payment.getCurrency() == null) {
            payment.setCurrency("CNY"); // 默认人民币
        }
        boolean success = paymentOrderService.save(payment);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新支付订单信息")
    @PutMapping
    public Result<Boolean> updatePayment(@RequestBody PaymentOrder payment) {
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
}
