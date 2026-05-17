package org.example.menuservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品规格实体类
 */
@Data
@TableName("item_specification")
@Schema(description = "菜品规格")
public class ItemSpecification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "规格ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "菜品ID", example = "1")
    private Long itemId;

    @Schema(description = "规格名称", example = "大份")
    private String specName;

    @Schema(description = "规格类型：size-份量，spicy-辣度，temperature-温度", example = "size")
    private String specType;

    @Schema(description = "价格调整（元），可正可负", example = "10.00")
    private BigDecimal priceAdjustment;

    @Schema(description = "该规格库存，-1表示无限制", example = "-1")
    private Integer stock;

    @Schema(description = "是否默认规格：0-否，1-是", example = "1")
    private Integer isDefault;

    @Schema(description = "是否可用：0-不可用，1-可用", example = "1")
    private Integer isAvailable;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
