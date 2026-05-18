package org.example.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.paymentservice.common.Result;
import org.example.paymentservice.dto.PaymentOrderCreateRequest;
import org.example.paymentservice.entity.PaymentOrder;
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
        summary = "创建支付订单",
        description = "创建新的支付订单，系统会自动生成支付单号",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "支付订单创建请求",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PaymentOrderCreateRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "创建支付订单示例",
                    value = "{\"orderNo\": \"ORD2026051700001\", \"shopId\": 1, \"userId\": 1001, \"paymentAmount\": 98.00, \"paymentMethod\": 1, \"subject\": \"美味餐厅订单支付\", \"body\": \"宫保鸡丁等3件商品\", \"clientIp\": \"192.168.1.100\"}"
                )
            )
        )
    )
    @PostMapping
    public Result<Boolean> createPayment(@RequestBody @Valid PaymentOrderCreateRequest request) {
        PaymentOrder payment = new PaymentOrder();
        BeanUtils.copyProperties(request, payment);
        // 生成支付单号
        payment.setPaymentNo(PaymentNoGenerator.generate());
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
