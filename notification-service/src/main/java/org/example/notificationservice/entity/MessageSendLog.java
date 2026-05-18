package org.example.notificationservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("message_send_log")
@Schema(description = "消息发送记录")
public class MessageSendLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "消息ID", example = "MSG2026051700001")
    private String messageId;

    @Schema(description = "模板编码", example = "SMS_ORDER_CREATE")
    private String templateCode;

    @Schema(description = "接收者类型：1-用户，2-店员，3-后厨", example = "1")
    private Integer recipientType;

    @Schema(description = "接收者ID", example = "1001")
    private Long recipientId;

    @Schema(description = "手机号", example = "13800138001")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "微信OpenID")
    private String openId;

    @Schema(description = "设备Token（APP推送）")
    private String deviceToken;

    @Schema(description = "消息类型：1-短信，2-微信，3-APP推送，4-邮件，5-语音", example = "1")
    private Integer messageType;

    @Schema(description = "业务类型", example = "ORDER_CREATE")
    private String businessType;

    @Schema(description = "业务ID（订单号、排队号等）", example = "ORD2026051700001")
    private String businessId;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容", example = "【美味餐厅】您的订单ORD2026051700001已创建，金额98.00元，请及时支付。")
    private String content;

    @Schema(description = "发送状态：0-待发送，1-发送中，2-发送成功，3-发送失败", example = "2")
    private Integer sendStatus;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "接收时间")
    private LocalDateTime receiveTime;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetry;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "渠道返回数据")
    private String channelResponse;

    @Schema(description = "发送耗时（毫秒）")
    private Integer duration;

    @Schema(description = "发送成本（元）", example = "0.0500")
    private BigDecimal cost;

    @Schema(description = "扩展数据，JSON格式")
    private String extraData;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
