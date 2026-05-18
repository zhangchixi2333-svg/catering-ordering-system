package org.example.notificationservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.notificationservice.entity.MessageSendLog;
import org.example.notificationservice.mapper.MessageSendLogMapper;
import org.example.notificationservice.service.MessageSendLogService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageSendLogServiceImpl extends ServiceImpl<MessageSendLogMapper, MessageSendLog> implements MessageSendLogService {
    
    @Override
    public MessageSendLog getByMessageId(String messageId) {
        LambdaQueryWrapper<MessageSendLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageSendLog::getMessageId, messageId);
        return getOne(wrapper);
    }
    
    @Override
    public List<MessageSendLog> getByBusinessId(String businessId) {
        LambdaQueryWrapper<MessageSendLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageSendLog::getBusinessId, businessId).orderByDesc(MessageSendLog::getCreatedAt);
        return list(wrapper);
    }
    
    @Override
    public List<MessageSendLog> getByPhone(String phone) {
        LambdaQueryWrapper<MessageSendLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageSendLog::getPhone, phone).orderByDesc(MessageSendLog::getCreatedAt);
        return list(wrapper);
    }
    
    @Override
    public List<MessageSendLog> getByStatus(Integer sendStatus) {
        LambdaQueryWrapper<MessageSendLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageSendLog::getSendStatus, sendStatus).orderByDesc(MessageSendLog::getCreatedAt);
        return list(wrapper);
    }
}
