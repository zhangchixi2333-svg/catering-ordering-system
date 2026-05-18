package org.example.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单创建请求DTO
 */
@Data
@Schema(description = "订单创建请求")
public class OrderCreateRequest {

    @Schema(description = "店铺ID", example = "1", required = true)
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    @Schema(description = "桌台ID", example = "1")
    private Long tableId;

    @Schema(description = "桌台编号", example = "A01")
    private String tableNumber;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "订单类型：1-堂食，2-外带，3-外卖", example = "1", required = true)
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @Schema(description = "订单总金额（元）", example = "98.00", required = true)
    @NotNull(message = "订单总金额不能为空")
    @Positive(message = "订单总金额必须大于0")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额（元）", example = "0.00")
    private BigDecimal discountAmount;

    @Schema(description = "实付金额（元）", example = "98.00", required = true)
    @NotNull(message = "实付金额不能为空")
    @Positive(message = "实付金额必须大于0")
    private BigDecimal actualAmount;

    @Schema(description = "菜品总数量", example = "3", required = true)
    @NotNull(message = "菜品总数量不能为空")
    @Positive(message = "菜品总数量必须大于0")
    private Integer itemCount;

    @Schema(description = "订单备注", example = "少辣")
    private String remark;

    @Schema(description = "排队号码", example = "A001")
    private String queueNumber;

    @Schema(description = "预计等待时间（分钟）", example = "15")
    private Integer estimatedTime;

    @Schema(description = "优先级：0-普通，1-加急", example = "0")
    private Integer priority;
}
