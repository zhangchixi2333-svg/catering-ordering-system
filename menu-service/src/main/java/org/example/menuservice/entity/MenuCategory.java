package org.example.menuservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单分类实体类
 */
@Data
@TableName("menu_category")
@Schema(description = "菜单分类")
public class MenuCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属店铺ID", example = "1")
    private Long shopId;

    @Schema(description = "分类名称", example = "热菜")
    private String categoryName;

    @Schema(description = "父分类ID，0表示一级分类", example = "0")
    private Long parentId;

    @Schema(description = "排序号，数字越小越靠前", example = "1")
    private Integer sortOrder;

    @Schema(description = "分类图标URL", example = "https://example.com/icon.jpg")
    private String iconUrl;

    @Schema(description = "是否可见：0-隐藏，1-显示", example = "1")
    private Integer isVisible;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
