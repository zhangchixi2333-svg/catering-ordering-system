package org.example.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建请求DTO
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>✅ 只包含客户端必须提供的字段</li>
 *   <li>❌ 不包含服务端计算的字段（金额、数量等）</li>
 *   <li>❌ 不包含服务端查询的字段（桌台编号、排队号码等）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * {
 *   "shopId": 1,
 *   "userId": 1001,
 *   "orderType": 1,
 *   "queueNumber": "A001",
 *   "remark": "不要辣，少盐",
 *   "items": [
 *     {
 *       "itemId": 1,
 *       "itemName": "宫保鸡丁",
 *       "price": 38.00,
 *       "quantity": 2,
 *       "remark": "微辣"
 *     },
 *     {
 *       "itemId": 5,
 *       "itemName": "酸辣汤",
 *       "price": 18.00,
 *       "quantity": 1
 *     }
 *   ]
 * }
 * }</pre>
 */
@Data
@Schema(description = "订单创建请求")
public class OrderCreateRequest {

    @Schema(description = "店铺ID（从店铺服务获取）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    @Schema(description = "桌台ID（堂食时必填，外带/外卖可为null）", example = "5")
    private Long tableId;

    @Schema(description = "用户ID（当前登录用户）", example = "1001")
    private Long userId;

    @Schema(description = "订单类型：1-堂食，2-外带，3-外卖", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @Schema(description = "排队号码（可选，关联已叫号的排队记录，如：A001、B002）", example = "A001")
    private String queueNumber;

    @Schema(description = "订单备注（如：不要辣、少盐）", example = "不要辣，少盐")
    private String remark;

    @Schema(description = "订单明细列表（至少包含一个菜品）", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<OrderItemRequest> items;
}
