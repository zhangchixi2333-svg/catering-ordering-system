package org.example.menuservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单变更日志实体类
 */
@Data
@TableName("menu_change_log")
@Schema(description = "菜单变更日志")
public class MenuChangeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "菜品ID", example = "1")
    private Long itemId;

    @Schema(description = "店铺ID", example = "1")
    private Long shopId;

    @Schema(description = "变更类型：1-上架，2-下架，3-价格调整，4-库存调整", example = "1")
    private Integer changeType;

    @Schema(description = "变更前的值", example = "38.00")
    private String oldValue;

    @Schema(description = "变更后的值", example = "42.00")
    private String newValue;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "备注说明", example = "成本上涨，调整价格")
    private String remark;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
