package org.example.userservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
@Schema(description = "系统菜单")
public class SysMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "父菜单ID（0表示顶级菜单）")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单编码（唯一标识）")
    private String menuCode;

    @Schema(description = "菜单类型：1-目录，2-菜单，3-按钮/权限")
    private Integer menuType;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "权限标识（如：queue:call）")
    private String permission;

    @Schema(description = "是否可见：0-隐藏，1-显示")
    private Integer visible;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
