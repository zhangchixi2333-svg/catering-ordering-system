package org.example.notificationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.notificationservice.entity.MessageSendLog;

@Mapper
public interface MessageSendLogMapper extends BaseMapper<MessageSendLog> {
}
