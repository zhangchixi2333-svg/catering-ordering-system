package org.example.shopservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺信息实体类
 */
@Data
@TableName("shop_info")
@Schema(description = "店铺信息")
public class ShopInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "店铺名称", example = "美味餐厅旗舰店")
    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    @Schema(description = "店铺编码", example = "SHOP001")
    @NotBlank(message = "店铺编码不能为空")
    private String shopCode;

    @Schema(description = "店铺详细地址", example = "北京市朝阳区建国路88号")
    private String address;

    @Schema(description = "店铺联系电话", example = "010-12345678")
    private String phone;

    @Schema(description = "营业时间", example = "09:00-22:00")
    private String businessHours;

    @Schema(description = "店铺状态：0-停业，1-营业，2-装修中", example = "1")
    private Integer shopStatus;

    @Schema(description = "店铺总座位数", example = "120")
    private Integer capacity;

    @Schema(description = "桌台数量", example = "20")
    private Integer tableCount;

    @Schema(description = "店铺类型：1-快餐店，2-中餐厅，3-西餐厅，4-咖啡厅，5-其他", example = "1")
    private Integer shopType;

    @Schema(description = "店铺描述信息", example = "主营川菜、湘菜")
    private String description;

    @Schema(description = "店铺Logo图片URL", example = "https://example.com/logo.jpg")
    private String logoUrl;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
