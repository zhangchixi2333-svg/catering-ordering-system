package org.example.notificationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.notificationservice.entity.ChatMessageRecipient;

import java.util.List;

@Mapper
public interface ChatMessageRecipientMapper extends BaseMapper<ChatMessageRecipient> {

    @Select({
            "<script>",
            "SELECT * FROM chat_message_recipient",
            "WHERE message_id IN",
            "<foreach collection='messageIds' item='messageId' open='(' separator=',' close=')'>#{messageId}</foreach>",
            "</script>"
    })
    List<ChatMessageRecipient> findByMessageIds(@Param("messageIds") List<String> messageIds);
}
