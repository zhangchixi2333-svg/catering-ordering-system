package org.example.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付订单创建请求DTO
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>✅ 只包含客户端必须提供的字段</li>
 *   <li>❌ 不包含服务端获取的字段（金额、店铺ID等）</li>
 *   <li>✅ 金额从order-service获取订单的actualAmount</li>
 *   <li>✅ 店铺ID从order-service获取订单的shopId</li>
 * </ul>
 */
@Data
@Schema(description = "支付订单创建请求")
public class PaymentOrderCreateRequest {

    @Schema(description = "关联订单编号", example = "ORD2026051700001", required = true)
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    @Schema(description = "关联订单ID(可选,后续通过orderNo查询)", example = "1")
    private Long orderId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "支付方式：1-微信，2-支付宝，3-现金，4-会员卡，5-银行卡", example = "1", required = true)
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    @Schema(description = "货币类型，默认CNY", example = "CNY")
    private String currency;

    @Schema(description = "支付主题", example = "美味餐厅订单支付")
    private String subject;

    @Schema(description = "支付描述", example = "宫保鸡丁等3件商品")
    private String body;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "异步通知地址(可选,从配置读取)")
    private String notifyUrl;

    @Schema(description = "同步返回地址(可选,从配置读取)")
    private String returnUrl;

    @Schema(description = "扩展参数，JSON格式")
    private String extraParams;
}
