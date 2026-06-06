package org.example.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "消息发送请求")
public class MessageSendRequest {

    @Schema(description = "模板编码", example = "SMS_ORDER_CREATE", required = true)
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "消息类型：1-短信，2-邮件，3-微信模板消息，4-APP推送", example = "1")
    private Integer messageType;

    @Schema(description = "接收者类型：1-用户，2-店员，3-后厨", example = "1", required = true)
    @NotNull(message = "接收者类型不能为空")
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

    @Schema(description = "业务类型", example = "ORDER_CREATE", required = true)
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @Schema(description = "业务ID（订单号、排队号等）", example = "ORD2026051700001")
    private String businessId;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容", example = "【美味餐厅】您的订单ORD2026051700001已创建，金额98.00元，请及时支付。", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetry;

    @Schema(description = "扩展数据，JSON格式")
    private String extraData;
}
