package org.example.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细项请求DTO
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>✅ 包含菜品基本信息和数量</li>
 *   <li>✅ 支持单项备注和规格选择</li>
 *   <li>❌ 不包含小计金额（服务端计算）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * {
 *   "itemId": 1,
 *   "itemName": "宫保鸡丁",
 *   "price": 38.00,
 *   "quantity": 2,
 *   "remark": "微辣，不要香菜",
 *   "specification": "{\"size\":\"大份\",\"spicy\":\"微辣\"}",
 *   "toppings": "[{\"name\":\"加蛋\",\"price\":3}]"
 * }
 * }</pre>
 */
@Data
@Schema(description = "订单明细项")
public class OrderItemRequest {

    @Schema(description = "菜品ID（从菜单服务获取）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜品ID不能为空")
    private Long itemId;

    @Schema(description = "菜品名称（用于展示和记录）", example = "宫保鸡丁", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜品名称不能为空")
    private String itemName;

    @Schema(description = "菜品单价（元），服务端会验证此价格", example = "38.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜品单价不能为空")
    @Positive(message = "菜品单价必须大于0")
    private BigDecimal price;

    @Schema(description = "购买数量（至少为1）", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;

    @Schema(description = "单项备注（如：微辣、不要香菜）", example = "微辣，不要香菜")
    private String remark;

    @Schema(description = "规格信息（JSON格式），如：{\"size\":\"大份\",\"spicy\":\"微辣\"}", 
            example = "{\"size\":\"大份\",\"spicy\":\"微辣\"}")
    private String specification;

    @Schema(description = "配料信息（JSON数组格式），如：[{\"name\":\"加蛋\",\"price\":3}]",
            example = "[{\"name\":\"加蛋\",\"price\":3},{\"name\":\"加火腿\",\"price\":5}]")
    private String toppings;
}
