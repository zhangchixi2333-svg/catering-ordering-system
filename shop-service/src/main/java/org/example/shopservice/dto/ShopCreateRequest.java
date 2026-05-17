package org.example.shopservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 店铺创建请求DTO
 */
@Data
@Schema(description = "店铺创建请求")
public class ShopCreateRequest {

    @Schema(description = "店铺名称", example = "美味餐厅旗舰店", required = true)
    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    @Schema(description = "店铺编码", example = "SHOP001", required = true)
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

    @Schema(description = "店铺描述信息", example = "主营川菜、湘菜")
    private String description;

    @Schema(description = "店铺Logo图片URL", example = "https://example.com/logo.jpg")
    private String logoUrl;
}
