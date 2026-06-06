package org.example.shopservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 桌台状态更新请求DTO
 */
@Data
@Schema(description = "桌台状态更新请求")
public class TableStatusUpdateRequest {

    @Schema(description = "桌台状态：0-空闲，1-已占用，2-预订，3-清洁中", example = "1", required = true)
    @NotNull(message = "桌台状态不能为空")
    private Integer tableStatus;
}
