package org.example.paymentservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_order")
@Schema(description = "支付订单")
public class PaymentOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "支付ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "支付单号", example = "PAY2026051700001")
    private String paymentNo;

    @Schema(description = "关联订单编号", example = "ORD2026051700001")
    private String orderNo;

    @Schema(description = "关联订单ID", example = "1")
    private Long orderId;

    @Schema(description = "店铺ID", example = "1")
    private Long shopId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "支付金额（元）", example = "98.00")
    private BigDecimal paymentAmount;

    @Schema(description = "支付方式：1-微信，2-支付宝，3-现金，4-会员卡，5-银行卡", example = "1")
    private Integer paymentMethod;

    @Schema(description = "支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败，4-已退款", example = "2")
    private Integer paymentStatus;

    @Schema(description = "货币类型", example = "CNY")
    private String currency;

    @Schema(description = "支付主题", example = "美味餐厅订单支付")
    private String subject;

    @Schema(description = "支付描述", example = "宫保鸡丁等3件商品")
    private String body;

    @Schema(description = "第三方支付交易号", example = "WX20260517113000123456")
    private String transactionId;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;

    @Schema(description = "支付成功时间")
    private LocalDateTime payTime;

    @Schema(description = "支付过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "退款金额（元）", example = "0.00")
    private BigDecimal refundAmount;

    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "异步通知地址")
    private String notifyUrl;

    @Schema(description = "同步返回地址")
    private String returnUrl;

    @Schema(description = "扩展参数，JSON格式")
    private String extraParams;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
