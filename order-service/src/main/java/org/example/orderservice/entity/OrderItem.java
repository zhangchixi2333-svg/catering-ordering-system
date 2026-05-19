package org.example.orderservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细表实体类
 * 
 * <p>对应数据库表：order_item</p>
 * <p>存储订单中的每个菜品项信息</p>
 */
@Data
@TableName("order_item")
@Schema(description = "订单明细信息")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "明细ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID，关联 orders.id", example = "123")
    private Long orderId;

    @Schema(description = "订单编号，冗余字段便于查询", example = "ORD2026051900001")
    private String orderNo;

    @Schema(description = "菜品ID，关联 menu_item.id", example = "1")
    private Long itemId;

    @Schema(description = "菜品名称，冗余字段", example = "宫保鸡丁")
    private String itemName;

    @Schema(description = "菜品编码", example = "ITEM001")
    private String itemCode;

    @Schema(description = "菜品图片URL", example = "http://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "菜品单价（元）", example = "38.00")
    private BigDecimal price;

    @Schema(description = "购买数量", example = "2")
    private Integer quantity;

    @Schema(description = "小计金额（元），由服务端计算：price * quantity", example = "76.00")
    private BigDecimal subtotal;

    @Schema(description = "规格信息（JSON格式）", example = "{\"size\":\"大份\",\"spicy\":\"微辣\"}")
    private String specification;

    @Schema(description = "配料信息（JSON数组格式）", example = "[{\"name\":\"加蛋\",\"price\":3}]")
    private String toppings;

    @Schema(description = "单项备注", example = "微辣，不要香菜")
    private String remark;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
