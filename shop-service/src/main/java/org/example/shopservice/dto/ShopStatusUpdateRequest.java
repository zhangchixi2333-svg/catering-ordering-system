package org.example.shopservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 店铺状态更新请求DTO
 */
@Data
@Schema(description = "店铺状态更新请求")
public class ShopStatusUpdateRequest {

    @Schema(description = "店铺状态：0-停业，1-营业，2-装修中", example = "1", required = true)
    @NotNull(message = "店铺状态不能为空")
    private Integer shopStatus;
}
