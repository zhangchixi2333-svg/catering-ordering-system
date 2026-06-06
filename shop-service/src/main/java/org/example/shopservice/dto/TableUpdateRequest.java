package org.example.shopservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 桌台更新请求DTO
 */
@Data
@Schema(description = "桌台更新请求")
public class TableUpdateRequest {

    @Schema(description = "桌台ID", example = "1", required = true)
    @NotNull(message = "桌台ID不能为空")
    private Long id;

    @Schema(description = "桌台名称", example = "A区1号桌（VIP）")
    private String tableName;

    @Schema(description = "座位数量", example = "4")
    private Integer seats;

    @Schema(description = "桌台类型：1-普通桌，2-卡座，3-包厢，4-吧台", example = "3")
    private Integer tableType;

    @Schema(description = "桌台状态：0-空闲，1-已占用，2-预订，3-清洁中", example = "0")
    private Integer tableStatus;

    @Schema(description = "扫码点餐二维码URL", example = "QR_A01_SHOP001_VIP")
    private String qrCode;

    @Schema(description = "位置描述", example = "A区靠窗（景观位）")
    private String location;

    @Schema(description = "是否可用：0-不可用，1-可用", example = "1")
    private Integer isAvailable;
}
