package org.example.notificationservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.notificationservice.entity.MessageSendLog;
import java.util.List;

public interface MessageSendLogService extends IService<MessageSendLog> {
    MessageSendLog getByMessageId(String messageId);
    List<MessageSendLog> getByBusinessId(String businessId);
    List<MessageSendLog> getByPhone(String phone);
    List<MessageSendLog> getByStatus(Integer sendStatus);
}
