package org.example.shopservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺配置实体类
 */
@Data
@TableName("shop_config")
@Schema(description = "店铺配置")
public class ShopConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "配置键名")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置说明")
    private String configDesc;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
