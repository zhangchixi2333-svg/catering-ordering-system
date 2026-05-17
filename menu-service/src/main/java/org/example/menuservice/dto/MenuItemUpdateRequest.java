package org.example.menuservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品更新请求DTO
 */
@Data
@Schema(description = "菜品更新请求")
public class MenuItemUpdateRequest {

    @Schema(description = "菜品ID", example = "1", required = true)
    @NotNull(message = "菜品ID不能为空")
    private Long id;

    @Schema(description = "菜品名称", example = "宫保鸡丁", required = true)
    @NotBlank(message = "菜品名称不能为空")
    private String itemName;

    @Schema(description = "菜品编码", example = "ITEM001")
    private String itemCode;

    @Schema(description = "菜品描述", example = "经典川菜，鸡肉鲜嫩，花生酥脆")
    private String description;

    @Schema(description = "菜品价格（元）", example = "38.00", required = true)
    @NotNull(message = "菜品价格不能为空")
    @Positive(message = "菜品价格必须大于0")
    private BigDecimal price;

    @Schema(description = "原价（元），用于显示折扣", example = "45.00")
    private BigDecimal originalPrice;

    @Schema(description = "成本价（元）", example = "20.00")
    private BigDecimal costPrice;

    @Schema(description = "计量单位", example = "份")
    private String unit;

    @Schema(description = "菜品图片URL", example = "https://example.com/dish.jpg")
    private String imageUrl;

    @Schema(description = "库存数量，-1表示无限制", example = "-1")
    private Integer stock;

    @Schema(description = "预计制作时间（分钟）", example = "15")
    private Integer preparationTime;

    @Schema(description = "辣度等级：0-不辣，1-微辣，2-中辣，3-特辣", example = "2")
    private Integer spicyLevel;

    @Schema(description = "是否推荐：0-否，1-是", example = "1")
    private Integer isRecommended;

    @Schema(description = "是否可售：0-下架，1-上架", example = "1")
    private Integer isAvailable;

    @Schema(description = "标签，多个用逗号分隔", example = "招牌,必点")
    private String tags;

    @Schema(description = "主要食材", example = "鸡肉,花生,辣椒")
    private String ingredients;

    @Schema(description = "过敏原信息", example = "花生")
    private String allergens;

    @Schema(description = "热量（卡路里）", example = "350")
    private Integer calories;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;
}
