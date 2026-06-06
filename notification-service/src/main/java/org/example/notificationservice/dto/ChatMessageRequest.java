package org.example.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatMessageRequest {
    private Long senderId;
    private String senderName;
    private String senderRole;
    private Long shopId;
    private List<Long> recipientIds;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
