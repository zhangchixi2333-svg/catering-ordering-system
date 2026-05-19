package org.example.orderservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体类
 */
@Data
@TableName("orders")
@Schema(description = "订单信息")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单编号", example = "ORD2026051700001")
    private String orderNo;

    @Schema(description = "店铺ID", example = "1")
    private Long shopId;

    @Schema(description = "桌台ID", example = "1")
    private Long tableId;

    @Schema(description = "桌台编号", example = "A01")
    private String tableNumber;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "订单类型：1-堂食，2-外带，3-外卖", example = "1")
    private Integer orderType;

    @Schema(description = "订单状态：0-待支付，1-待接单，2-制作中，3-待取餐，4-已完成，5-已取消", example = "1")
    private Integer orderStatus;

    @Schema(description = "订单总金额（元）", example = "98.00")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额（元）", example = "0.00")
    private BigDecimal discountAmount;

    @Schema(description = "实付金额（元）", example = "98.00")
    private BigDecimal actualAmount;

    @Schema(description = "菜品总数量", example = "3")
    private Integer itemCount;

    @Schema(description = "订单备注", example = "少辣")
    private String remark;

    @Schema(description = "支付方式：1-微信，2-支付宝，3-现金，4-会员卡", example = "1")
    private Integer paymentMethod;

    @Schema(description = "支付状态：0-未支付，1-已支付", example = "1")
    private Integer paymentStatus;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "接单时间")
    private LocalDateTime acceptTime;

    @Schema(description = "开始制作时间")
    private LocalDateTime prepareTime;

    @Schema(description = "制作完成时间")
    private LocalDateTime readyTime;

    @Schema(description = "订单完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因", example = "用户取消")
    private String cancelReason;

    @Schema(description = "排队ID", example = "17")
    private Long queueId;

    @Schema(description = "排队号码", example = "A001")
    private String queueNumber;

    @Schema(description = "预计等待时间（分钟）", example = "15")
    private Integer estimatedTime;

    @Schema(description = "优先级：0-普通，1-加急", example = "0")
    private Integer priority;

    @Schema(description = "是否已评价：0-否，1-是", example = "0")
    private Integer isEvaluated;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
