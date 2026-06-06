package org.example.shopservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 桌台创建请求DTO
 */
@Data
@Schema(description = "桌台创建请求")
public class TableCreateRequest {

    @Schema(description = "所属店铺ID", example = "1", required = true)
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    @Schema(description = "桌台编号", example = "A01", required = true)
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
}
