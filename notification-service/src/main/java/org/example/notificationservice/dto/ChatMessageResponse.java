package org.example.notificationservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ChatMessageResponse {
    private String messageId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private List<Long> recipientIds;
    private Long shopId;
    private String content;
    private LocalDateTime sendTime;
    private boolean delivered;
    private Map<Long, Boolean> recipientOnlineStatus;
}
