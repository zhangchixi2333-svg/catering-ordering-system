package org.example.queueservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("queue_number")
@Schema(description = "排队号码")
public class QueueNumber implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "排队ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "排队号码", example = "A001")
    private String queueNo;

    @Schema(description = "店铺ID", example = "1")
    private Long shopId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "联系电话", example = "13800138001")
    private String phone;

    @Schema(description = "用餐人数", example = "3")
    private Integer partySize;

    @Schema(description = "排队类型：1-堂食，2-外带", example = "1")
    private Integer queueType;

    @Schema(description = "期望桌台类型：1-普通桌，2-卡座，3-包厢，NULL-不限制", example = "1")
    private Integer tableType;

    @Schema(description = "排队状态：0-等待中，1-已叫号，2-已入座，3-已取消，4-已过号", example = "0")
    private Integer queueStatus;

    @Schema(description = "当前排队位置", example = "2")
    private Integer currentPosition;

    @Schema(description = "前方等待人数", example = "1")
    private Integer totalAhead;

    @Schema(description = "预计等待时间（分钟）", example = "10")
    private Integer estimatedWaitTime;

    @Schema(description = "叫号时间")
    private LocalDateTime callTime;

    @Schema(description = "入座时间")
    private LocalDateTime seatTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "是否已通知：0-否，1-是", example = "0")
    private Integer isNotified;

    @Schema(description = "通知次数", example = "0")
    private Integer notifyCount;

    @Schema(description = "最后通知时间")
    private LocalDateTime lastNotifyTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间（取号时间）")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
