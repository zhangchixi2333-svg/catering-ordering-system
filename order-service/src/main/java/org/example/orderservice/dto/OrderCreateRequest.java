package org.example.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单创建请求DTO
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>✅ 只包含客户端必须提供的字段</li>
 *   <li>❌ 不包含服务端计算的字段（金额、数量等）</li>
 *   <li>❌ 不包含服务端查询的字段（桌台编号、排队号码等）</li>
 * </ul>
 */
@Data
@Schema(description = "订单创建请求")
public class OrderCreateRequest {

    @Schema(description = "店铺ID", example = "1", required = true)
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    @Schema(description = "桌台ID（堂食必填）", example = "5")
    private Long tableId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "订单类型：1-堂食，2-外带，3-外卖", example = "1", required = true)
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @Schema(description = "排队ID（可选，关联排队号）", example = "1")
    private Long queueId;

    @Schema(description = "订单备注", example = "不要辣，少盐")
    private String remark;
}
