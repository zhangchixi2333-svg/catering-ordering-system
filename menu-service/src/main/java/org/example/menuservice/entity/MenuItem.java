package org.example.menuservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品信息实体类
 */
@Data
@TableName("menu_item")
@Schema(description = "菜品信息")
public class MenuItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜品ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属店铺ID", example = "1")
    private Long shopId;

    @Schema(description = "所属分类ID", example = "3")
    private Long categoryId;

    @Schema(description = "菜品名称", example = "宫保鸡丁")
    private String itemName;

    @Schema(description = "菜品编码", example = "ITEM001")
    private String itemCode;

    @Schema(description = "菜品描述", example = "经典川菜，鸡肉鲜嫩，花生酥脆")
    private String description;

    @Schema(description = "菜品价格（元）", example = "38.00")
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

    @Schema(description = "销售数量", example = "1250")
    private Integer salesCount;

    @Schema(description = "评分，范围0-5", example = "4.80")
    private BigDecimal rating;

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

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
