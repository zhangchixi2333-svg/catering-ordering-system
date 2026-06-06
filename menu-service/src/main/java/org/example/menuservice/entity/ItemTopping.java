package org.example.menuservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品配料实体类
 */
@Data
@TableName("item_topping")
@Schema(description = "菜品配料")
public class ItemTopping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配料ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "菜品ID", example = "3")
    private Long itemId;

    @Schema(description = "配料名称", example = "加蛋")
    private String toppingName;

    @Schema(description = "配料价格（元）", example = "3.00")
    private BigDecimal price;

    @Schema(description = "是否必选：0-可选，1-必选", example = "0")
    private Integer isRequired;

    @Schema(description = "最多可选数量，0表示不限制", example = "2")
    private Integer maxSelect;

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
