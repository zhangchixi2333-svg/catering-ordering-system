package org.example.notificationservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message_recipient")
public class ChatMessageRecipient {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String messageId;
    private Long recipientId;
    private Integer delivered;
    private LocalDateTime deliveredTime;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
