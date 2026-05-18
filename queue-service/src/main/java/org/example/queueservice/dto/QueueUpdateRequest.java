package org.example.queueservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "排队记录更新请求")
public class QueueUpdateRequest {

    @Schema(description = "排队ID", example = "1", required = true)
    @NotNull(message = "排队ID不能为空")
    private Long id;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "联系电话", example = "13800138001")
    private String phone;

    @Schema(description = "用餐人数", example = "3")
    private Integer partySize;

    @Schema(description = "期望桌台类型：1-普通桌，2-卡座，3-包厢", example = "1")
    private Integer tableType;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "叫号时间")
    private String callTime;

    @Schema(description = "入座时间")
    private String seatTime;

    @Schema(description = "是否已通知：0-否，1-是", example = "0")
    private Integer isNotified;

    @Schema(description = "通知次数", example = "0")
    private Integer notifyCount;
}
