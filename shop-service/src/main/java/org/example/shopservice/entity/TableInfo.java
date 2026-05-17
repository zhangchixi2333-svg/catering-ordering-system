package org.example.shopservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌台信息实体类
 */
@Data
@TableName("table_info")
@Schema(description = "桌台信息")
public class TableInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "桌台ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属店铺ID", example = "1")
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    @Schema(description = "桌台编号", example = "A01")
    @NotBlank(message = "桌台编号不能为空")
    private String tableNumber;

    @Schema(description = "桌台名称", example = "A区1号桌")
    private String tableName;

    @Schema(description = "座位数量", example = "4")
    private Integer seats;

    @Schema(description = "桌台类型：1-普通桌，2-卡座，3-包厢，4-吧台", example = "1")
    private Integer tableType;

    @Schema(description = "桌台状态：0-空闲，1-已占用，2-预订，3-清洁中", example = "0")
    private Integer tableStatus;

    @Schema(description = "扫码点餐二维码URL", example = "QR_A01_SHOP001")
    private String qrCode;

    @Schema(description = "位置描述", example = "A区靠窗")
    private String location;

    @Schema(description = "是否可用：0-不可用，1-可用", example = "1")
    private Integer isAvailable;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
