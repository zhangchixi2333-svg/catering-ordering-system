package org.example.notificationservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String messageId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private Long shopId;
    private String content;
    private Integer delivered;
    private LocalDateTime sendTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
