package org.example.shopservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 店铺更新请求DTO
 */
@Data
@Schema(description = "店铺更新请求")
public class ShopUpdateRequest {

    @Schema(description = "店铺ID", example = "1", required = true)
    @NotNull(message = "店铺ID不能为空")
    private Long id;

    @Schema(description = "店铺名称", example = "美味餐厅旗舰店（已装修）")
    private String shopName;

    @Schema(description = "店铺详细地址", example = "北京市朝阳区建国路88号")
    private String address;

    @Schema(description = "店铺联系电话", example = "010-12345678")
    private String phone;

    @Schema(description = "营业时间", example = "09:00-23:00")
    private String businessHours;

    @Schema(description = "店铺状态：0-停业，1-营业，2-装修中", example = "1")
    private Integer shopStatus;

    @Schema(description = "店铺总座位数", example = "150")
    private Integer capacity;

    @Schema(description = "店铺描述信息", example = "装修升级，环境更好")
    private String description;

    @Schema(description = "店铺Logo图片URL", example = "https://example.com/logo-new.jpg")
    private String logoUrl;
}
