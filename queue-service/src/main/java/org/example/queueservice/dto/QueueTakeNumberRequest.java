package org.example.queueservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "排队取号请求")
public class QueueTakeNumberRequest {

    @Schema(description = "店铺ID", example = "1", required = true)
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "联系电话", example = "13800138001", required = true)
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @Schema(description = "用餐人数", example = "3", required = true)
    @NotNull(message = "用餐人数不能为空")
    @Positive(message = "用餐人数必须大于0")
    private Integer partySize;

    @Schema(description = "排队类型：1-堂食，2-外带", example = "1", required = true)
    @NotNull(message = "排队类型不能为空")
    private Integer queueType;

    @Schema(description = "期望桌台类型：1-普通桌，2-卡座，3-包厢，NULL-不限制", example = "1")
    private Integer tableType;

    @Schema(description = "备注")
    private String remark;
}
